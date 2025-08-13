package MagicUtils.magicutils.client.data.stackkey.hypixel;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import MagicUtils.magicutils.client.data.stackkey.vanilla.VanillaStackKey;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class HypixelStackKey extends StackKey {
    private NbtCompound filteredNbt;
    private final String skyblockId;
    private final NbtCompound enchantsNbt;

    protected HypixelStackKey(ItemStack stack, NbtCompound nbt) {
        super(stack.copy(), nbt);
        // handle items that don't have custom data?
        NbtCompound custom = nbt.getCompound("minecraft:custom_data").orElse(new NbtCompound());
        // might need to handle this too...
        // for now just going to return false on null items
        this.skyblockId = custom.getString("id").orElse(null);
        this.enchantsNbt = custom.getCompound("enchantments").orElse(null);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HypixelStackKey other)) return false;

        // Compare item types
        if (this.stack.getItem() != other.stack.getItem()) {
            return false;}

        // Compare display names (strings)
        if (!this.stack.getName().getString().equals(other.stack.getName().getString())) return false;

        // if one or both sb ids are null not equal (might need to change this later)
        if (this.skyblockId == null || other.skyblockId == null)return false;
        //both items have skyblock ids check if only 1 has enchant nbt then not equal
        if ((this.enchantsNbt == null) != (other.enchantsNbt == null))return false;
        //if both have an enchant nbt (as if they didnt the above if would've returned false) compare them
        if (this.enchantsNbt != null){
            return this.enchantsNbt.equals(other.enchantsNbt);
        }
        // finally if its not an enchant just compare their skyblock ids
        return this.skyblockId.equals(other.skyblockId);

    }

    @Override
    public int hashCode() {
        return 0;
    }
}
