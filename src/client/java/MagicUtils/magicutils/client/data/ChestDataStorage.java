package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import MagicUtils.magicutils.client.data.stackkey.core.StackKeyProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static MagicUtils.magicutils.client.MagicUtilsClient.CONFIG;
import static MagicUtils.magicutils.client.MagicUtilsClient.STACK_KEY_PROVIDER;

public class ChestDataStorage {


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

                            // Save new single chest file
                            Path newFile = dataFolder.resolve(newKey + ".dat");
                            try (var out = Files.newOutputStream(newFile)) {
                                NbtIo.writeCompressed(root, out);
                            }

                            // Delete old double chest file
                            Files.deleteIfExists(path);

                        } catch (IOException e) {
                            MagicUtilsClient.LOGGER.error("Failed to update chest data file after chest break: {}", filename, e);
                        }
                    });
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Failed to list chest data folder during chest break handling", e);
        }
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

    public static @Nullable List<Pair<Set<BlockPos>, List<ItemStack>>> loadChestData() {
        List<Pair<Set<BlockPos>, List<ItemStack>>> result = new ArrayList<>();
        Path dataFolder = MagicUtilsDataHandler.getCurrentContextSaveDir();
        RegistryWrapper.WrapperLookup lookup = MinecraftClient.getInstance().getNetworkHandler() != null
                ? MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()
                : null;
        if (lookup == null) return null;
        if (!Files.exists(dataFolder)) return result;

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

                    List<ItemStack> items = new ArrayList<>();
                    for (NbtElement element : itemsList) {
                        if (!(element instanceof NbtCompound slotCompound)) continue;
                        if (!slotCompound.contains("Item")) continue;

                        NbtCompound itemCompound = slotCompound.getCompound("Item").orElse(null);
                        if (itemCompound == null) continue;

                        Optional<ItemStack> optionalStack = ItemStack.CODEC.parse(registryOps, itemCompound).result();
                        optionalStack.ifPresent(items::add);
                    }

                    result.add(Pair.of(positions, items));

                } catch (IOException e) {
                    MagicUtilsClient.LOGGER.error("Error reading chest file {}: {}", path, e);
                }
            });
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Error listing chest data files", e);
        }

        return result;
    }

    // 1. Core logic: loads all items in range (no filtering)
    public static Map<StackKey, Integer> loadItemsWithinRange() {
        Map<StackKey, Integer> aggregated = new HashMap<>();
        assert MinecraftClient.getInstance().player != null;
        BlockPos origin = MinecraftClient.getInstance().player.getBlockPos();

        List<Pair<Set<BlockPos>, List<ItemStack>>> chestData = loadChestData();

        assert chestData != null;
        for (Pair<Set<BlockPos>, List<ItemStack>> pair : chestData) {
            Set<BlockPos> positions = pair.getLeft();

            // Only process groups within range
            boolean withinRange = positions.stream().anyMatch(pos -> pos.isWithinDistance(origin, CONFIG.searchRange));
            if (!withinRange) continue;

            List<ItemStack> items = pair.getRight();
            for (ItemStack stack : items) {
                if (stack.isEmpty()) continue;


                StackKey key = STACK_KEY_PROVIDER.getStackKey(stack);
                aggregated.merge(key, stack.getCount(), Integer::sum);
            }
        }

        return aggregated;
    }
    public static Map<StackKey, Integer> loadItemsWithinRange(
            String filter
    ) {
        Map<StackKey, Integer> raw = loadItemsWithinRange();
        String loweredFilter = filter.toLowerCase();
        Map<StackKey, Integer> filtered = new HashMap<>();

        for (var entry : raw.entrySet()) {
            ItemStack stack = entry.getKey();
            int count = entry.getValue();

            boolean matches;

            if (loweredFilter.charAt(0) == '#') {
                String tooltipQuery = loweredFilter.substring(1);
                List<Text> tooltipLines = stack.getTooltip(null, null, TooltipType.BASIC);
                matches = tooltipLines.stream()
                        .map(text -> text.getString().toLowerCase())
                        .anyMatch(line -> line.contains(tooltipQuery));
            } else {
                matches = stack.getItem().getName().getString().toLowerCase().contains(loweredFilter);
            }

            if (matches) {
                filtered.put(entry.getKey(), count);
            }
        }

        return filtered;
    }

    public static Set<Set<BlockPos>> getItemPositions(ItemStack filterStack) {
        Set<Set<BlockPos>> result = new HashSet<>();
        if (filterStack == null || filterStack.isEmpty()) return result;

        StackKey filterKey = new StackKey(filterStack);
        Path dataFolder = MagicUtilsDataHandler.getCurrentContextSaveDir();

        if (!Files.exists(dataFolder)) return result;

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

                if (positions.isEmpty()) return;

                try (var in = Files.newInputStream(path)) {
                    RegistryWrapper.WrapperLookup lookup = MinecraftClient.getInstance().getNetworkHandler() != null
                            ? MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()
                            : null;
                    if (lookup == null) return;

                    RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, lookup);

                    NbtCompound root = NbtIo.readCompressed(in, net.minecraft.nbt.NbtSizeTracker.ofUnlimitedBytes());
                    NbtList items = root.getList("Items").orElse(null);

                    if (items == null) return;

                    for (NbtElement element : items) {
                        if (!(element instanceof NbtCompound slotCompound)) continue;
                        if (!slotCompound.contains("Item")) continue;

                        NbtCompound itemCompound = slotCompound.getCompound("Item").orElse(null);
                        if (itemCompound == null) continue;

                        Optional<ItemStack> optionalStack = ItemStack.CODEC.parse(registryOps, itemCompound).result();
                        if (optionalStack.isEmpty()) continue;

                        ItemStack stack = optionalStack.get();
                        if (stack.isEmpty()) continue;

                        StackKey key = new StackKey(stack);
                        if (key.equals(filterKey)) {
                            result.add(Set.copyOf(positions));
                            break; // no need to continue scanning this file
                        }
                    }

                } catch (IOException e) {
                    MagicUtilsClient.LOGGER.error("Error reading chest file {}: {}", path, e);
                }
            });
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Error listing chest data files", e);
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