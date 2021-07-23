package com.example.e2e4_test_work.di

import com.example.e2e4_test_work.api.DummyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)

object ApiModule {

    @Provides
    fun provideDummyApi(): DummyApi = DummyApi()
}