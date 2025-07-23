package MagicUtils.magicutils.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;


public class ModCommands {
    public static void registerAll(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        OpenItemScreen.register(dispatcher, registryAccess);
        // Add more client-side commands here
    }
}
