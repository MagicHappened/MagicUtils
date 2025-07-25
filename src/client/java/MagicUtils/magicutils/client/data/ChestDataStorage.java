package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.config.MagicUtilsConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
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

    public static Map<Item, Integer> loadAggregatedItemsWithinRange(
            BlockPos playerPos,
            double searchRange,
            RegistryWrapper.WrapperLookup lookup,
            String filter // add filter param here
    ) {
        Map<Item, Integer> aggregated = new HashMap<>();
        double rangeSq = searchRange * searchRange;
        Path dataFolder = MagicUtilsDataHandler.getCurrentContextSaveDir();

        if (!Files.exists(dataFolder)) return aggregated;

        try (var files = Files.list(dataFolder)) {
            files.filter(path -> path.toString().endsWith(".dat")).forEach(path -> {
                String filename = path.getFileName().toString().replace(".dat", "");

                List<BlockPos> positions = Arrays.stream(filename.split("__"))
                        .map(s -> {
                            String[] parts = s.split(",");
                            if (parts.length != 3) return null;
                            try {
                                return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();

                boolean inRange = positions.stream().anyMatch(pos -> pos.getSquaredDistance(playerPos) <= rangeSq);
                if (!inRange) return;

                try (var in = Files.newInputStream(path)) {
                    NbtCompound root = NbtIo.readCompressed(in, net.minecraft.nbt.NbtSizeTracker.ofUnlimitedBytes());
                    NbtList items = root.getList("Items").orElse(new NbtList());


                    for (NbtElement element : items) {
                        if (element instanceof NbtCompound slotCompound) {
                            if (!slotCompound.contains("Item")) continue;

                            NbtCompound itemCompound = slotCompound.getCompound("Item").orElse(null);
                            if (itemCompound == null) continue;

                            Optional<ItemStack> optionalStack = ItemStack.fromNbt(lookup, itemCompound);
                            if (optionalStack.isPresent()) {
                                ItemStack stack = optionalStack.get();
                                if (!stack.isEmpty()) {
                                    // Apply filter here
                                    String itemName = stack.getName().getString().toLowerCase();
                                    if (filter == null || filter.isEmpty() || itemName.contains(filter.toLowerCase())) {
                                        aggregated.merge(stack.getItem(), stack.getCount(), Integer::sum);
                                    }
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    MagicUtilsClient.LOGGER.error("Error reading chest file {}: {}", path, e);
                }
            });
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Error listing chest data files", e);
        }
        return aggregated;
    }

    public static Set<Set<BlockPos>> findGroupedChestsWithItem(ItemStack filterStack) {
        Set<Set<BlockPos>> result = new HashSet<>();
        if (filterStack == null || filterStack.isEmpty()) return result;

        Item filterItem = filterStack.getItem();
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
        int range = MagicUtilsConfig.searchRange;

        Map<String, NbtList> allChestData = loadAllChestData();
        var registryManager = MinecraftClient.getInstance().getNetworkHandler() != null
                ? MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()
                : null;
        if (registryManager == null) return Set.of();

        RegistryWrapper.WrapperLookup lookup = registryManager;
        for (Map.Entry<String, NbtList> entry : allChestData.entrySet()) {
            String key = entry.getKey();
            NbtList nbtList = entry.getValue();

            boolean containsItem = false;

            for (NbtElement element : nbtList) {
                if (!(element instanceof NbtCompound slotCompound)) continue;
                if (!slotCompound.contains("Item")) continue;

                NbtCompound itemCompound = slotCompound.getCompound("Item").orElse(null);
                if (itemCompound == null) continue;

                Optional<ItemStack> optionalStack = ItemStack.fromNbt(lookup, itemCompound);
                if (optionalStack.isEmpty()) continue;

                ItemStack stack = optionalStack.get();
                if (!stack.isEmpty() && stack.isOf(filterItem)) {
                    containsItem = true;
                    break;
                }
            }

            if (!containsItem) continue;

            // Parse the position key
            String[] parts = key.split("_");
            Set<BlockPos> group = new HashSet<>();

            for (String part : parts) {
                String[] coords = part.split(",");
                if (coords.length != 3) continue;

                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);

                    BlockPos chestPos = new BlockPos(x, y, z);
                    if (chestPos.getSquaredDistance(playerPos) <= range * range) {
                        group.add(chestPos);
                    }
                } catch (NumberFormatException ignored) {}
            }

            if (!group.isEmpty()) {
                result.add(group);
            }
        }

        return result;
    }



    private static String getKeyFromPositions(List<BlockPos> positions) {
        List<String> sorted = positions.stream()
                .map(pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ())
                .sorted()
                .toList();

        return String.join("__", sorted);
    }
}