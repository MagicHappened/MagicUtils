package MagicUtils.magicutils.client.data;

import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Chest {
    private final Set<BlockPos> positions;

    public Chest(Set<BlockPos> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("Chest must have at least one position");
        }
        this.positions = Collections.unmodifiableSet(positions);
    }

    public Set<BlockPos> getPositions() {
        return positions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chest)) return false;
        Chest chest = (Chest) o;
        return positions.equals(chest.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions);
    }

    @Override
    public String toString() {
        return "Chest{" +
                "positions=" + positions +
                '}';
    }
}
