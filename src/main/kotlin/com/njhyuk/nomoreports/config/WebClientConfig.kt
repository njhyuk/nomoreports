package com.njhyuk.nomoreports.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000) // 60초 연결 타임아웃
            .responseTimeout(Duration.ofMinutes(10)) // 10분 응답 타임아웃
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(600)) // 10분 읽기 타임아웃
                conn.addHandlerLast(WriteTimeoutHandler(600)) // 10분 쓰기 타임아웃
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(1024 * 1024) // 1MB
            }
            .build()
    }
} 