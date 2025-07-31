package MagicUtils.magicutils.client.utils;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;

public class StackKeyUtils {
    public static NbtCompound encodeComponentsToNbt(ItemStack stack) {
        RegistryWrapper.WrapperLookup lookup = MinecraftClient.getInstance().getNetworkHandler() != null
                ? MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()
                : null;

        if (lookup == null) {
            MagicUtilsClient.LOGGER.warn("No registry lookup available, returning empty NbtCompound");
            return new NbtCompound();
        }

        DynamicOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, lookup);

        return ComponentMap.CODEC.encodeStart(ops,stack.getComponents())
                .resultOrPartial(error -> MagicUtilsClient.LOGGER.warn("ComponentMap to NBT error: {}", error))
                .filter(tag -> tag instanceof NbtCompound)
                .map(tag -> (NbtCompound) tag)
                .orElseGet(() -> {
                    MagicUtilsClient.LOGGER.warn("ComponentMap root tag is not an NbtCompound: {}", stack.getItem());
                    return new NbtCompound();
                });
    }



    // You can add helper methods for hypixel-specific encoding here as well, or in its own helper class
}