package MagicUtils.magicutils.client.utils;

import MagicUtils.magicutils.client.data.stackkey.core.StackKeyProvider;
import MagicUtils.magicutils.client.data.stackkey.hypixel.HypixelStackKeyProvider;
import MagicUtils.magicutils.client.data.stackkey.vanilla.VanillaStackKeyProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public class EnvironmentDetector {
    public enum EnvironmentType {
        SINGLEPLAYER,
        MULTIPLAYER_LOCAL,  // LAN or direct connect to localhost
        MULTIPLAYER_ONLINE,
        UNKNOWN
    }

    private static EnvironmentType type = EnvironmentType.UNKNOWN;
    private static String serverAddress = "";
    private static String serverName = "";
    private static StackKeyProvider activeProvider = new VanillaStackKeyProvider();

    public static void detect() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.isInSingleplayer()) {
            type = EnvironmentType.SINGLEPLAYER;
            activeProvider = new VanillaStackKeyProvider();
            serverAddress = "localhost";
            serverName = "Singleplayer";
        } else {
            ServerInfo info = client.getCurrentServerEntry();
            if (info != null) {
                serverAddress = info.address.toLowerCase();
                serverName = info.name;

                if (serverAddress.contains("hypixel")) {
                    type = EnvironmentType.MULTIPLAYER_ONLINE;
                    activeProvider = new HypixelStackKeyProvider();
                } else if (serverAddress.contains("localhost") || serverAddress.startsWith("192.168.") || serverAddress.startsWith("10.")) {
                    type = EnvironmentType.MULTIPLAYER_LOCAL;
                    activeProvider = new VanillaStackKeyProvider();
                } else { //handle servers that arent hypixel
                    type = EnvironmentType.MULTIPLAYER_ONLINE;
                    activeProvider = new VanillaStackKeyProvider();
                }
            } else {
                type = EnvironmentType.UNKNOWN;
                activeProvider = new VanillaStackKeyProvider();
                serverAddress = "";
                serverName = "";
            }
        }
    }

    public static EnvironmentType getType() {
        return type;
    }

    public static boolean isSingleplayer() {
        return type == EnvironmentType.SINGLEPLAYER;
    }

    public static boolean isMultiplayer() {
        return type == EnvironmentType.MULTIPLAYER_LOCAL || type == EnvironmentType.MULTIPLAYER_ONLINE;
    }

    public static String getServerAddress() {
        return serverAddress;
    }

    public static String getServerName() {
        return serverName;
    }

    public static StackKeyProvider getActiveProvider() {
        return activeProvider;
    }
}
