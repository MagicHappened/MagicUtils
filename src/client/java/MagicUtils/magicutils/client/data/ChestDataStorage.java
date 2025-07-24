package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.forEach;

public class ChestDataStorage {
    private static final Map<String, NbtList> chestData = new HashMap<>();


    private static Path getDataFolder() {
        return MagicUtilsDataHandler.getCurrentContextSaveDir();
    }
    public static void addChestContents(List<BlockPos> positions, NbtList contents) {
        String key = getKeyFromPositions(positions);
        chestData.put(key, contents);
        saveChestData(key, contents);
    }

    private static void saveChestData(String key, NbtList contents) {
        try {
            Path dataFolder = getDataFolder();
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            Path chestFile = dataFolder.resolve(key + ".dat");
            NbtCompound root = new NbtCompound();
            root.put("Items", contents);

            try (var out = Files.newOutputStream(chestFile)) {
                NbtIo.writeCompressed(root, out);
            }
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Failed to save chest data for key {}: {}", key, e);
        }
    }

    public static Map<String, NbtList> loadAllChestData() {
        Path dataFolder = MagicUtilsDataHandler.getCurrentContextSaveDir();
        if (!Files.exists(dataFolder)) return Collections.emptyMap();

        Map<String, NbtList> loaded = new HashMap<>();
        try {
            Files.list(dataFolder)
                    .filter(p -> p.toString().endsWith(".dat"))
                    .forEach(p -> {
                        try (var in = Files.newInputStream(p)) {
                            NbtCompound root = NbtIo.readCompressed(in, NbtSizeTracker.ofUnlimitedBytes());
                            NbtList items = root.getList("Items").orElse(new NbtList());
                            String key = p.getFileName().toString().replace(".dat", "");
                            loaded.put(key, items);
                        } catch (IOException e) {
                            MagicUtilsClient.LOGGER.error("Failed to load chest data from file {}: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Failed to read chest data folder: {}", e);
        }

        return loaded;
    }

    private static String getKeyFromPositions(List<BlockPos> positions) {
        List<String> sorted = positions.stream()
                .map(pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ())
                .sorted()
                .toList();

        return String.join("__", sorted);
    }
}