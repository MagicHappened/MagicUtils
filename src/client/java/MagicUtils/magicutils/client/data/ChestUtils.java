package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class ChestUtils {

    public static List<BlockPos> getConnectedChestPositions(BlockPos basePos) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || basePos == null) return List.of();

        BlockState state = world.getBlockState(basePos);
        Block block = state.getBlock();

        if (!(block instanceof ChestBlock chestBlock)) return List.of();

        if (!state.contains(Properties.CHEST_TYPE) || !state.contains(Properties.HORIZONTAL_FACING)) {
            MagicUtilsClient.LOGGER.info("GetConnectedChestsPositions found 1 single chest. at pos: {}",basePos);
            return List.of(basePos);
        }

        BlockEntity be = world.getBlockEntity(basePos);
        if (!(be instanceof ChestBlockEntity)) {
            MagicUtilsClient.LOGGER.info("Why am i here, not a chestblockentity?");
            return List.of(basePos);
        }


        ChestType type = state.get(ChestBlock.CHEST_TYPE);
        Direction facing = state.get(ChestBlock.FACING);

        if (type == ChestType.SINGLE) {
            MagicUtilsClient.LOGGER.info("chest position: " + basePos);
            return List.of(basePos);
        }

        BlockPos otherPos = basePos;

        switch (facing) {
            case EAST -> {
                if (type == ChestType.LEFT) {
                    otherPos = basePos.north();
                } else {
                    otherPos = basePos.south();
                }
            }
            case WEST -> {
                if (type == ChestType.LEFT) {
                    otherPos = basePos.south();
                } else {
                    otherPos = basePos.north();
                }
            }
            case SOUTH -> {
                if (type == ChestType.LEFT) {
                    otherPos = basePos.east();
                } else {
                    otherPos = basePos.west();
                }
            }
            case NORTH -> {
                if (type == ChestType.LEFT) {
                    otherPos = basePos.west();
                } else {
                    otherPos = basePos.east();
                }
            }
            default -> {
                MagicUtilsClient.LOGGER.error("Chest facing up or down? how did we get here");
            }
        }

        BlockState otherState = world.getBlockState(otherPos);
        MagicUtilsClient.LOGGER.info("First chest position: " + basePos + "\n Second Chest Position: "+ otherPos);
        return List.of(basePos,otherPos);

    }
}