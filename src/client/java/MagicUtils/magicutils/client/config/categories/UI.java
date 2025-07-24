package MagicUtils.magicutils.client.config.categories;

import MagicUtils.magicutils.client.config.MagicUtilsConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class UI {
    public enum SortingMode {
        ItemName,
        Quantity
    } // maybe add later item requirements and stuff?

    public enum SortingOrder {
        Ascending,
        Descending
    }




    public static void register(ConfigBuilder builder, MagicUtilsConfig config) {
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("UI"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startEnumSelector(
                        Text.literal("Sorting Mode"),
                        SortingMode.class,
                        config.sortingMode
                )
                .setDefaultValue(SortingMode.Quantity)
                .setSaveConsumer(newValue -> config.sortingMode = newValue)
                .setTooltip(
                        Text.literal("Choose how the search orders items:\n" +
                                "- Quantity: Displays items based on the amount stored.\n" +
                                "- Item Name: Displays items in alphabetical order")
                )
                .build());

        general.addEntry(entryBuilder.startEnumSelector(
                        Text.literal("Sorting Order"),
                        SortingOrder.class,
                        config.sortingOrder
                )
                .setDefaultValue(SortingOrder.Descending)
                .setSaveConsumer(newValue -> config.sortingOrder = newValue)
                .build());


        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Sidebar Tooltip"), config.SidebarTooltip)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> config.SidebarTooltip = newValue)
                .build());


    }
}
