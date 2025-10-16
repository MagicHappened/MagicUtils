package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static MagicUtils.magicutils.client.MagicUtilsClient.CONFIG;
import static MagicUtils.magicutils.client.MagicUtilsClient.STACK_KEY_PROVIDER;

public class ChestDataStorage {
    public static List<ChestData> cachedItems = new ArrayList<>();
    private static Path getDataFolder() {
        return MagicUtilsDataHandler.getCurrentContextSaveDir();
    }

    public static void addChestContents(List<BlockPos> positions, NbtList contents) {
        String key = getKeyFromPositions(positions);
        saveChestData(key, contents);
    }


    public static void handleChestBreak(BlockPos brokenPos) {
        Path dataFolder = getDataFolder();
        if (!Files.exists(dataFolder)) return;

        String brokenKeyPart = brokenPos.getX() + "," + brokenPos.getY() + "," + brokenPos.getZ();

        try (Stream<Path> paths = Files.list(dataFolder)) {
            paths
                    .filter(p -> p.toString().endsWith(".dat"))
                    .forEach(path -> {
                        String filename = path.getFileName().toString().replace(".dat", "");

                        if (!filename.contains(brokenKeyPart)) return; // Not related to broken chest

                        List<String> positions = new ArrayList<>(Arrays.asList(filename.split("__")));

                        if (positions.size() == 1) {
                            // Single chest file - just delete
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                MagicUtilsClient.LOGGER.error("Failed to delete chest data file for broken chest: {}", filename, e);
                            }
                            return;
                        }

                        // Double chest file - remove broken chest pos, clean slots, rename file
                        positions.remove(brokenKeyPart);

                        if (positions.size() != 1) {
                            // Unexpected number of positions, skip to be safe
                            MagicUtilsClient.LOGGER.warn("Unexpected chest file format when handling broken chest: {}", filename);
                            return;
                        }

                        String newKey = positions.getFirst();

                        try {
                            // Read NBT
                            NbtCompound root;
                            try (var in = Files.newInputStream(path)) {
                                root = NbtIo.readCompressed(in, NbtSizeTracker.ofUnlimitedBytes());
                            }

                            NbtList items = root.getList("Items").orElse(new NbtList());

                            // Remove slots >= 27, keep only first 27 slots for single chest
                            NbtList filteredItems = new NbtList();
                            for (NbtElement element : items) {
                                if (!(element instanceof NbtCompound slotCompound)) continue;
                                int slot = slotCompound.getInt("Slot").orElse(30); // fallback if Slot is missing
                                if (slot < 27) {
                                    filteredItems.add(slotCompound);
                                }
                            }

                            root.put("Items", filteredItems);

                            Path newFile = dataFolder.resolve(newKey + ".dat");
                            try (var out = Files.newOutputStream(newFile)) {
                                NbtIo.writeCompressed(root, out);
                            }

                            Files.deleteIfExists(path);

                        } catch (IOException e) {
                            MagicUtilsClient.LOGGER.error("Failed to update chest data file after chest break: {}", filename, e);
                        }
                    });
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Failed to list chest data folder during chest break handling", e);
        }
        CompletableFuture.runAsync(ChestDataStorage::loadChestData);
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
        CompletableFuture.runAsync(ChestDataStorage::loadChestData);
    }

    public static void loadChestData() {
        List<ChestData> result = new ArrayList<>();
        Path dataFolder = MagicUtilsDataHandler.getCurrentContextSaveDir();
        RegistryWrapper.WrapperLookup lookup = MinecraftClient.getInstance().getNetworkHandler() != null
                ? MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()
                : null;
        if (lookup == null) return;
        if (!Files.exists(dataFolder)) return;

        try (var files = Files.list(dataFolder)) {
            files.filter(path -> path.toString().endsWith(".dat")).forEach(path -> {
                String filename = path.getFileName().toString().replace(".dat", "");

                Set<BlockPos> positions = Arrays.stream(filename.split("__"))
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
                        .collect(Collectors.toSet());

                if (positions.isEmpty()) return;

                try (var in = Files.newInputStream(path)) {
                    RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, lookup);

                    NbtCompound root = NbtIo.readCompressed(in, net.minecraft.nbt.NbtSizeTracker.ofUnlimitedBytes());
                    NbtList itemsList = root.getList("Items").orElse(null);
                    if (itemsList == null) return;

                    List<StackKey> keys = new ArrayList<>();
                    for (NbtElement element : itemsList) {
                        if (!(element instanceof NbtCompound slotCompound)) continue;
                        if (!slotCompound.contains("Item")) continue;

                        NbtCompound itemCompound = slotCompound.getCompound("Item").orElse(null);
                        if (itemCompound == null) continue;

                        Optional<ItemStack> optionalStack = ItemStack.CODEC.parse(registryOps, itemCompound).result();
                        optionalStack.ifPresent(stack -> {
                            StackKey key = STACK_KEY_PROVIDER.getStackKey(stack);
                            keys.add(key);
                        });

                    }
                    result.add(new ChestData(new Chest(positions),keys));

                } catch (IOException e) {
                    MagicUtilsClient.LOGGER.error("Error reading chest file {}: {}", path, e);
                }

            });
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Error listing chest data files", e);
        }

        cachedItems = result;
    }


    public static Map<StackKey, Integer> getItemsWithinRange() {
        Map<StackKey, Integer> aggregated = new HashMap<>();
        assert MinecraftClient.getInstance().player != null;
        BlockPos origin = MinecraftClient.getInstance().player.getBlockPos();

        List<ChestData> allChestData = cachedItems;

        assert allChestData != null;
        for (ChestData chestData : allChestData) {
            Chest chest = chestData.getChest();

            // Only process groups within range
            boolean withinRange = chest.getPositions().stream().anyMatch(pos -> pos.isWithinDistance(origin, CONFIG.searchRange));
            if (!withinRange) continue;

            List<StackKey> items = chestData.getItems();
            for (StackKey key : items) {
                aggregated.merge(key, key.getStack().getCount(), Integer::sum);
            }
        }

        return aggregated;
    }


    public static Set<Chest> getItemPositions(StackKey givenKey) {
        Set<Chest> result = new HashSet<>();
        ItemStack filterStack = givenKey.getStack();
        if (filterStack.isEmpty()) return result;
        //todo: replace this with cached result
        List<ChestData> allChestData = cachedItems;
        if (allChestData == null) return result;

        // For every chestData entry...
        for (ChestData chestData : allChestData) {
            // Check if it contains the givenKey
            for (StackKey key : chestData.getItems()) {
                if (key.equals(givenKey)) {
                    // Add this chest’s positions once
                    result.add(new Chest(chestData.getChest().getPositions()));
                    break; // stop scanning items in *this* chest, but continue with next chest
                }
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