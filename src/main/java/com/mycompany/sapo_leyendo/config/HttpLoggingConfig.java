package com.mycompany.sapo_leyendo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Configuration
public class HttpLoggingConfig {

    private static final Logger LOG = LoggerFactory.getLogger("HTTP_LOG");

    @Value("${app.http.logging.enabled:true}")
    private boolean httpLoggingEnabled;

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> httpLoggingFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                if (!httpLoggingEnabled) {
                    filterChain.doFilter(request, response);
                    return;
                }

                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 64 * 1024);
                ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
                long start = System.currentTimeMillis();
                try {
                    filterChain.doFilter(wrappedRequest, wrappedResponse);
                } finally {
                    long durationMs = System.currentTimeMillis() - start;
                    String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
                    String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
                    Map<String, String> requestHeaders = Collections.list(wrappedRequest.getHeaderNames())
                            .stream()
                            .collect(Collectors.toMap(h -> h, wrappedRequest::getHeader, (v1, v2) -> v1));
                    Map<String, String> responseHeaders = wrappedResponse.getHeaderNames()
                            .stream()
                            .collect(Collectors.toMap(h -> h, wrappedResponse::getHeader, (v1, v2) -> v1));
                    Principal principal = wrappedRequest.getUserPrincipal();
                    LOG.info("ts={} user={} method={} path={} query={} status={} durationMs={} reqHeaders={} reqBody={} resHeaders={} resBody={}",
                            Instant.now(),
                            principal != null ? principal.getName() : "anon",
                            wrappedRequest.getMethod(),
                            wrappedRequest.getRequestURI(),
                            wrappedRequest.getQueryString(),
                            wrappedResponse.getStatus(),
                            durationMs,
                            requestHeaders,
                            requestBody,
                            responseHeaders,
                            responseBody);
                    wrappedResponse.copyBodyToResponse();
                }
            }
        });
        registration.setOrder(1);
        return registration;
    }
}
