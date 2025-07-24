package MagicUtils.magicutils.client.mixins;

import MagicUtils.magicutils.client.data.ChestTracker;
import MagicUtils.magicutils.client.data.ChestUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onOpenScreen", at = @At("TAIL"))
    private void onOpenScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        ScreenHandlerType<?> type = packet.getScreenHandlerType();
        int syncId = ((OpenScreenS2CPacketAccessor) packet).magicutils$getSyncId();

        if (type == ScreenHandlerType.GENERIC_9X3 || type == ScreenHandlerType.GENERIC_9X6) {
            var posList = ChestUtils.getConnectedChestPositions(ChestTracker.lastInteractedChest);
            ChestTracker.syncIdToChestPositions.put(syncId, posList);
        }
    }
}
