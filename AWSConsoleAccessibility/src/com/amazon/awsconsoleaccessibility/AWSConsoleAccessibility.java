package com.amazon.awsconsoleaccessibility;

import static com.google.inject.Guice.createInjector;
import static java.lang.Runtime.getRuntime;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amazon.platform.config.AppConfig;

import com.amazon.coral.bobcat.BobcatServer;
import com.google.inject.Injector;

public class AWSConsoleAccessibility {

    private static final Logger log = LoggerFactory.getLogger(AWSConsoleAccessibility.class);

    private static final String APP_NAME = "AWSConsoleAccessibility";

    private static final int TEN_SECONDS = (int)TimeUnit.SECONDS.toMillis(10);

    public static void main(String[] args) throws Throwable {
        log.info("Starting with args {}", Arrays.toString(args));
        initAppConfig(args);

        String root = System.getProperty("root");
        String realm = AppConfig.getRealm().name();
        String domain = AppConfig.getDomain();

        Injector injector = createInjector(
            new CoralModule(root, domain, realm),
            new AWSConsoleAccessibilityModule()
        );
        final BobcatServer server = injector.getInstance(BobcatServer.class);
        server.acquire();
        server.activate();

        getRuntime().addShutdownHook(new Thread(() -> stopServer(server)));

        // wait for termination
        Thread.currentThread().join();
    }

    private static synchronized void stopServer(BobcatServer server) {
        try {
            server.deactivate();
            server.release();
        } catch (Exception e) {
            log.warn("Exception while stopping BobcatServer: ", e);
        }
    }

    private static void initAppConfig(String[] args) throws InterruptedException {
        verifyArguments(args);

        AppConfig.initialize(APP_NAME, null, args);
    }

    private static void verifyArguments(String[] args) throws InterruptedException {
        boolean hasRealm = false;
        boolean hasDomain = false;
        boolean hasRoot = false;

        for (String arg : args) {
            if (arg.startsWith("--realm=")) {
                hasRealm = true;
            } else if (arg.startsWith("--domain=")) {
                hasDomain = true;
            } else if (arg.startsWith("--root=")) {
                hasRoot = true;
            }
        }

        if (hasRealm && hasDomain && hasRoot) {
            return;
        } else {
            System.out.println("The service cannot determine what environment it is running in and will shut down.");
            System.out.println("If you are trying to run from an Eclipse workspace, add the following");
            System.out.println("program arguments to your launch configuration: ");
            System.out.println("");
            System.out.println("--domain=test --realm=us-west-2 --root=build/private");
            Thread.sleep(TEN_SECONDS); // Wait a while to avoid flapping at full speed (in case this happens in Apollo)
            System.exit(0);
        }
    }
}
