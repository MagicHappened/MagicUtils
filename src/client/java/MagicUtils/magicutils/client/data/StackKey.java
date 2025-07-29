package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.MagicUtilsClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

import java.util.Objects;

public record StackKey(ItemStack stack, NbtCompound nbt) {

    // Canonical constructor that encodes NBT from the stack
    public StackKey(ItemStack stack) {
        this(
                stack.copy(),
                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack)
                        .result()
                        .filter(e -> e instanceof NbtCompound)
                        .map(e -> (NbtCompound) e)
                        .orElse(new NbtCompound())
        );

        MagicUtilsClient.LOGGER.info("Constructed StackKey:");
        MagicUtilsClient.LOGGER.info("- Item: {}", stack.getItem());
        MagicUtilsClient.LOGGER.info("- NBT: {}", this.nbt.asString()); // SNBT format
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            MagicUtilsClient.LOGGER.info("Equals: same instance");
            return true;
        }
        if (!(o instanceof StackKey(ItemStack stack1, NbtCompound nbt1))) return false;

        boolean itemsEqual = this.stack.getItem() == stack1.getItem();
        boolean nbtEqual = this.nbt.equals(nbt1);

        MagicUtilsClient.LOGGER.info("Comparing StackKeys:");
        MagicUtilsClient.LOGGER.info("- Items equal: {}", itemsEqual);
        MagicUtilsClient.LOGGER.info("- This NBT: {}", this.nbt.asString());
        MagicUtilsClient.LOGGER.info("- Other NBT: {}", nbt1.asString());
        MagicUtilsClient.LOGGER.info("- NBT equal: {}", nbtEqual);

        return itemsEqual && nbtEqual;
    }

    @Override
    public int hashCode() {
        // Combine item identity with deep NBT hash
        return Objects.hash(this.stack.getItem(), this.nbt);
    }
}
