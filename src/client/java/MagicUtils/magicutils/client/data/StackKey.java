package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
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

public record StackKey(ItemStack stack, NbtCompound nbt) {

    // Canonical constructor that encodes NBT from the stack
    public StackKey(ItemStack stack) {
        this(stack.copy(), encodeComponentsToNbt(stack));

    }
    private static NbtCompound encodeComponentsToNbt(ItemStack stack) {
        RegistryWrapper.WrapperLookup lookup = Objects.requireNonNull(MinecraftClient.getInstance()
                        .getNetworkHandler())
                .getRegistryManager();

        DynamicOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, lookup);

        return ComponentMap.CODEC.encodeStart(ops, stack.getComponents())
                .resultOrPartial(error -> MagicUtilsClient.LOGGER.warn("ComponentMap to NBT error: {}", error))
                .filter(tag -> tag instanceof NbtCompound)
                .map(tag -> (NbtCompound) tag)
                .orElseGet(() -> {
                    MagicUtilsClient.LOGGER.warn("ComponentMap root tag is not an NbtCompound: {}", stack.getItem());
                    return new NbtCompound();
                });
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StackKey(ItemStack stack1, NbtCompound nbt1))) return false;

        boolean itemsEqual = this.stack.getItem() == stack1.getItem();
        boolean nbtEqual = this.nbt.equals(nbt1);

        return itemsEqual && nbtEqual;
    }

    @Override
    public int hashCode() {
        // Combine item identity with deep NBT hash
        return Objects.hash(this.stack.getItem(), this.nbt);
    }
}
