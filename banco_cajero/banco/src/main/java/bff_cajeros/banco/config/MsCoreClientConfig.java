package bff_cajeros.banco.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MsCoreClientConfig {

    @Value("${core.base-url}")
    private String baseUrl;

    @SuppressWarnings("null")
    @Bean
    public RestClient coreRestClient() {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}