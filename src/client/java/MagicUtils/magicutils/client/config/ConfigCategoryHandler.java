package MagicUtils.magicutils.client.config;

import MagicUtils.magicutils.client.config.categories.General;
import MagicUtils.magicutils.client.config.categories.UI;
import me.shedaniel.clothconfig2.api.ConfigBuilder;

public class ConfigCategoryHandler {
    public static void registerAll(ConfigBuilder builder) {
        General.register(builder);
        UI.register(builder);
        // Add more categories like: DisplayCategory.register(builder, config);
    }
}