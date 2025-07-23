package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MagicUtilsDataHandler {
    private static final String FOLDER_NAME = "magicutils_data"; // change this to your mod's ID or name
    private static final Path DATA_FOLDER = FabricLoader.getInstance().getGameDir().resolve(FOLDER_NAME);

    public static void init() {
        try {
            if (!Files.exists(DATA_FOLDER)) {
                Files.createDirectories(DATA_FOLDER);
            }

            // Example: Create a file inside your folder
            Path dataFile = DATA_FOLDER.resolve("example_data.txt");
            if (!Files.exists(dataFile)) {
                Files.writeString(dataFile, "This is your first saved data!");
            }

        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Error handling mod data! printing error:\n{}",(Object) e.getStackTrace());
        }
    }

    public static Path getDataFolder() {
        return DATA_FOLDER;
    }
}