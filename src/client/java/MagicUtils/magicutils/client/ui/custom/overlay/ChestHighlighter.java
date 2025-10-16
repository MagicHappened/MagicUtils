package MagicUtils.magicutils.client.ui.custom.overlay;

import MagicUtils.magicutils.client.data.Chest;
import MagicUtils.magicutils.client.data.ChestDataStorage;
import MagicUtils.magicutils.client.data.HighlightTarget;
import MagicUtils.magicutils.client.data.stackkey.core.StackKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static MagicUtils.magicutils.client.MagicUtilsClient.CONFIG;
import static MagicUtils.magicutils.client.MagicUtilsClient.STACK_KEY_PROVIDER;

public class ChestHighlighter {

    // Positions to highlight
    private static List<HighlightTarget> highlightedChests = new ArrayList<>();

    private static long highlightStartTime = 0;
    public static boolean isBlinking = false;
    private static boolean visible = false;

    // Duration constants
    private static long BLINK_INTERVAL_MS = CONFIG.BLINK_MS;
    private static long TOTAL_DURATION_MS = CONFIG.DURATION_MS;

    private static long lastBlinkToggleTime = 0;

    public static void updateHighlightTime(){
        BLINK_INTERVAL_MS = CONFIG.BLINK_MS;
        TOTAL_DURATION_MS = CONFIG.DURATION_MS;
    }

    public static void onItemClicked(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        highlightedChests = ChestDataStorage.getHighlightTargets(STACK_KEY_PROVIDER.getStackKey(stack));

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

    public static List<HighlightTarget> getHighlightedChests() {
        return highlightedChests;
    }
}