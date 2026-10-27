package com.epam.gymcrm.config;

import com.epam.gymcrm.integration.reports.ReportsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ReportsProperties.class)
@ConditionalOnProperty(name = "gym.integrations.reports.enabled", havingValue = "true")
public class ReportsClientConfig {

    @Bean
    public RestClient reportsRestClient(
            RestClient.Builder builder,
            ReportsProperties properties) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        return builder
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }
}
