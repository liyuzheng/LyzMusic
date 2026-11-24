package yz.li.core_network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import yz.li.core_tools.exts.debug
import yz.li.core_tools.exts.release
import java.util.concurrent.TimeUnit

/**
 * desc:
 * createed by liyuzheng on 2023/8/24 19:48
 */
class RetrofitClient : IRetrofitClient {
    companion object {
        private const val BASE_URL = "https://audius.co"
    }

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HttpLoggingInterceptor().apply {
                debug {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                release {
                    level = HttpLoggingInterceptor.Level.NONE
                }
            }).build()
    }

    override fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .client(provideOkHttpClient())
            .baseUrl(BASE_URL)
            .addConverterFactory(StringConverterFactory())
            .build()
    }
}