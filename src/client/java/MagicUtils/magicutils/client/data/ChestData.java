package MagicUtils.magicutils.client.data;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Extends Chest and adds the contents of the chest (ChestSlots) with slot indices.
 */
public class ChestData extends Chest {
    private final List<ChestSlot> contents;

    /**
     * Primary constructor taking a set of positions and a list of ChestSlot items.
     */
    public ChestData(Set<BlockPos> positions, List<ChestSlot> contents) {
        super(positions);
        if (contents == null) throw new IllegalArgumentException("Contents cannot be null");
        this.contents = Collections.unmodifiableList(contents);
    }

    /**
     * Convenience constructor for single chest position.
     */
    public ChestData(BlockPos pos, List<ChestSlot> contents) {
        this(Set.of(pos), contents);
    }

    /**
     * Convenience constructor for double chest (two positions).
     */
    public ChestData(BlockPos pos1, BlockPos pos2, List<ChestSlot> contents) {
        this(Set.of(pos1, pos2), contents);
    }

    public List<ChestSlot> getContents() {
        return contents;
    }

    @Override
    public @NotNull String toString() {
        return "ChestData{" +
                "positions=" + positions +
                ", contents=" + contents +
                '}';
    }
}
