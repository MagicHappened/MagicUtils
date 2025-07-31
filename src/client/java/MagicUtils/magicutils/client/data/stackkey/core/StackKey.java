package MagicUtils.magicutils.client.data.stackkey.core;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public abstract class StackKey {
    protected final ItemStack stack;
    protected final NbtCompound nbt;

    protected StackKey(ItemStack stack, NbtCompound nbt) {
        this.stack = stack;
        this.nbt = nbt;
    }

    public ItemStack getStack() {
        return stack;
    }
    public abstract boolean equals(Object o);
    public abstract int hashCode();
}
