package com.example.language.data.di

import android.content.Context
import com.example.language.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // JSON 설정 공통화
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true
        }
    }

    // SSLSocketFactory 생성 (앱 실행 시 1회만 수행됨)
    @Provides
    @Singleton
    fun provideSSLSocketFactory(@ApplicationContext context: Context): SSLSocketFactory {
        return try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val inputStream: InputStream = context.resources.openRawResource(R.raw.server)
            val certificate = inputStream.use {
                certificateFactory.generateCertificate(it) as X509Certificate
            }

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("server", certificate)

            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.trustManagers, null)

            sslContext.socketFactory
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("SSL 인증서 로딩 실패: ${e.message}")
        }
    }
}