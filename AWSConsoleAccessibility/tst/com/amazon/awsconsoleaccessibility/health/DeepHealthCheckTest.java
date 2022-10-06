package com.amazon.awsconsoleaccessibility.health;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeepHealthCheckTest {

    @Test
    public void healthyDelegate_ShouldSucceedHealthCheck() {
        DeepHealthCheck healthCheck = new DeepHealthCheck(() -> true);
        assertTrue(healthCheck.isHealthy());
    }

    @Test
    public void faultyDelegate_ShouldFailHealthCheck() {
        DeepHealthCheck healthCheck = new DeepHealthCheck(() -> false);
        assertFalse(healthCheck.isHealthy());
    }

}
