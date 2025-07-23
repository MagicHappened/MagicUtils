package MagicUtils.magicutils.client.data;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestTracker {
    public static BlockPos lastInteractedChest = null;

    // Maps sync ID -> list of chest block positions (single = 1, double = 2)
    public static final Map<Integer, List<BlockPos>> syncIdToChestPositions = new HashMap<>();
}