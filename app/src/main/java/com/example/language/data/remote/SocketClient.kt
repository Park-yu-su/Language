package com.example.language.data.remote

import android.util.Log
import com.example.language.BuildConfig
import com.example.language.data.remote.model.ClientRequest
import com.example.language.data.remote.model.GenericServerResponse
import com.example.language.data.remote.model.SimpleMessagePayload
import com.example.language.data.remote.model.SocketResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import javax.inject.Inject
import javax.net.ssl.SSLSocketFactory

class SocketClient @Inject constructor(
    private val sslSocketFactory: SSLSocketFactory,
    @PublishedApi
    internal val json: Json,
) {
    companion object {
        private const val TAG = "SocketClient"
        private val SERVER_IP = BuildConfig.BASE_URL
        private val PORT = BuildConfig.SERVER_PORT
    }

    /**
     * 외부에서 호출하는 인라인 함수
     */
    suspend inline fun <reified Req, reified Res> executeRequest(
        request: ClientRequest<Req>,
        fileBytes: ByteArray? = null,
    ): SocketResult<Res> {
        // 요청 데이터를 JSON 문자열로 변환
        val finalJsonString = json.encodeToString(request)

        // 실제 소켓 통신 수행 및 통신 결과에 따른 처리
        return when (val responseResult = performCommunication(finalJsonString, fileBytes)) {
            is CommunicationRawResult.Success -> {
                try {
                    // 응답 본문을 Res 타입으로 역직렬화
                    val genericResponse = json.decodeFromString<GenericServerResponse>(responseResult.json)
                    parseGenericResponse<Res>(genericResponse)
                } catch (e: Exception) {
                    SocketResult.Error("PARSE_ERROR", "응답 데이터 해석에 실패했습니다.")
                }
            }
            is CommunicationRawResult.Error -> {
                SocketResult.Error(responseResult.code, responseResult.message)
            }
        }
    }

    /**
     * 실제 소켓 통신을 담당하는 내부 함수.
     */
    @PublishedApi
    internal suspend fun performCommunication(
        jsonString: String,
        fileBytes: ByteArray?,
    ): CommunicationRawResult = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            Timber.d("Connecting to $SERVER_IP:$PORT...")
            socket = sslSocketFactory.createSocket(SERVER_IP, PORT)

            val outputStream = DataOutputStream(socket.getOutputStream())
            val inputStream = DataInputStream(socket.getInputStream())

            val requestBytes = jsonString.toByteArray(Charsets.UTF_8)
            Timber.d("Send JSON: $jsonString")

            // 길이 헤더 전송 (4 byte)
            outputStream.writeInt(requestBytes.size)
            outputStream.write(requestBytes)
            outputStream.flush()

            // 파일 전송 (있을 경우)
            fileBytes?.let {
                outputStream.write(it)
                outputStream.flush()
            }

            // 응답 수신
            val responseLength = inputStream.readInt()
            if (responseLength <= 0) return@withContext CommunicationRawResult.Error("PROTOCOL_ERROR", "서버 응답이 없습니다.")

            val responseBytes = ByteArray(responseLength)
            inputStream.readFully(responseBytes)

            CommunicationRawResult.Success(String(responseBytes, Charsets.UTF_8))

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Socket Error")
            CommunicationRawResult.Error("NETWORK_ERROR", e.message ?: "네트워크 오류")
        } finally {
            socket?.close()
        }
    }

    /**
     * GenericServerResponse를 분석하여 최종 SocketResult를 반환합니다.
     */
    @PublishedApi
    internal inline fun <reified Res> parseGenericResponse(
        genericResponse: GenericServerResponse,
    ): SocketResult<Res> {
        return when (genericResponse.status) {
            "ACCEPT" -> {
                val successData = json.decodeFromJsonElement<Res>(genericResponse.payload)
                SocketResult.Success(successData)
            }
            "REJECT", "ERROR" -> {
                val errorPayload = try {
                    json.decodeFromJsonElement<SimpleMessagePayload>(genericResponse.payload)
                } catch (e: Exception) {
                    SimpleMessagePayload("에러 메시지 파싱 실패")
                }
                SocketResult.Error(genericResponse.code ?: "SERVER_ERROR", errorPayload.message)
            }
            else -> SocketResult.Error("UNKNOWN_STATUS", "알 수 없는 상태")
        }
    }

    // 내부 통신 결과 전달을 위한 헬퍼 클래스
    @PublishedApi
    internal sealed class CommunicationRawResult {
        data class Success(val json: String) : CommunicationRawResult()
        data class Error(val code: String, val message: String) : CommunicationRawResult()
    }
}