package MagicUtils.magicutils.client.data.stackkey.vanilla;

import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import MagicUtils.magicutils.client.data.stackkey.core.StackKeyProvider;
import net.minecraft.item.ItemStack;

public class VanillaStackKeyProvider implements StackKeyProvider {

    @Override
    public StackKey getStackKey(ItemStack stack) {
        return new VanillaStackKey(stack);
    }

    @Override
    public boolean areEqual(StackKey key1, StackKey key2) {
        return key1.equals(key2);
    }
}