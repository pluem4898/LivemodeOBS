package camsetobs.alpha;

import net.fabricmc.api.ClientModInitializer;

public class CameraModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("CameraModClient loaded for 1.21.4");
    }
}
