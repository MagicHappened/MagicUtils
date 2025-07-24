package MagicUtils.magicutils.client.mixins;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.ChestDataStorage;
import MagicUtils.magicutils.client.data.ChestTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
@Mixin(HandledScreen.class)
public class HandledScreenMixin<T extends ScreenHandler> {

    @Inject(method = "removed", at = @At("TAIL"))
    private void onRemoved(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        Screen screen = client.currentScreen;
        if (!(screen instanceof GenericContainerScreen)) return;

        String title = screen.getTitle().getString().toLowerCase();
        if (!(Objects.equals(title, "chest") || Objects.equals(title, "large chest"))) return;

        ScreenHandler handler = ((HandledScreen<?>) (Object) this).getScreenHandler();
        int syncId = handler.syncId;

        List<BlockPos> positions = ChestTracker.syncIdToChestPositions.remove(syncId);
        if (positions == null || positions.isEmpty()) return;

        if (handler instanceof GenericContainerScreenHandler genericHandler) {
            NbtList itemsList = new NbtList();
            for (int i = 0; i < genericHandler.getInventory().size(); i++) {
                ItemStack stack = genericHandler.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    NbtCompound itemData = new NbtCompound();
                    itemData.putInt("Slot", i);
                    itemData.put("Item", stack.writeNbt(new NbtCompound()));
                    itemsList.add(itemData);
                }
            }
            ChestDataStorage.addChestContents(positions, itemsList);
        }
    }
}
