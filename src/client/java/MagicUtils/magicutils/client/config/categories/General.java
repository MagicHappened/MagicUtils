package MagicUtils.magicutils.client.config.categories;

import MagicUtils.magicutils.client.ui.custom.overlay.ChestHighlighter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import static MagicUtils.magicutils.client.MagicUtilsClient.CONFIG;

public class General {


    public static void register(ConfigBuilder builder) {
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startIntField(
                        Text.literal("Search Range"),
                        CONFIG.searchRange
                )
                .setDefaultValue(50)
                .setMin(5)
                .setMax(500)
                .setSaveConsumer(newValue -> CONFIG.searchRange = newValue)
                .setTooltip(Text.literal("The maximum distance (in blocks) to include chests in item lookup."))
                .build());

        general.addEntry(entryBuilder.startLongField(
                        Text.literal("Highlight duration"),
                        CONFIG.DURATION_MS
                )
                .setDefaultValue(5000)
                .setMin(1000)
                .setMax(50000)
                .setSaveConsumer(newValue -> {
                    CONFIG.DURATION_MS = newValue;
                    ChestHighlighter.updateHighlightTime();
                })
                .setTooltip(Text.literal("The total duration that the block will be highlighted (in ms)"))
                .build());

        general.addEntry(entryBuilder.startLongField(
                        Text.literal("Blink Duration"),
                        CONFIG.BLINK_MS
                )
                .setDefaultValue(500)
                .setMin(100)
                .setMax(2000)
                .setSaveConsumer(newValue -> {
                    CONFIG.BLINK_MS = newValue;
                    ChestHighlighter.updateHighlightTime();
                })
                .setTooltip(Text.literal("The time that the lines inside the blocks will be rendered (in ms)"))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Item highlight"),
                        CONFIG.ItemHighlight
                )
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> CONFIG.ItemHighlight = newValue)
                .setTooltip(Text.literal("Whether or not to highlight the items inside the chest"))
                .build());

    }
}
