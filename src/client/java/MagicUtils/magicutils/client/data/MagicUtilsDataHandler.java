package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MagicUtilsDataHandler {
    private static final String FOLDER_NAME = "magicutils_data";
    private static final Path DATA_FOLDER = FabricLoader.getInstance().getGameDir().resolve(FOLDER_NAME);

    public static void init() {
        try {
            Files.createDirectories(DATA_FOLDER);
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Error creating data folder", e);
        }
    }

    public static Path getCurrentContextSaveDir() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer()) {
            assert client.getServer() != null;
            String levelName = client.getServer().getSaveProperties().getLevelName();
            return DATA_FOLDER.resolve("singleplayer").resolve(levelName);
        } else if (client.getCurrentServerEntry() != null) {
            String address = client.getCurrentServerEntry().address;
            String safeName = sanitizeServerAddress(address);
            return DATA_FOLDER.resolve("multiplayer").resolve(safeName);
        }
        return DATA_FOLDER.resolve("unknown");
    }

    private static String sanitizeServerAddress(String address) {
        return address.replace(":", "_");
    }

}
