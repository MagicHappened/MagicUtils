package MagicUtils.magicutils.client.data.stackkey.hypixel;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import MagicUtils.magicutils.client.data.stackkey.core.StackKeyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import static MagicUtils.magicutils.client.utils.StackKeyUtils.encodeComponentsToNbt;

public class HypixelStackKeyProvider implements StackKeyProvider {
    @Override
    public StackKey getStackKey(ItemStack stack) {
        ItemStack copy = stack.copy();
        NbtCompound fullNbt = encodeComponentsToNbt(copy);
        NbtCompound normalized = getFilteredData(fullNbt);

        return new HypixelStackKey(copy, fullNbt, normalized);
    }

    private NbtCompound getFilteredData(NbtCompound tag) {
        MagicUtilsClient.LOGGER.info("Tag: {}",tag);
        return tag;
    }

    @Override
    public boolean areEqual(StackKey key1, StackKey key2) {
        return key1.equals(key2);
    }
}
