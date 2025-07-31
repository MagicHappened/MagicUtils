package MagicUtils.magicutils.client.data.stackkey.core;

import net.minecraft.item.ItemStack;

public interface StackKeyProvider {

    StackKey getStackKey(ItemStack stack);

    boolean areEqual(StackKey key1, StackKey key2);
}
