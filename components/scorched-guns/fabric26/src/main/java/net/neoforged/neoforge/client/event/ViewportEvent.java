package net.neoforged.neoforge.client.event;
import net.neoforged.bus.api.Event;
public class ViewportEvent extends Event {
    public static class Pre extends ViewportEvent {}
    public static class Post extends ViewportEvent {}
    public static final class ComputeFov extends ViewportEvent {
        private float fov;
        private final float partialTick;
        public ComputeFov(float fov, float partialTick) { this.fov = fov; this.partialTick = partialTick; }
        public float getFOV() { return fov; }
        public void setFOV(float fov) { this.fov = fov; }
        public float getPartialTick() { return partialTick; }
    }
    public static final class ComputeCameraAngles extends ViewportEvent {
        private final float partialTick;
        private float yaw;
        private float pitch;
        private float roll;
        public ComputeCameraAngles(float partialTick, float yaw, float pitch, float roll) {
            this.partialTick = partialTick; this.yaw = yaw; this.pitch = pitch; this.roll = roll;
        }
        public float getPartialTick() { return partialTick; }
        public float getYaw() { return yaw; }
        public float getPitch() { return pitch; }
        public float getRoll() { return roll; }
        public void setYaw(float yaw) { this.yaw = yaw; }
        public void setPitch(float pitch) { this.pitch = pitch; }
        public void setRoll(float roll) { this.roll = roll; }
    }
}
