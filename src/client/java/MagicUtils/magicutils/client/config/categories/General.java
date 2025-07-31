package MagicUtils.magicutils.client.config.categories;

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


    }
}
