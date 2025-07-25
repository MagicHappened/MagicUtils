package MagicUtils.magicutils.client.ui.custom.overlay;

import com.jcraft.jorbis.Block;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.Set;

public class ChestOverlayRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!ChestHighlighter.shouldRender()) return;

            MinecraftClient client = MinecraftClient.getInstance();
            Vec3d camPos = context.camera().getPos();

            // Disable texture mapping to draw solid colored lines
            GL11.glDisable(GL11.GL_TEXTURE_2D);


            // Set line width (thicker lines)
            RenderSystem.lineWidth(3.0f);

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);

            for (Set<BlockPos> blocks : ChestHighlighter.getHighlightedChests()) {
                Box box;

                if (blocks.size() == 1) {
                    BlockPos pos = blocks.iterator().next();
                    box = new Box(pos);
                } else if (blocks.size() == 2) {
                    Iterator<BlockPos> it = blocks.iterator();
                    BlockPos pos1 = it.next();
                    BlockPos pos2 = it.next();

                    // Ensure the box fully encloses both blocks
                    int minX = Math.min(pos1.getX(), pos2.getX());
                    int minY = Math.min(pos1.getY(), pos2.getY());
                    int minZ = Math.min(pos1.getZ(), pos2.getZ());

                    int maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
                    int maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
                    int maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

                    box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
                } else {
                    continue; // Skip invalid sets
                }

                box = box.expand(0.002); // avoid z-fighting

                double x1 = box.minX - camPos.x;
                double y1 = box.minY - camPos.y;
                double z1 = box.minZ - camPos.z;

                double x2 = box.maxX - camPos.x;
                double y2 = box.maxY - camPos.y;
                double z2 = box.maxZ - camPos.z;

                drawBox(buffer, x1, y1, z1, x2, y2, z2, 1.0f, 0.0f, 0.0f, 1.0f);
            }
            int glMode;
            BuiltBuffer builtBuffer = buffer.end();
            switch (builtBuffer.getDrawParameters().mode()) {
                case LINES -> glMode = GL11.GL_LINES;
                case TRIANGLES -> glMode = GL11.GL_TRIANGLES;
                case QUADS -> glMode = GL11.GL_QUADS;
                default -> glMode = GL11.GL_TRIANGLES; // fallback
            }

            GL11.glDrawArrays(glMode, 0, builtBuffer.getDrawParameters().vertexCount());
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        });
    }

    private static void drawBox(VertexConsumer consumer,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                float r, float g, float b, float a) {
        // Draw the 12 edges of the box as lines
        addLine(consumer, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(consumer, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(consumer, x1, y1, z1, x1, y1, z2, r, g, b, a);

        addLine(consumer, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(consumer, x2, y1, z1, x2, y1, z2, r, g, b, a);

        addLine(consumer, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(consumer, x1, y2, z1, x1, y2, z2, r, g, b, a);

        addLine(consumer, x1, y1, z2, x2, y1, z2, r, g, b, a);
        addLine(consumer, x1, y1, z2, x1, y2, z2, r, g, b, a);

        addLine(consumer, x2, y2, z2, x2, y2, z1, r, g, b, a);
        addLine(consumer, x2, y2, z2, x2, y1, z2, r, g, b, a);
        addLine(consumer, x2, y2, z2, x1, y2, z2, r, g, b, a);
    }

    private static void addLine(VertexConsumer consumer,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                float r, float g, float b, float a) {
        consumer.vertex((float) x1, (float) y1, (float) z1).color(r, g, b, a);
        consumer.vertex((float) x2, (float) y2, (float) z2).color(r, g, b, a);
    }
}
