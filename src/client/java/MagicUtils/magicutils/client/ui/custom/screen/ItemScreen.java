package MagicUtils.magicutils.client.ui.custom.screen;

import MagicUtils.magicutils.client.MagicUtilsClient;
import static MagicUtils.magicutils.client.MagicUtilsClient.CONFIG;
import MagicUtils.magicutils.client.data.StackKey;
import MagicUtils.magicutils.client.ui.custom.overlay.ChestHighlighter;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtOps;
import MagicUtils.magicutils.client.config.categories.UI;
import MagicUtils.magicutils.client.data.ChestDataStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ItemScreen extends Screen {

    private static final Identifier CHEST_TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");
    private final List<ItemStack> displayedItems = new ArrayList<>();
    private final List<Integer> displayedCounts = new ArrayList<>();

    private final int rows = 6;
    private final int columns = 9;
    private final int slotSize = 18;
    private final int backgroundWidth = 176;
    private final int backgroundHeight = 114 + rows * slotSize;

    private TextFieldWidget searchBox;
    private String filter = "";

    public ItemScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        MagicUtilsClient.ShouldOpenScreen = false;

        int boxWidth = 120;
        int boxHeight = 12;
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        searchBox = new TextFieldWidget(this.textRenderer, x + 7, y + 5, boxWidth, boxHeight, Text.literal("Search"));
        searchBox.setChangedListener(text -> {
            filter = text;
            if (!filter.isEmpty()) updateDisplayedItems(filter);
            else updateDisplayedItems();
        });

        addSelectableChild(searchBox);
        setInitialFocus(searchBox);

        updateDisplayedItems();
    }

    private void updateDisplayedItemsFromMap(Map<StackKey, Integer> aggregated) {
        displayedItems.clear();
        displayedCounts.clear();
        if (aggregated == null || aggregated.isEmpty()) return;

        // could replace explicit declaration with var but meh
        Stream<Map.Entry<ItemStack,Integer>> entryStream = aggregated.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().stack(), entry.getValue()));

        Comparator<Map.Entry<ItemStack, Integer>> comparator = switch (CONFIG.sortingMode) {
            case Quantity -> Map.Entry.comparingByValue();
            case ItemName -> Comparator
                    .comparing((Map.Entry<ItemStack, Integer> e) -> e.getKey().getName().getString())
                    .thenComparing(e -> serializeComponentsToString(e.getKey().getComponents()));
        };

        if (CONFIG.sortingOrder == UI.SortingOrder.Descending) {
            comparator = comparator.reversed();
        }

        entryStream.sorted(comparator)
                .forEach(entry -> {
                    ItemStack stack = entry.getKey().copy();
                    stack.setCount(1);
                    displayedItems.add(stack);
                    displayedCounts.add(entry.getValue());
                });
    }
    private void updateDisplayedItems() {
        var allItems = ChestDataStorage.loadItemsWithinRange();
        updateDisplayedItemsFromMap(allItems);
    }
    private void updateDisplayedItems(String filter) {
        var allItems = ChestDataStorage.loadItemsWithinRange(filter);
        updateDisplayedItemsFromMap(allItems);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = (this.width - backgroundWidth) >> 1;
        int y = (this.height - backgroundHeight) >> 1;

        renderBackground(context, mouseX, mouseY, delta);
        context.drawTexture(RenderLayer::getGuiTextured, CHEST_TEXTURE, x, y, 0f, 0f, backgroundWidth, backgroundHeight, 256, 256);
        searchBox.render(context, mouseX, mouseY, delta);

        ItemStack hoveredStack = ItemStack.EMPTY;

        // First pass: draw items only
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index >= displayedItems.size()) continue;

                ItemStack stack = displayedItems.get(index);
                int slotX = x + 8 + col * slotSize;
                int slotY = y + 18 + row * slotSize;

                context.drawItem(stack, slotX, slotY);
            }
        }

        // Second pass: draw counts on top of items with higher z-level
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 300); // Higher z-level than items

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index >= displayedItems.size()) continue;

                ItemStack stack = displayedItems.get(index);
                int slotX = x + 8 + col * slotSize;
                int slotY = y + 18 + row * slotSize;

                int actualCount = displayedCounts.get(index);
                String countText = formatCount(actualCount);
                int textWidth = textRenderer.getWidth(countText);

                // Draw count at bottom right inside the slot
                context.drawText(textRenderer, countText, slotX + 16 - textWidth, slotY + 8, 0xFFFFFF, false);

                // Detect hovered stack
                if (isPointWithin(mouseX, mouseY, slotX, slotY)) {
                    hoveredStack = stack;
                }
            }
        }

        context.getMatrices().pop();

        // Tooltip rendering
        if (!hoveredStack.isEmpty()) {
            if (CONFIG.sidebarTooltip) {
                List<Text> tooltipLines = hoveredStack.getTooltip(Item.TooltipContext.DEFAULT, null, TooltipType.BASIC);
                int maxWidth = tooltipLines.stream().mapToInt(textRenderer::getWidth).max().orElse(0);
                int tooltipX = x - maxWidth - 16;
                int tooltipY = y + 16;
                context.drawTooltip(textRenderer, tooltipLines, hoveredStack.getTooltipData(), tooltipX, tooltipY);
            } else {
                context.drawItemTooltip(textRenderer, hoveredStack, mouseX, mouseY);
            }
        }
    }

    private String formatCount(int count) {
        if (count >= 1_000_000) return String.format("%.1fM", count / 1_000_000f);
        if (count >= 1_000) return String.format("%.1fk", count / 1_000f);
        return String.valueOf(count);
    }

    private boolean isPointWithin(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16;
    }

    private static String serializeComponentsToString(ComponentMap components) {
        if (components == null) return "";
        // ComponentMap doesn't have a direct toString that guarantees stability, so convert to NBT then to string:
        // Use NbtOps to encode the components back to an NBT element, then to string
        var dynamicResult = ComponentMap.CODEC.encodeStart(NbtOps.INSTANCE, components).result();
        if (dynamicResult.isEmpty()) return "";
        var nbtElement = dynamicResult.get(); // NbtElement
        return nbtElement.toString(); // stable serialized string
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int x = (this.width - backgroundWidth) >> 1;
        int y = (this.height - backgroundHeight) >> 1;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index >= displayedItems.size()) continue;

                int slotX = x + 8 + col * slotSize;
                int slotY = y + 18 + row * slotSize;

                if (isPointWithin((int) mouseX, (int) mouseY, slotX, slotY)) {
                    ItemStack clickedStack = displayedItems.get(index);
                    if (!clickedStack.isEmpty()) {
                        ChestHighlighter.onItemClicked(clickedStack);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
