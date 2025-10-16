package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static MagicUtils.magicutils.client.MagicUtilsClient.*;

public class ChestDataStorage {
    public static List<ChestData> cachedItems = new ArrayList<>();
    private static Path getDataFolder() {
        return MagicUtilsDataHandler.getCurrentContextSaveDir();
    }

    public static void addChestContents(Chest chest, NbtList contents) {
        String key = getKeyFromPositions(chest);
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

                    List<ChestSlot> slots = new ArrayList<>();
                    for (NbtElement element : itemsList) {
                        if (!(element instanceof NbtCompound slotCompound)) continue;

                        // Make sure it contains both Item and Slot
                        if (!slotCompound.contains("Item") || !slotCompound.contains("Slot")) continue;

                        int slotIndex = slotCompound.getInt("Slot").orElseThrow(() -> new IllegalStateException("Missing Slot in chest data"));
                        NbtCompound itemCompound = slotCompound.getCompound("Item").orElseThrow(
                                () -> new IllegalStateException("Missing Item NBT in chest data")
                        );

                        Optional<ItemStack> optionalStack = ItemStack.CODEC.parse(registryOps, itemCompound).result();
                        optionalStack.ifPresent(stack -> {
                            StackKey key = STACK_KEY_PROVIDER.getStackKey(stack);
                            // create ChestSlot with stack key and slot index
                            slots.add(new ChestSlot(key, slotIndex));
                        });
                    }

                    result.add(new ChestData(positions, slots));

                } catch (IOException e) {
                    MagicUtilsClient.LOGGER.error("Error reading chest file {}: {}", path, e);
                }

            });
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Error listing chest data files", e);
        }

        cachedItems = new ArrayList<>(result);
    }
    public static Map<StackKey, Integer> getItemsWithinRange() {
        Map<StackKey, Integer> aggregated = new HashMap<>();
        assert MinecraftClient.getInstance().player != null;
        BlockPos origin = MinecraftClient.getInstance().player.getBlockPos();

        List<ChestData> allChestData = cachedItems;

        assert allChestData != null;
        for (ChestData chestData : allChestData) {
            Chest chest = new Chest(chestData.positions);
            // Only process groups within range
            boolean withinRange = chest.positions.stream().anyMatch(pos -> pos.isWithinDistance(origin, CONFIG.searchRange));
            if (!withinRange) continue;

            List<StackKey> items = chestData.getContents().stream()
                    .map(ChestSlot::getStack) // extract the StackKey from each ChestSlot
                    .toList();

            for (StackKey key : items) {
                aggregated.merge(key, key.getStack().getCount(), Integer::sum);
            }
        }

        return aggregated;
    }


    public static List<HighlightTarget> getHighlightTargets(StackKey givenKey) {
        List<HighlightTarget> results = new ArrayList<>();
        ItemStack filterStack = givenKey.getStack();
        if (filterStack.isEmpty()) return results;

        List<ChestData> allChestData = cachedItems;
        if (allChestData == null) return results;

        for (ChestData chestData : allChestData) {
            List<Integer> matchingSlots = new ArrayList<>();

            // Iterate over ChestSlot objects instead of raw StackKey
            for (ChestSlot slot : chestData.getContents()) {
                if (slot.getStack().equals(givenKey)) {
                    matchingSlots.add(slot.getSlotIndex()); // use the actual slot index
                }
            }

            if (!matchingSlots.isEmpty()) {
                results.add(new HighlightTarget(chestData, matchingSlots));
            }
        }

        return results;
    }



    private static String getKeyFromPositions(Chest chest) {
        List<String> sorted = chest.positions.stream()
                .map(pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ())
                .sorted()
                .toList();

        return String.join("__", sorted);
    }
}