package com.amazon.awsconsoleaccessibility.test;

import com.amazon.awsconsoleaccessibility.AWSConsoleAccessibilityClient;
import com.amazon.coral.client.ClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.Before;

/**
 *
 * Base class for test cases, loads the unit testing spring configuration context
 * and does some other setup before execution, then cleans up afterwards. It also
 * contains a few helper methods for creating unit tests.
 */
public abstract class AbstractTestCase {

    // Default logger
    protected Logger logger = null;

    // Coral's client builder
    private ClientBuilder builder;

    // Coral Mock client for testing activities through the server paths
    protected AWSConsoleAccessibilityClient client = null;

    /**
     * Runs before the tests to initialize the test context.
     *
     * We create the logger and initialize the client in this method, if they have
     * not already been initialized.
     *
     * @throws Throwable
     */
    @Before
    public void setUp() throws Throwable {
        // Setup the root if it isn't defined
        if (System.getProperty("root") == null) {
            System.setProperty("root", ".");
        }

        // Set up the logger, if it isn't defined
        if (logger == null) {
            logger = LogManager.getLogger(getClass());
        }

        if (builder == null) {
            builder = new ClientBuilder();
        }

        if (client == null) {
            client = builder.inlineOf(AWSConsoleAccessibilityClient.class).newClient();
        }
    }
}