package com.ilanacpa.backend.document;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supabase")
public record SupabaseStorageProperties(String url, String serviceRoleKey, String storageBucket) {
}
