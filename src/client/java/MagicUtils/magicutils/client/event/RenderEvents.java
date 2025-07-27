package MagicUtils.magicutils.client.event;

import MagicUtils.magicutils.client.ui.custom.overlay.ChestOverlayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class RenderEvents {
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(ChestOverlayRenderer::onWorldRender);
    }
}
