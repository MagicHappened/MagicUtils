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
                        MagicUtilsConfig.sortingMode
                )
                .setDefaultValue(SortingMode.Quantity)
                .setSaveConsumer(newValue -> MagicUtilsConfig.sortingMode = newValue)
                .setTooltip(
                        Text.literal("Choose how the search orders items:\n" +
                                "- Quantity: Displays items based on the amount stored.\n" +
                                "- Item Name: Displays items in alphabetical order")
                )
                .build());

        general.addEntry(entryBuilder.startEnumSelector(
                        Text.literal("Sorting Order"),
                        SortingOrder.class,
                        MagicUtilsConfig.sortingOrder
                )
                .setDefaultValue(SortingOrder.Descending)
                .setSaveConsumer(newValue -> MagicUtilsConfig.sortingOrder = newValue)
                .build());


        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Sidebar Tooltip"), MagicUtilsConfig.SidebarTooltip)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> MagicUtilsConfig.SidebarTooltip = newValue)
                .build());


    }
}
