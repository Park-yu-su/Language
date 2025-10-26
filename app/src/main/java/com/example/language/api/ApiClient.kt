package com.example.language.api

import android.content.Context
import android.util.Log
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
    private const val SERVER_IP = "58.76.177.184"
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

    suspend fun authenticate(context: Context, email: String, nickname: String): ApiResponse<AuthResponsePayload> {
        val payload = AuthRequestPayload(email, nickname, image = "null")
        val request = ClientRequest("Authentication", payload)
        return executeRequest(context, request)
    }

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


    /**유저 ID 검색**/
    suspend fun searchUserByUid(context: Context, uid: Int): ApiResponse<SearchUserResponsePayload> {
        val payload = FriendListRequestPayload(uid)
        val request = ClientRequest("SearchUserByUid", payload)
        return executeRequest(context, request)
    }


}