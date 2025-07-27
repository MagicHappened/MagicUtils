package MagicUtils.magicutils.client.event;

import MagicUtils.magicutils.client.ui.custom.overlay.ChestHighlighter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class TickEvents {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ChestHighlighter.isBlinking) {
                ChestHighlighter.tick();
            }
        });

    }
}
