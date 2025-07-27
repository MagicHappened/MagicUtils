package MagicUtils.magicutils.client.ui.custom.overlay;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.config.MagicUtilsConfig;
import MagicUtils.magicutils.client.config.categories.General;
import MagicUtils.magicutils.client.data.ChestDataStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class ChestHighlighter {

    // Positions to highlight
    private static Set<Set<BlockPos>> highlightedChests = new HashSet<>();

    // Timing controls
    private static long highlightStartTime = 0;
    public static boolean isBlinking = false;
    private static boolean visible = false;

    // Duration constants
    private static final long BLINK_INTERVAL_MS = 500;
    private static final long TOTAL_DURATION_MS = 5000;

    private static long lastBlinkToggleTime = 0;

    public static void onItemClicked(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        Item clickedItem = stack.getItem();

        MagicUtilsClient.LOGGER.info("Item clicked: {} | Mode: {}", clickedItem.getTranslationKey(), MagicUtilsConfig.searchPriorityMode);

        highlightedChests = ChestDataStorage.findGroupedChestsWithItem(new ItemStack(clickedItem));

        startHighlighting();
        MinecraftClient.getInstance().setScreen(null); // Close the GUI
    }



    private static void startHighlighting() {
        highlightStartTime = System.currentTimeMillis();
        lastBlinkToggleTime = highlightStartTime;
        isBlinking = true;
        visible = true;
    }

    public static void tick() {
        if (!isBlinking) return;

        long currentTime = System.currentTimeMillis();

        // Stop after TOTAL_DURATION_MS
        if (currentTime - highlightStartTime >= TOTAL_DURATION_MS) {
            isBlinking = false;
            highlightedChests.clear();
            return;
        }

        // Toggle visibility every BLINK_INTERVAL_MS
        if (currentTime - lastBlinkToggleTime >= BLINK_INTERVAL_MS) {
            visible = !visible;
            lastBlinkToggleTime = currentTime;
        }
    }

    public static boolean shouldRender() {
        return isBlinking && visible;
    }

    public static Set<Set<BlockPos>> getHighlightedChests() {
        return highlightedChests;
    }
}