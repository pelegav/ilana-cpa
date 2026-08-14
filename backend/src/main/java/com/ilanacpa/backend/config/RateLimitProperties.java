package com.ilanacpa.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        int loginPerIpPerMinute,
        int loginPerAccountPer15Min,
        int refreshPerIpPerMinute) {
}
