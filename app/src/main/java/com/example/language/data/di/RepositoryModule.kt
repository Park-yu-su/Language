package com.example.language.data.di

import com.example.language.data.repository.*
import com.example.language.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // AuthRepository 연결
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    // FriendRepository 연결
    @Binds
    @Singleton
    abstract fun bindFriendRepository(
        impl: FriendRepositoryImpl
    ): FriendRepository

    // WordRepository 연결
    @Binds
    @Singleton
    abstract fun bindWordRepository(
        impl: WordRepositoryImpl
    ): WordRepository

    // AiRepository 연결
    @Binds
    @Singleton
    abstract fun bindAiRepository(
        impl: AiRepositoryImpl
    ): AiRepository
}