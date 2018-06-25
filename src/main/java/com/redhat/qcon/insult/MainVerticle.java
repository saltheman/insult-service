package com.redhat.qcon.insult;

import io.reactivex.Maybe;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {


    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    Maybe<JsonObject> initConfigRetriever() {
        // Load the default configuration from the classpath
        LOG.info("Configuration store loading.");
        ConfigStoreOptions defaultOpts = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "insult_default_config.json"));

        // Load container specific configuration from a specific file path inside of the
        // container
        ConfigStoreOptions localConfig = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "/opt/docker_config.json"))
            .setOptional(true);

        // When running inside of Kubernetes, configure the application to also load
        // from a ConfigMap. This config is ONLY loaded when running inside of
        // Kubernetes or OpenShift
        ConfigStoreOptions confOpts = new ConfigStoreOptions()
            .setType("configmap")
            .setConfig(new JsonObject()
                .put("name", "insult-config")
                .put("optional", true)
            );

        // Add the default and container config options into the ConfigRetriever
        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions()
            .addStore(defaultOpts)
            .addStore(confOpts);

        // Create the ConfigRetriever and return the Maybe when complete
        return ConfigRetriever.create(vertx, retrieverOptions).rxGetConfig().toMaybe();
    }

    @Override
    public void start(Future<Void> startFuture) {

        initConfigRetriever()                                                   // (7)
            .doOnError(startFuture::fail)                                   // (8)
            .subscribe(c -> {
                LOG.info(c.encodePrettily());
                context.config().mergeIn(c);                                // (9)
                startFuture.complete();                                     // (10)
            });
    }
}
