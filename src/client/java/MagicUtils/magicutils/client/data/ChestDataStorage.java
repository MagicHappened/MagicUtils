package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ChestDataStorage {
    private static final Gson GSON = new Gson();
    private static final String FILE_NAME = "chests.json";

    private static final Type LIST_TYPE = new TypeToken<List<BlockPos>>() {}.getType();

    public static void saveChests(List<BlockPos> positions) {
        Path dir = MagicUtilsDataHandler.getCurrentContextSaveDir();
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(FILE_NAME);
            String json = GSON.toJson(positions, LIST_TYPE);
            Files.writeString(file, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Failed to save chest data", e);
        }
    }

    public static List<BlockPos> loadChests() {
        Path file = MagicUtilsDataHandler.getCurrentContextSaveDir().resolve(FILE_NAME);
        if (!Files.exists(file)) return new ArrayList<>();
        try {
            String json = Files.readString(file);
            return GSON.fromJson(json, LIST_TYPE);
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Failed to load chest data", e);
            return new ArrayList<>();
        }
    }
}