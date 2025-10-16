package MagicUtils.magicutils.client.data;


import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an item stored in a chest along with its slot index.
 */
public class ChestSlot {
    private final StackKey stack;
    private final int slotIndex;

    public ChestSlot(StackKey stack, int slotIndex) {
        if (stack == null) throw new IllegalArgumentException("StackKey cannot be null");
        if (slotIndex < 0) throw new IllegalArgumentException("Slot index cannot be negative");
        this.stack = stack;
        this.slotIndex = slotIndex;
    }

    public StackKey getStack() {
        return stack;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChestSlot)) return false;
        ChestSlot other = (ChestSlot) o;
        return slotIndex == other.slotIndex && stack.equals(other.stack);
    }

    @Override
    public int hashCode() {
        return 31 * stack.hashCode() + slotIndex;
    }

    @Override
    public @NotNull String toString() {
        return "ChestSlot{" +
                "stack=" + stack +
                ", slotIndex=" + slotIndex +
                '}';
    }
}
