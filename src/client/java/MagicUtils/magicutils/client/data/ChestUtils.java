package MagicUtils.magicutils.client.data;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestUtils {

    // Track the last chest block position the player interacted with
    public static BlockPos lastInteractedChest = null;
    public static Chest openedChest = null;

    // Map from sync IDs to the chest positions they correspond to (single or double chest)
    public static final Map<Integer, Chest> syncIdToChestPositions = new HashMap<>();

    private static Direction getChestConnectionOffset(Direction facing, ChestType type) {
        return switch (type) {
            case LEFT  -> getSideDirection(facing).getOpposite(); // flipped as per fix
            case RIGHT -> getSideDirection(facing);
            default    -> Direction.NORTH; // SINGLE chest fallback
        };
    }

    private static Direction getSideDirection(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case WEST  -> Direction.SOUTH;
            case EAST  -> Direction.NORTH;
            default -> throw new IllegalStateException("Invalid chest facing: " + facing);
        };
    }

    public static Chest getConnectedChestPositions(BlockPos basePos) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || basePos == null) return null;

        BlockState state = world.getBlockState(basePos);
        if (!(state.getBlock() instanceof ChestBlock)) return null;

        if (!state.contains(Properties.CHEST_TYPE) || !state.contains(Properties.HORIZONTAL_FACING)) {
            return new Chest(basePos);
        }

        ChestType type = state.get(ChestBlock.CHEST_TYPE);
        Direction facing = state.get(ChestBlock.FACING);

        if (type == ChestType.SINGLE) return new Chest(basePos);

        Direction offset = getChestConnectionOffset(facing, type);
        BlockPos otherPos = basePos.offset(offset);

        return new Chest(basePos, otherPos);
    }
}
