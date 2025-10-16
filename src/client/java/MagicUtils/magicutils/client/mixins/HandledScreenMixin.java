package MagicUtils.magicutils.client.mixins;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.Chest;
import MagicUtils.magicutils.client.data.ChestDataStorage;
import MagicUtils.magicutils.client.data.ChestUtils;
import MagicUtils.magicutils.client.data.HighlightTarget;
import MagicUtils.magicutils.client.ui.custom.overlay.ChestHighlighter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

import static MagicUtils.magicutils.client.data.ChestUtils.openedChest;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {


    @Shadow protected int x;

    @Shadow protected int y;

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

        Chest chest = ChestUtils.syncIdToChestPositions.remove(syncId);
        openedChest = null;
        if (chest == null || chest.positions.isEmpty()) return;

        RegistryWrapper.WrapperLookup lookup = Objects.requireNonNull(MinecraftClient.getInstance()
                        .getNetworkHandler())
                .getRegistryManager();

        if (handler instanceof GenericContainerScreenHandler genericHandler) {
            NbtList itemsList = new NbtList();

            for (int i = 0; i < genericHandler.getInventory().size(); i++) {
                ItemStack stack = genericHandler.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    NbtCompound itemData = new NbtCompound();

                    // Store the slot number for this specific item
                    itemData.putInt("Slot", i);

                    // Store the actual item data
                    itemData.put("Item", stack.toNbt(lookup));

                    itemsList.add(itemData);
                }
            }


            ChestDataStorage.addChestContents(chest, itemsList);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!ChestHighlighter.isBlinking) return;
        if (openedChest == null) return;

        int size = openedChest.positions.size() * 27; // chest slots count
        int cols = 9;

        for (HighlightTarget target : ChestHighlighter.getHighlightedChests()) {
            if (openedChest.equals(target.chest())) {
                for (Integer index : target.matchingSlots()) {
                    if (index < size) {
                        int row = index / cols;
                        int col = index % cols;
                        int xPos = this.x + 8 + col * 18;
                        int yPos = this.y + 18 + row * 18;
                        context.fill(xPos, yPos, xPos + 16, yPos + 16, 0x80FF0000);
                    }
                }
            }
        }
    }






}