package com.amazon.awsconsoleaccessibility.health;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import com.amazon.coral.service.HealthCheckStrategy;

/**
 * Shallow health check verifies that the http endpoint
 * is properly running. This is shallow for two reasons:
 * - It is called very frequently by VIP health checks
 * - If this was deep, and one of your dependencies failed, the
 *   VIP health check would remove all your hosts from the VIP.
 *
 */
public class ShallowHealthCheck implements HealthCheckStrategy {

    @Override
    public boolean isHealthy() {
        // Helpful things to put here:
        // - Checks that the filesystem is working how you expect
        // - Checks that any local caches are working
        // - Checks that Odin is working properly
        //
        // DO NOT: add checks that depend on a remote resource
        // (like a DB or other service), since if they fail,
        // this check will fail for every host, and all hosts in
        // your VIP will fail health check and be taken out of
        // service.

        try {
            checkFileSystem();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check file system.", e);
        }

        return true;
    }

    private static void checkFileSystem() throws Exception {
        // Test that we can create and write to a file...
        Path file = Files.createTempFile(null, null);
        byte[] data = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        Files.write(file, data);

        // Just for sanity's sake, make sure we can read our work
        byte[] result = Files.readAllBytes(file);
        if (!Arrays.equals(data, result)) {
            throw new RuntimeException(
                "Ping check failure: something went wrong writing to the filesystem"
            );
        }

        // Delete the file to prevent gradually consuming the disk.
        Files.deleteIfExists(file);
    }
}
