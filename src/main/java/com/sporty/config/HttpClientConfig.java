package com.sporty.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    RestClient openF1RestClient(RestClient.Builder builder) {
        // OpenF1 base is https://api.openf1.org, the sessions path is /v1/sessions
        return builder
                .baseUrl("https://api.openf1.org/v1")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    // Optional: set connect/read timeouts
    @Bean
    RestClient.Builder restClientBuilderWithTimeouts() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return RestClient.builder().requestFactory(factory);
    }
}
