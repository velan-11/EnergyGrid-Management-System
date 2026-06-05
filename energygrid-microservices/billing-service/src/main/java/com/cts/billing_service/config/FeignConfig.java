package com.cts.billing_service.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation
        .Bean;
import org.springframework.context.annotation
        .Configuration;
import org.springframework.web.context.request
        .RequestContextHolder;
import org.springframework.web.context.request
        .ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor
    requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder
                                    .getRequestAttributes();

            if (attributes != null) {
                // Propagate gateway-injected identity headers so that
                // service-to-service Feign calls (which bypass the gateway)
                // are still authenticated by the downstream HeaderAuthFilter.
                String email = attributes
                        .getRequest()
                        .getHeader("X-Auth-Email");
                String role = attributes
                        .getRequest()
                        .getHeader("X-Auth-Role");
                if (email != null) {
                    requestTemplate.header("X-Auth-Email", email);
                }
                if (role != null) {
                    requestTemplate.header("X-Auth-Role", role);
                }

                String token = attributes
                        .getRequest()
                        .getHeader("Authorization");
                if (token != null) {
                    requestTemplate.header(
                            "Authorization", token);
                    System.out.println(
                            " JWT forwarded to: "
                                    + requestTemplate.url());
                }
            }
        };
    }
}