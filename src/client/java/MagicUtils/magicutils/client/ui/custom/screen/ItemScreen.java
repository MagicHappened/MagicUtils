package MagicUtils.magicutils.client.ui.custom.screen;

import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.config.MagicUtilsConfig;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;



public class ItemScreen extends Screen {

    private static final Identifier CHEST_TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");

    private final List<ItemStack> displayedItems = new ArrayList<>();

    private final int rows = 6; // double chest
    private final int columns = 9;
    private final int slotSize = 18;
    private final int backgroundWidth = 176;
    private final int backgroundHeight = 114 + rows * slotSize;
    private TextFieldWidget searchBox;
    private String filter = "";

    public ItemScreen(Text title){
        super(title);
        // Fill with dummy items for demo
        for (int i = 0; i < 54; i++) {
            displayedItems.add(new ItemStack(i % 2 == 0 ? Items.DIAMOND : Items.APPLE));
        }
    }

    @Override
    protected void init() {
        super.init();
        MagicUtilsClient.LOGGER.info("Initialized item screen");
        MagicUtilsClient.ShouldOpenScreen = false;
        int boxWidth = 100;
        int boxHeight = 10;
        int x = (this.width - backgroundWidth) / 2 ;
        int y = (this.height - backgroundHeight) / 2;

        searchBox = new TextFieldWidget(
                this.textRenderer, x+7, y+5, boxWidth, boxHeight, Text.literal("Search")
        );
        searchBox.setChangedListener(text -> {
            filter = text.toLowerCase();
            //updateFilteredItems(); not yet :D
        });

        addSelectableChild(searchBox);
        setInitialFocus(searchBox);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {


        int x = (this.width - backgroundWidth) >> 1;
        int y = (this.height - backgroundHeight) >> 1;



        renderBackground(context,mouseX,mouseY,delta);

        context.drawTexture(RenderLayer::getGuiTextured, CHEST_TEXTURE, x, y, 0f, 0f, backgroundWidth, backgroundHeight, 256, 256);
        searchBox.render(context, mouseX, mouseY, delta);

        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index >= displayedItems.size()) continue;

                ItemStack stack = displayedItems.get(index);
                int slotX = x + 8 + col * slotSize;
                int slotY = y + 18 + row * slotSize;

                context.drawItem(stack, slotX, slotY);

                if (isPointWithin(mouseX, mouseY, slotX, slotY)) {
                    hoveredStack = stack;
                }
            }
        }

        if (!hoveredStack.isEmpty()) {
            if (MagicUtilsClient.CONFIG.SidebarTooltip)
            {
                List<Text> tooltipLines = hoveredStack.getTooltip(Item.TooltipContext.DEFAULT, null, TooltipType.BASIC);

                int maxWidth = 0;
                for (Text line : tooltipLines) {
                    int lineWidth = this.textRenderer.getWidth(line);
                    if (lineWidth > maxWidth) {
                        maxWidth = lineWidth;
                    }
                }
                int tooltipWidth = maxWidth + 6 * 2 + 4; // dont know why but counted 4 pixels
                int tooltipX = x - tooltipWidth;
                int tooltipY = y + 16;
                context.drawTooltip(this.textRenderer, tooltipLines, hoveredStack.getTooltipData(), tooltipX, tooltipY);

            }
            else {
                context.drawItemTooltip(this.textRenderer, hoveredStack, mouseX, mouseY);
            }
        }
        //super.render(context, mouseX, mouseY, delta);
    }

    private boolean isPointWithin(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16;
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