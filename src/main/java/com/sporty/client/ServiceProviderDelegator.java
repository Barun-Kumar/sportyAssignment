package com.sporty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.Map;
import java.util.Objects;

/**
 * Delegates F1 operations to a provider-specific {@link F1Client}.
 * <p>
 * Strategy resolution is based on Spring bean names. For example, a bean declared as
 * {@code @Component("OPENF1")} will be returned for {@link ServiceProviderENUM#OPENF1}.
 * </p>
 *
 * <h3>How to add a new provider</h3>
 * <ol>
 *   <li>Create a new {@code F1Client} implementation and annotate it with {@code @Component("<ENUM_NAME>")}.</li>
 *   <li>Add the enum constant to {@link ServiceProviderENUM}.</li>
 *   <li>Done – this delegator will auto-discover it.</li>
 * </ol>
 */
@Component
public class ServiceProviderDelegator {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderDelegator.class);

    private final Map<String, F1Client> registry;
    private final F1Client defaultClient;

    /**
     * @param clients       all {@link F1Client} beans keyed by their Spring bean name.
     * @param defaultClient default client to use when provider is {@code null}.
     *                      Qualifier must match an existing bean name (e.g., "OPENF1").
     */
    public ServiceProviderDelegator(Map<String, F1Client> clients,
                                    @Qualifier("OPENF1") F1Client defaultClient) {
        this.registry = Map.copyOf(clients);
        this.defaultClient = Objects.requireNonNull(defaultClient, "defaultClient");
    }

    /**
     * Returns the {@link F1Client} for the given provider.
     *
     * @param provider the desired provider enum; if {@code null}, the default client is returned.
     * @return matching {@link F1Client}
     * @throws IllegalArgumentException if no client is registered for the given provider.
     */
    public F1Client get(ServiceProviderENUM provider) {
        logger.info("Getting the client for provider : {}",provider.name());
        if (provider == null) {
            logger.info("No Provider name, returning the default provide");
            return defaultClient;
        }
        F1Client client = registry.get(provider.name());
        if (client == null) {
            throw new IllegalArgumentException("Unsupported provider: " + provider +
                    ". Available: " + registry.keySet());
        }
        return client;
    }
}

