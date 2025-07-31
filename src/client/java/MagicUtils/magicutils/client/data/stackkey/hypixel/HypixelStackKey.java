package MagicUtils.magicutils.client.data.stackkey.hypixel;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import MagicUtils.magicutils.client.data.stackkey.vanilla.VanillaStackKey;
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
        if (this == o) return true;
        if (!(o instanceof HypixelStackKey other)) return false;

        // Compare item types
        if (this.stack.getItem() != other.stack.getItem()) {
            MagicUtilsClient.LOGGER.info("""
                    Item types mismatch:
                    Current item: {}
                    Other item: {}""",this.stack.getItem(),other.stack.getItem());
            return false;}

        // Compare display names (strings)
        if (!this.stack.getName().getString().equals(other.stack.getName().getString())) return false;

        MagicUtilsClient.LOGGER.info("------------------------");
        MagicUtilsClient.LOGGER.info("This.nbt: {}", this.nbt);
        MagicUtilsClient.LOGGER.info("other.nbt: {}",other.nbt);
        return this.nbt.equals(other.nbt);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
