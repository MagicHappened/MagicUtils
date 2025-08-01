package MagicUtils.magicutils.client.config;
import MagicUtils.magicutils.client.MagicUtilsClient;
import MagicUtils.magicutils.client.config.categories.UI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.fabricmc.loader.api.FabricLoader;

public class MagicUtilsConfig {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "magicutils.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static MagicUtilsConfig INSTANCE;

    // Config values: non-static now
    public Boolean sidebarTooltip = false;
    public int searchRange = 50;
    public UI.SortingMode sortingMode = UI.SortingMode.Quantity;
    public UI.SortingOrder sortingOrder = UI.SortingOrder.Descending;

    private MagicUtilsConfig() {
        // private constructor to prevent direct instantiation outside this class
    }

    public static MagicUtilsConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, MagicUtilsConfig.class);
            } catch (IOException e) {
                MagicUtilsClient.LOGGER.error("Error loading config! printing error:\n{}", (Object) e.getStackTrace());
                INSTANCE = new MagicUtilsConfig();
            }
        } else {
            INSTANCE = new MagicUtilsConfig();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(get(), writer);
        } catch (IOException e) {
            MagicUtilsClient.LOGGER.error("Error writing config! printing error:\n{}", (Object) e.getStackTrace());
        }
    }
}
