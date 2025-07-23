package MagicUtils.magicutils.client.commands;

import MagicUtils.magicutils.client.MagicUtilsClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class OpenItemScreen {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(build());
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> build() {
        return literal("ItemScreen")
                .executes(ctx -> executeCommand(ctx.getSource(), ""))
                .then(addItemNameArg());
    }

    private static ArgumentBuilder<FabricClientCommandSource, ?> addItemNameArg() {
        return argument("item", StringArgumentType.greedyString())
                .executes(ctx -> {
                    // This executes block is called when the 'item' argument is provided.
                    String itemName = StringArgumentType.getString(ctx, "item");
                    return executeCommand(ctx.getSource(), itemName);
                });
    }
    private static int executeCommand(FabricClientCommandSource source, String ItemName) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null) {
            source.sendError(Text.literal("You must be in-game to open this screen."));
            return 0;
        }
        MagicUtilsClient.ShouldOpenScreen = true;

        source.sendFeedback(Text.literal("Opening ItemScreen for: " + (ItemName.isEmpty() ? "all items (no specific item provided)" : ItemName)));
        MagicUtilsClient.LOGGER.info("Current screen after setScreen: {}", client.currentScreen != null ? client.currentScreen.getClass().getName() : "null");

        return 1;
    }
}