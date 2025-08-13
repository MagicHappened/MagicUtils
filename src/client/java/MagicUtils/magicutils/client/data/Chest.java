package MagicUtils.magicutils.client.data;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Chest {
    public final Set<BlockPos> positions;
    public Chest(Set<BlockPos> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("Chest must have at least one position");
        }
        this.positions = Collections.unmodifiableSet(positions);
    }
    // Convenience constructor: single BlockPos
    public Chest(BlockPos pos1) {
        this(Set.of(pos1)); // Delegate to main constructor
    }

    // Convenience constructor: two BlockPos values
    public Chest(BlockPos pos1, BlockPos pos2) {
        this(Set.of(pos1, pos2)); // Delegate to main constructor
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chest)) return false;
        return positions.equals(((Chest) o).positions);
    }

    @Override
    public @NotNull String toString() {
        return "Chest{" +
                "positions=" + positions +
                '}';
    }
}
