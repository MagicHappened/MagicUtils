package MagicUtils.magicutils.client.data;

import java.util.List;

public record HighlightTarget(Chest chest, List<Integer> matchingSlots) {}