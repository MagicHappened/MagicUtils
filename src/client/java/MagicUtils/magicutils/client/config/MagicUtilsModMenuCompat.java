package MagicUtils.magicutils.client.config;

import MagicUtils.magicutils.client.MagicUtilsClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.text.Text;

public class MagicUtilsModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            MagicUtilsConfig config = MagicUtilsConfig.get();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("MagicUtils Config"))
                    .setSavingRunnable(() -> {
                        MagicUtilsConfig.save(); // Save to disk
                        MagicUtilsClient.CONFIG = MagicUtilsConfig.get(); // Refresh reference
                    });

            ConfigCategoryHandler.registerAll(builder, config);
            return builder.build();
        };
    }
}