package MagicUtils.magicutils.client.event;

public class ModEvents {
    public static void register() {
        TickEvents.register();
        RenderEvents.register();
        InteractionEvents.register();
    }
}