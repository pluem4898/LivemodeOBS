package camsetobs.alpha;

import net.minecraft.util.math.Vec3d;

public class CameraPosition {
    public Vec3d position;
    public float yaw, pitch;
    public String name;

    public CameraPosition(String name, Vec3d position, float yaw, float pitch) {
        this.name = name;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
