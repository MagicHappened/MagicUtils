package MagicUtils.magicutils.client.config.categories;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import static MagicUtils.magicutils.client.MagicUtilsClient.CONFIG;

public class UI {
    public enum SortingMode {
        ItemName,
        Quantity
    } // maybe add later item requirements and stuff?

    public enum SortingOrder {
        Ascending,
        Descending
    }




    public static void register(ConfigBuilder builder) {
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("UI"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startEnumSelector(
                        Text.literal("Sorting Mode"),
                        SortingMode.class,
                        CONFIG.sortingMode
                )
                .setDefaultValue(SortingMode.Quantity)
                .setSaveConsumer(newValue -> CONFIG.sortingMode = newValue)
                .setTooltip(
                        Text.literal("""
                                Choose how the search orders items:
                                - Quantity: Displays items based on the amount stored.
                                - Item Name: Displays items in alphabetical order""")
                )
                .build());

        general.addEntry(entryBuilder.startEnumSelector(
                        Text.literal("Sorting Order"),
                        SortingOrder.class,
                        CONFIG.sortingOrder
                )
                .setDefaultValue(SortingOrder.Descending)
                .setSaveConsumer(newValue -> CONFIG.sortingOrder = newValue)
                .build());


        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Sidebar Tooltip"), CONFIG.sidebarTooltip)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> CONFIG.sidebarTooltip = newValue)
                .build());


    }
}
