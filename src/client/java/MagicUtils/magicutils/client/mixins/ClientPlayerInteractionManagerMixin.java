package MagicUtils.magicutils.client.mixins;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.Chest;
import MagicUtils.magicutils.client.data.ChestDataStorage;
import MagicUtils.magicutils.client.data.ChestUtils;
import MagicUtils.magicutils.client.ui.custom.overlay.ChestHighlighter;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ChestUtils.lastInteractedChest = hitResult.getBlockPos();
        if (ChestHighlighter.isBlinking){
            ChestUtils.openedChest = new Chest(ChestUtils.getConnectedChestPositions(hitResult.getBlockPos()).positions);
        }
    }

    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        BlockState state = client.world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock) {
            ChestDataStorage.handleChestBreak(pos);
        }
    }
}