package com.amazon.awsconsoleaccessibility.health;

import com.amazon.coral.service.HealthCheckStrategy;

public class DeepHealthCheck implements HealthCheckStrategy {

    private final HealthCheckStrategy delegate;

    public DeepHealthCheck(HealthCheckStrategy delegate) {
        this.delegate = delegate;
    }

    /**
     * Performs a deep health check on your service.
     *
     * You should improve this method with some simple but meaningful health
     * check. This will be invoked at startup during SanityTest to make
     * sure you have got everything configured properly before adding this
     * host to the VIP.
     *
     * The default delegate validates that activities can be instantiated.
     *
     * @return true if the service is healthy
     */
    public boolean isHealthy() {
       // Helpful checks to include here:
       // - Checks that all remote services and stores
       // - Checks that involve config unique to each stage
        return delegate.isHealthy();
    }

}

