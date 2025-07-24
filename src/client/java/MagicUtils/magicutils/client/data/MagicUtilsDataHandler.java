package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

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
            String levelName = client.getServer().getSaveProperties().getLevelName(); // example: "New World"
            return DATA_FOLDER.resolve("singleplayer").resolve(levelName);
        } else if (client.getCurrentServerEntry() != null) {
            String address = client.getCurrentServerEntry().address; // e.g., play.example.net
            String encoded = hashAddress(address);
            return DATA_FOLDER.resolve("multiplayer").resolve(encoded);
        }
        return DATA_FOLDER.resolve("unknown");
    }

    private static String hashAddress(String address) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(address.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            MagicUtilsClient.LOGGER.error("Error hashing address: {}", address, e);
            return "unknown_server";
        }
    }
}