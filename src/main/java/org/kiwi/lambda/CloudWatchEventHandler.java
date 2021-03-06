package org.kiwi.lambda;

import com.google.inject.Injector;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.kiwi.FloatingAverageLambdaModule;

import static com.google.inject.Guice.createInjector;

public class CloudWatchEventHandler implements RequestHandler<Object, Void> {

    private static final Injector INJECTOR = createInjector(new FloatingAverageLambdaModule());

    private final FloatingAverageJob floatingAverageJob = INJECTOR.getInstance(FloatingAverageJob.class);

    @Override
    public Void handleRequest(Object input, Context context) {
        floatingAverageJob.run();
        return null;
    }
}
