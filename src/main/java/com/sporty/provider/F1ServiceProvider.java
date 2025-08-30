package com.sporty.provider;

import org.springframework.stereotype.Service;

@Service
public class F1ServiceProvider {
    private final java.util.Map<String, F1Provider> providers;

    // Spring injects: beanName -> bean
    public F1ServiceProvider(java.util.Map<String, F1Provider> providers) {
        // normalize keys if you want case-insensitive lookups
        this.providers = new java.util.HashMap<>();
        providers.forEach((name, bean) -> this.providers.put(name.toUpperCase(), bean));
    }

    public F1Provider getF1ServiceProvider(String providerCode) {
        if (providerCode == null || providerCode.isEmpty()) {
            throw new IllegalArgumentException("No service provider code found");
        }
        F1Provider provider = providers.get(providerCode.toUpperCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported provider: " + providerCode);
        }
        return provider; // container-managed bean (singleton by default)
    }
}
