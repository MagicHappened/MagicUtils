package MagicUtils.magicutils.client.data.stackkey.vanilla;

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

import java.util.Objects;

import static MagicUtils.magicutils.client.utils.StackKeyUtils.encodeComponentsToNbt;

public class VanillaStackKey extends StackKey {

    public VanillaStackKey(ItemStack stack) {
        super(stack.copy(), encodeComponentsToNbt(stack));
        MagicUtilsClient.LOGGER.info("Constructed VanillaStackKey for item: {}", stack.getItem());
    }
    // Encoder stays here for reuse

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VanillaStackKey other)) return false;

        // Compare item types
        if (this.stack.getItem() != other.stack.getItem()) return false;

        // Compare display names (strings)
        if (!this.stack.getName().getString().equals(other.stack.getName().getString())) return false;

        // Compare the full component NBT
        return this.nbt.equals(other.nbt);
    }


    @Override
    public int hashCode() {
        return Objects.hash(stack.getItem(), stack.getName().getString(), nbt);
    }
}
