package camsetobs.alpha;

import net.fabricmc.api.ModInitializer;

public class CameraMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CameraManager.registerCommands();
    }
}
