package MagicUtils.magicutils.client.event;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.ChestDataStorage;
import MagicUtils.magicutils.client.ui.custom.screen.ItemScreen;
import MagicUtils.magicutils.client.utils.EnvironmentDetector;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.item.Items;

import java.util.concurrent.CompletableFuture;

public class InteractionEvents {
    public static void register() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            EnvironmentDetector.detect();  // Detect when world loads (singleplayer or multiplayer)
            MagicUtilsClient.STACK_KEY_PROVIDER = EnvironmentDetector.getActiveProvider();
            CompletableFuture.runAsync(ChestDataStorage::loadChestData);
        });

    }
}
