package MagicUtils.magicutils.client.data;

import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;

public class ChestData {
    private final Chest chest;  // One or two BlockPos (for double chests)
    private final List<StackKey> items;

    public ChestData(Chest chest, List<StackKey> items) {
        this.chest = chest;
        this.items = items;
    }

    public Chest getChest() {
        return this.chest;
    }

    public List<StackKey> getItems() {
        return items;
    }
}