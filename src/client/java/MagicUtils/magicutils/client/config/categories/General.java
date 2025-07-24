package MagicUtils.magicutils.client.config.categories;

import MagicUtils.magicutils.client.config.MagicUtilsConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class General {

    public static void register(ConfigBuilder builder, MagicUtilsConfig config) {
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();


        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("UpdateOnWorldChange"), config.UpdateOnWorldChange)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> config.UpdateOnWorldChange = newValue)
                .build());

        // ^^ example of how to add entries
        // config.VARIABLEINCONFIG for the variable itself in MagicUtilsConfig

    }
}
