package MagicUtils.magicutils.client;

import MagicUtils.magicutils.client.commands.ModCommands;
import MagicUtils.magicutils.client.config.MagicUtilsConfig;
import MagicUtils.magicutils.client.data.MagicUtilsDataHandler;
import MagicUtils.magicutils.client.ui.custom.overlay.ChestOverlayRenderer;
import MagicUtils.magicutils.client.ui.custom.screen.ItemScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicUtilsClient implements ClientModInitializer {

    public static MagicUtilsConfig CONFIG = MagicUtilsConfig.get();
    public static final Logger LOGGER = LoggerFactory.getLogger(MagicUtilsClient.class);
    public static boolean ShouldOpenScreen = false;
    @Override
    public void onInitializeClient() {
        MagicUtilsConfig.load();
        MagicUtilsDataHandler.init();
        ChestOverlayRenderer.register();

        ClientCommandRegistrationCallback.EVENT.register(ModCommands::registerAll);
        LOGGER.info("MagicUtils loaded successfully.");


        ClientTickEvents.EndTick ScreenCheckTicker = client1 -> {
            if (!MagicUtilsClient.ShouldOpenScreen){ return;}
            client1.setScreen(new ItemScreen(Text.literal("Item Screen")));
        };
        MinecraftClient.getInstance().execute(() -> {
            ClientTickEvents.END_CLIENT_TICK.register(ScreenCheckTicker);
        });



    }
}
