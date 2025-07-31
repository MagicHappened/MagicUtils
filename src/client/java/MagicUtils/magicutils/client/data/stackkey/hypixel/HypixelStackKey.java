package MagicUtils.magicutils.client.data.stackkey.hypixel;

import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class HypixelStackKey extends StackKey {
    private NbtCompound filteredNbt;

    protected HypixelStackKey(ItemStack stack, NbtCompound nbt,NbtCompound filteredNbt) {
        super(stack, nbt);
        this.filteredNbt = filteredNbt;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
