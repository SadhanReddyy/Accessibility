package com.amazon.awsconsoleaccessibility;

import javax.inject.Named;

import com.amazon.guice.brazil.AppConfigBinder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Configures application-specific dependencies for injection.
 *
 */
public class AWSConsoleAccessibilityModule extends AbstractModule {

    @Override
    protected void configure() {
        AppConfigBinder appConfigBinder = new AppConfigBinder(binder());
        appConfigBinder.bindPrefix("*");
    }
}
