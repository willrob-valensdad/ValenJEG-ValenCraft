package com.zurrtum.create.content.trains.graph;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import java.nio.file.Files;
import java.nio.file.Path;

/** Separate steady-state fixture; original ordered-startup results remain evidence. */
public final class RailSteadyFixture implements ModInitializer {
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if(Boolean.getBoolean("valen.rail.fixture"))server.execute(()->{
                try {
                    RailFixture.check(server.isSameThread(),"not owner");
                    RailFixture.check(Files.isRegularFile(Path.of(".valen-disposable-benchmark")),"not disposable");
                    // Fresh graph and curve objects on every warmup, same complete call for
                    // original and candidate. This warms generated code, never test caches.
                    for(int i=0;i<40;i++)RailFixture.trial(server,new RailFixture.Shape("global_warmup",32,0,false));
                    RailFixture.run(server);
                } catch(Throwable failure) {
                    failure.printStackTrace();server.halt(false);
                }
            });
        });
    }
}
