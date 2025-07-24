package MagicUtils.magicutils.client.config.categories;

import MagicUtils.magicutils.client.config.MagicUtilsConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class General {
    public enum SearchPriorityMode {
        NEAREST_FIRST,
        LARGEST_STOCK
    }


    public static void register(ConfigBuilder builder, MagicUtilsConfig config) {
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();


        general.addEntry(entryBuilder.startEnumSelector(
                        Text.literal("Search Priority Mode"),
                        SearchPriorityMode.class,
                        config.searchPriorityMode
                )
                .setDefaultValue(SearchPriorityMode.NEAREST_FIRST)
                .setSaveConsumer(newValue -> config.searchPriorityMode = newValue)
                .setTooltip(
                        Text.literal("Choose how the search prioritizes chests:\n" +
                                "- Nearest First: Prioritizes closest chests.\n" +
                                "- Largest Stock: Prioritizes chests with the most items.")
                )
                .build());
        // ^^ example of how to add entries
        // config.VARIABLEINCONFIG for the variable itself in MagicUtilsConfig

    }
}
