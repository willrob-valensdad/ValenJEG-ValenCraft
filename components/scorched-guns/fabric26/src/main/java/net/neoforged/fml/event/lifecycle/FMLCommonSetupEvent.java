package net.neoforged.fml.event.lifecycle;
public final class FMLCommonSetupEvent {
    public void enqueueWork(Runnable work) { work.run(); }
}
