package MagicUtils.magicutils.client.config.categories;

import MagicUtils.magicutils.client.config.MagicUtilsConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class UI {

    public static void register(ConfigBuilder builder, MagicUtilsConfig config) {
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("UI"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Sidebar Tooltip"), config.SidebarTooltip)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> config.SidebarTooltip = newValue)
                .build());


    }
}
