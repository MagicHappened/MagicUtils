package MagicUtils.magicutils.client.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import MagicUtils.magicutils.client.MagicUtilsClient;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "onBroken", at = @At("HEAD"))
    private void onBlockBroken(WorldAccess world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!world.isClient()) return; // Only client side

        if (state.getBlock() instanceof ChestBlock) {
            MagicUtilsClient.LOGGER.info("Chest broken at position: {}", pos);
            // TODO: add your chest data update/removal logic here
        }
    }
}
