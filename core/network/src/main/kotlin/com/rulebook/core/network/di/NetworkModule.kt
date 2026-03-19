package com.rulebook.core.network.di

import com.rulebook.core.network.BuildConfig
import com.rulebook.core.network.RulebookApiClient
import com.rulebook.core.network.api.ReceiptValidationApi
import com.rulebook.core.network.api.RulebookApi
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single<OkHttpClient> {
        RulebookApiClient.createOkHttpClient(isDebug = BuildConfig.DEBUG)
    }

    single<Retrofit> {
        RulebookApiClient.createRetrofit(
            okHttpClient = get(),
            baseUrl = BuildConfig.BASE_URL,
        )
    }

    single<RulebookApi> {
        RulebookApiClient.createRulebookApi(retrofit = get())
    }

    single<ReceiptValidationApi> {
        RulebookApiClient.createReceiptValidationApi(retrofit = get())
    }
}
