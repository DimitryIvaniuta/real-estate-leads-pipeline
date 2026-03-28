package com.github.dimitryivaniuta.gateway.realestate.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

/**
 * Common infrastructure beans.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class ApplicationConfiguration {

    /**
     * Enables safe support for forwarded headers when the service runs behind a trusted proxy.
     *
     * @return transformer bean
     */
    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        return new ForwardedHeaderTransformer();
    }
}
