package easton.sharedechest;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class SharedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(SharedEChest.SHARED_E_HANDLER, SharedEChestScreen::new);
    }
}
