package MagicUtils.magicutils.client.mixins;

import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OpenScreenS2CPacket.class)
public interface OpenScreenS2CPacketAccessor {
    @Accessor("syncId")
    int magicutils$getSyncId();
}