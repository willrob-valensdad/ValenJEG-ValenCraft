package net.neoforged.fml.event.lifecycle;
public final class FMLClientSetupEvent {
    public void enqueueWork(Runnable work) { work.run(); }
}
