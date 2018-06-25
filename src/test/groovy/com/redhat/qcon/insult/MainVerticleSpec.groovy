package com.redhat.qcon.insult

import io.vertx.core.Future
import io.vertx.core.Vertx
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

class MainVerticleSpec extends Specification {

    def 'Test Vert.x configuration loading'() {
        given: 'An instance of Vert.x'                      // (1)
        def vertx = Vertx.vertx()
        and: 'An instance of a Vert.x Future'               // (2)
        def fut = Future.future()
        and: '''An instance of Spock's AsyncConditions'''
        def async = new AsyncConditions(1)              // (4)

        when: 'We attempt to deploy the main Verticle'      // (3)
        vertx.deployVerticle(new MainVerticle(), fut.completer())

        then: 'Expect that the correct configuration is found and loaded'
        fut.setHandler({ res ->
            async.evaluate {                            // (5)
                def config = vertx.getOrCreateContext().config()
                assert res.succeeded()                  // (6)
                assert config.hasProperty('insult')     // (7)
                assert config.hasProperty('adjective')  // (8)
                assert config.hasProperty('http')       // (9)
            }
        })

        cleanup: 'Await the async operations'               // (10)
        async.await(3600)
        vertx.close()
    }
}