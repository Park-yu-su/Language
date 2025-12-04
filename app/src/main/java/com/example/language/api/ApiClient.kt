package com.example.language.api

import android.content.Context
import android.util.Log
import com.example.language.BuildConfig
import com.example.language.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

object ApiClient {
    private const val SERVER_IP = "43.201.171.76"
    private const val PORT = 2121
    private const val TAG = "ApiClient"

    val json = Json { ignoreUnknownKeys = true }

    private fun createCustomSocketFactory(context: Context): SSLSocketFactory {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val inputStream = context.resources.openRawResource(R.raw.server)
        val certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
        inputStream.close()

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("server", certificate)

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)

        return sslContext.socketFactory
    }

    private suspend inline fun <reified ReqPayload, reified RespPayload> executeRequest(
        context: Context,
        request: ClientRequest<ReqPayload>,
        fileBytes: ByteArray? = null
    ): ApiResponse<RespPayload> { // 반환 타입의 제네릭을 RespPayload로 지정
        return withContext(Dispatchers.IO) {
            var socket: Socket? = null
            try {
                val factory = createCustomSocketFactory(context)
                socket = factory.createSocket(SERVER_IP, PORT)

                val outputStream = DataOutputStream(socket.getOutputStream())
                val inputStream = DataInputStream(socket.getInputStream())

                // 이제 'request'의 타입이 명확하므로 에러 없이 직렬화됩니다.
                val requestJson = json.encodeToString(request)
                val requestBytes = requestJson.toByteArray(Charsets.UTF_8)

                Log.d(TAG, "Sent JSON: $requestJson")
                Log.d(TAG, "Sent JSON's size: ${requestBytes.size}")

                // outputStream.writeInt(requestBytes.size)
                // outputStream.flush()

                val size = requestBytes.size
                val lengthBytes = byteArrayOf(
                    (size shr 24).toByte(), // Int 의 첫 8비트
                    (size shr 16).toByte(), // Int 의 두 번째 8비트
                    (size shr 8).toByte(),  // Int 의 세 번째 8비트
                    size.toByte()           // Int 의 마지막 8비트
                )
                outputStream.write(lengthBytes) // 생성된 4바이트 배열을 전송
                outputStream.flush()


                outputStream.write(requestBytes)
                outputStream.flush()

                fileBytes?.let {
                    outputStream.write(it)
                    outputStream.flush()
                    Log.d(TAG, "Sent File: ${it.size} bytes")
                }

                val responseLength = inputStream.readInt()
                if (responseLength <= 0) {
                    throw Exception("서버로부터 유효하지 않은 길이의 응답을 받았습니다.")
                }
                Log.d(TAG, "Received Bytes: $responseLength")
                val responseBytes = ByteArray(responseLength)
                inputStream.readFully(responseBytes)
                val responseJson = String(responseBytes, Charsets.UTF_8)
                Log.d(TAG, "Received JSON: $responseJson")

                val genericResponse = json.decodeFromString<GenericServerResponse>(responseJson)

                return@withContext when (genericResponse.status) {
                    "ACCEPT" -> {
                        // 성공 시, payload를 RespPayload 타입으로 변환
                        val successPayload = json.decodeFromJsonElement<RespPayload>(genericResponse.payload)
                        ApiResponse.Success(successPayload)
                    }
                    "REJECT", "ERROR" -> {
                        val errorPayload = try {
                            json.decodeFromJsonElement<SimpleMessagePayload>(genericResponse.payload)
                        } catch (e: Exception) {
                            SimpleMessagePayload("서버 페이로드 파싱에 실패했습니다: ${genericResponse.payload}")
                        }
                        ApiResponse.Error(genericResponse.code, errorPayload.message)
                    }
                    else -> {
                        ApiResponse.Error("UNKNOWN_STATUS", "알 수 없는 상태값: ${genericResponse.status}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "통신 중 에러 발생", e)
                return@withContext ApiResponse.Error("NETWORK_ERROR", e.message ?: "네트워크 오류가 발생했습니다.")
            } finally {
                socket?.close()
            }
        }
    }

    // --- 각 API 호출 함수들은 executeRequest를 호출할 때 자동으로 타입을 추론하므로 수정할 필요가 없습니다. ---



    /**인증 관련**/
    suspend fun authenticate(context: Context, email: String, nickname: String, image: String, oneline: String): ApiResponse<AuthResponsePayload> {
        val payload = AuthRequestPayload(email, nickname, image, oneline)
        val request = ClientRequest("Authentication", payload)
        return executeRequest(context, request)
    }


    /**친구 관련**/

    suspend fun getFriendList(context: Context, uid: Int): ApiResponse<FriendListResponsePayload> {
        val payload = FriendListRequestPayload(uid)
        val request = ClientRequest("Friend", payload)
        return executeRequest(context, request)
    }

    suspend fun addFriend(context: Context, requesterId: Int, requestieId: Int): ApiResponse<SimpleMessagePayload> {
        val payload = FriendRequestPayload(requesterId, requestieId)
        val request = ClientRequest("Request", payload)
        return executeRequest(context, request)
    }

    suspend fun acceptFriend(context: Context, requesterId: Int, requestieId: Int): ApiResponse<SimpleMessagePayload> {
        val payload = FriendRequestPayload(requesterId, requestieId)
        val request = ClientRequest("Accept", payload)
        return executeRequest(context, request)
    }

    suspend fun rejectFriend(context: Context, requesterId: Int, requestieId: Int): ApiResponse<SimpleMessagePayload> {
        val payload = FriendRequestPayload(requesterId, requestieId)
        val request = ClientRequest("Reject", payload)
        return executeRequest(context, request)
    }

    suspend fun getPendingRequests(context: Context, uid: Int, type: String): ApiResponse<FriendListResponsePayload> {
        val payload = PendingRequestsPayload(uid, type)
        val request = ClientRequest("PendingRequests", payload)
        return executeRequest(context, request)
    }

    suspend fun deleteFriend(context: Context, requesterID: Int, requestieID: Int): ApiResponse<SimpleMessagePayload> {
        val payload = FriendRequestPayload(requesterID, requestieID)
        val request = ClientRequest("DeleteFriend", payload)
        return executeRequest(context, request)
    }


    /**특정 파일 보내기 관련**/

    suspend fun sendVoiceForSTT(context: Context, fileBytes: ByteArray, fileName: String, answer: String): ApiResponse<SttResponsePayload> {
        val payload = SttRequestPayload(fileName, fileBytes.size.toLong(), answer)
        val request = ClientRequest("STT", payload)
        return executeRequest(context, request, fileBytes)
    }

    suspend fun uploadImagesForDictionary(
        context: Context,
        fileNames: List<String>,
        fileSizes: List<Long>,
        combinedFileBytes: ByteArray
    ): ApiResponse<DictionaryResponsePayload> {

        val payload = DictionaryRequestPayload(
            cnt = fileNames.size.toString(),
            file_name = fileNames,
            file_size = fileSizes
        )

        val request = ClientRequest("Dictionary", payload)

        return executeRequest(context, request, combinedFileBytes)
    }

    /**단어장 업데이트 및 태그 등록 관련**/

    suspend fun registerWordbook(context: Context, payload: WordbookRegisterRequestPayload): ApiResponse<WordbookRegisterResponsePayload> {
        val request = ClientRequest("Wordbook", payload)
        return executeRequest(context, request)
    }

    suspend fun updateWordbook(context: Context, payload: WordbookUpdateRequestPayload): ApiResponse<WordbookUpdateResponsePayload> {
        val request = ClientRequest("WordbookUpdate", payload)
        return executeRequest(context, request)
    }

    suspend fun deleteWordbook(context: Context, wid: String, ownerUid: String): ApiResponse<WordbookDeleteResponsePayload> {
        val payload = WordbookDeleteRequestPayload(wid, ownerUid)
        val request = ClientRequest("WordbookDelete", payload)
        return executeRequest(context, request)
    }

    suspend fun updateTag(context: Context, payload: TagUpdateRequestPayload): ApiResponse<SimpleMessagePayload> {
        val request = ClientRequest("TagUpdate", payload)
        return executeRequest(context, request)
    }

    /**단어장 get 관련 + tag 가져오기 관련**/

    //단어장 ID로 단어장의 단어들을 얻는 함수
    suspend fun getWordbook(context: Context, wid: Int): ApiResponse<GetWordbookResponsePayload> {
        val payload = GetWordbookRequestPayload(wid)
        val request = ClientRequest("GetWordbook", payload)
        return executeRequest(context, request)
    }

    //태그 String을 줄 때 -> 이에 매칭되는 태그가 있으면 태그 id를 주는 함수
    suspend fun searchTag(context: Context, query: String): ApiResponse<SearchTagResponsePayload> {
        val payload = SearchTagRequestPayload(query)
        val request = ClientRequest("SearchTag", payload)
        return executeRequest(context, request)
    }

    // 태그로 단어장 검색
    suspend fun searchWordbook(context: Context, tids: List<Int>): ApiResponse<SearchWordbookResponsePayload> {
        val payload = SearchWordbookRequestPayload(tids)
        val request = ClientRequest("SearchWordbook", payload)
        return executeRequest(context, request)
    }

    // 태그로 단어장 검색 (OR)
    suspend fun searchWordbookOr(context: Context, tids: List<Int>): ApiResponse<SearchWordbookResponsePayload>{
        val payload = SearchWordbookRequestPayload(tids)
        val request = ClientRequest("SearchWordbookOr", payload)
        return executeRequest(context, request)
    }



    //단어장 구독(내 단어장 추가)
    suspend fun subscribe(context: Context, wid: Int, subscriber: Int): ApiResponse<SimpleMessagePayload> {
        val payload = SubscribeRequestPayload(wid, subscriber)
        val request = ClientRequest("Subscribe", payload)
        return executeRequest(context, request)
    }

    //단어장 구독 취소(내 단어장에서 삭제)
    suspend fun cancelSubscription(context: Context, wid: Int, subscriber: Int): ApiResponse<SimpleMessagePayload> {
        // SubscribeRequestPayload 재사용
        val payload = SubscribeRequestPayload(wid, subscriber)
        val request = ClientRequest("Cancel", payload)
        return executeRequest(context, request)
    }

    //구독된 단어장 목록 가져오기
    suspend fun getSubscribedWordbooks(context: Context, uid: Int): ApiResponse<GetSubscribedWordbooksResponsePayload> {
        // FriendListRequestPayload 재사용
        val payload = FriendListRequestPayload(uid)
        val request = ClientRequest("GetSubscribedWordbooks", payload)
        return executeRequest(context, request)
    }

    //각 태그에 따라 유저의 좋아요 한 단어, 틀린 단어, 리뷰할 단어 get
    suspend fun linkWordUser(context: Context, uid: Int, word_ids: List<Int>, status: String): ApiResponse<SimpleMessagePayload>{
        // status : liked | wrong | review
        // liked : 좋아요 한 단어
        // wrong : 틀린 단어
        // review : 리뷰할 단어

        val payload = LinkUserWordRequestPayload(uid, word_ids, status)
        val request = ClientRequest("LinkUserWord", payload)
        return executeRequest(context, request)
    }

    suspend fun unlinkWordUser(context: Context, uid: Int, word_ids: List<Int>, status: String): ApiResponse<SimpleMessagePayload>{
        // status : liked | wrong | review
        // liked : 좋아요 한 단어
        // wrong : 틀린 단어
        // review : 리뷰할 단어

        // LinkUserWordRequestPayload 재사용
        val payload = LinkUserWordRequestPayload(uid, word_ids, status)
        val request = ClientRequest("UnlinkUserWord", payload)
        return executeRequest(context, request)
    }

    suspend fun getLinkedWordOfUser(context: Context, uid: Int, status: String): ApiResponse<GetLinkedWordOfUserResponsePayload>{
        // status : liked | wrong | review
        // liked : 좋아요 한 단어
        // wrong : 틀린 단어
        // review : 리뷰할 단어

        val payload = GetLinkedWordOfUserRequestPayload(uid, status)
        val request = ClientRequest("GetLinkedWordOfUser", payload)
        return executeRequest(context, request)
    }




    /**유저 ID 검색**/
    suspend fun searchUserByUid(context: Context, uid: Int): ApiResponse<SearchUserResponsePayload> {
        val payload = FriendListRequestPayload(uid)
        val request = ClientRequest("SearchUserByUid", payload)
        return executeRequest(context, request)
    }

    //11.11일자 추가
    //랜덤으로 단어 가져오기
    suspend fun getRandomSubscribedWord(context: Context, uid: Int): ApiResponse<GetWordbookResponsePayload>{
        // GetWordbookResponsePayload 재사용
        // FriendListRequestPayload 재사용

        val payload = FriendListRequestPayload(uid)
        val request = ClientRequest("GetRandomSubscribedWord", payload)
        return executeRequest(context, request)
    }

    //단어장 ID로 검색
    suspend fun getWordbookInfoWithID(context: Context, wid: Int): ApiResponse<GetWordbookInfoWithIDResponsePayload>{
        // GetWordbookRequestPayload 재사용
        val payload = GetWordbookRequestPayload(wid)
        val request = ClientRequest("GetWordbookInfoWithID", payload)
        return executeRequest(context, request)
    }


    //11.25일자 추가
    suspend fun startSession(context: Context, uid: Int, name: String): ApiResponse<SessionStartResponsePayload>{
        val payload = SessionStartRequestPayload(uid, name)
        val request = ClientRequest("SessionStart", payload)
        return executeRequest(context, request)
    }
    suspend fun chatInput(context: Context, uid: Int, sessionId: String, message: String): ApiResponse<AIResponseDataPayload>{
        val payload = ChatInputRequestPayload(uid, sessionId, message)
        val request = ClientRequest("ChatInput", payload)
        return executeRequest(context, request)
    }
    suspend fun quizSubmit(context: Context, uid: Int, wordId: Int, wordText: String, question: String, userAnswer: String, correctAnswer: String): ApiResponse<SimpleMessagePayload>{
        val payload = QuizSubmitRequestPayload(uid, wordId, wordText, question, userAnswer, correctAnswer)
        val request = ClientRequest("QuizSubmit", payload)
        return executeRequest(context, request)
    }
    suspend fun analyzeLearning(context: Context, uid: Int, sessionId: String): ApiResponse<AIResponseDataPayload>{
        val payload = AnalyzeLearningRequestPayload(uid, sessionId)
        val request = ClientRequest("AnalyzeLearning", payload)
        return executeRequest(context, request)
    }
    suspend fun todayReview(context: Context, uid: Int, sessionId: String): ApiResponse<AIResponseDataPayload>{
        // AnalyzeLearningRequestPayload 재사용
        val payload = AnalyzeLearningRequestPayload(uid, sessionId)
        val request = ClientRequest("TodayReview", payload)
        return executeRequest(context, request)
    }
    suspend fun businessTalk(context: Context, uid: Int, sessionId: String, text: String): ApiResponse<BusinessTalkResponsePayload>{
        val payload = BusinessTalkReqeustPayload(uid, sessionId, text)
        val request = ClientRequest("BusinessTalk", payload)
        return executeRequest(context, request)
    }
    suspend fun generateExample(context: Context, uid: Int, sessionId: String): ApiResponse<AIResponseDataPayload>{
        // AnalyzeLearningRequestPayload 재사용
        val payload = AnalyzeLearningRequestPayload(uid, sessionId)
        val request = ClientRequest("GenerateExample", payload)
        return executeRequest(context, request)
    }

    suspend fun SendBackSTT(context: Context, fileBytes: ByteArray, fileName: String): ApiResponse<SimpleMessagePayload> {
        val payload = SendBackRequestPayload(fileName, fileBytes.size.toLong())
        val request = ClientRequest("SendBack", payload)
        return executeRequest(context, request, fileBytes)
    }


}