package MagicUtils.magicutils.client.mixins;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.Chest;
import MagicUtils.magicutils.client.data.ChestUtils;
import MagicUtils.magicutils.client.ui.custom.overlay.ChestHighlighter;
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
        if (type != ScreenHandlerType.GENERIC_9X3 && type != ScreenHandlerType.GENERIC_9X6) return;
        Chest chest = ChestUtils.getConnectedChestPositions(ChestUtils.lastInteractedChest);
        ChestUtils.syncIdToChestPositions.put(syncId, chest);


    }
}
