package MagicUtils.magicutils.client.ui.custom.overlay;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.Set;

public class ChestOverlayRenderer {

    public static void onWorldRender(WorldRenderContext context) {
        if (ChestHighlighter.isBlinking) {
            ChestHighlighter.tick();
        }

        if (!ChestHighlighter.shouldRender()) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d camPos = context.camera().getPos();
        VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());

        for (Set<BlockPos> blocks : ChestHighlighter.getHighlightedChests()) {
            Box box = createBoxFromPositions(blocks);
            if (box == null) continue;

            box = box.expand(0.002);
            renderBoxLines(consumer, matrices, box, camPos, 1f, 0f, 0f, 1f);
        }
    }

    private static Box createBoxFromPositions(Set<BlockPos> blocks) {
        if (blocks == null || blocks.isEmpty()) return null;

        Iterator<BlockPos> it = blocks.iterator();

        if (blocks.size() == 1) {
            return new Box(it.next());
        }

        if (blocks.size() == 2) {
            BlockPos pos1 = it.next();
            BlockPos pos2 = it.next();

            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());

            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());

            // Chests are always 1 block wide, so expand the box by 1 in the axis where they differ
            int dx = Math.abs(pos1.getX() - pos2.getX());
            int dz = Math.abs(pos1.getZ() - pos2.getZ());

            if (dx == 1 && dz == 0) {
                // X-aligned double chest
                return new Box(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
            } else if (dx == 0 && dz == 1) {
                // Z-aligned double chest
                return new Box(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
            } else {
                // Unexpected alignment (not directly adjacent), fallback to a full bounding box
                return new Box(
                        Math.min(pos1.getX(), pos2.getX()),
                        Math.min(pos1.getY(), pos2.getY()),
                        Math.min(pos1.getZ(), pos2.getZ()),
                        Math.max(pos1.getX(), pos2.getX()) + 1,
                        Math.max(pos1.getY(), pos2.getY()) + 1,
                        Math.max(pos1.getZ(), pos2.getZ()) + 1
                );
            }
        }

        return null;
    }



    private static void renderBoxLines(VertexConsumer consumer, MatrixStack matrices, Box box, Vec3d camPos,
                                       float r, float g, float b, float a) {
        double x1 = box.minX - camPos.x;
        double y1 = box.minY - camPos.y;
        double z1 = box.minZ - camPos.z;
        double x2 = box.maxX - camPos.x;
        double y2 = box.maxY - camPos.y;
        double z2 = box.maxZ - camPos.z;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 12 edges of a box
        drawLine(consumer, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a); // Bottom edges
        drawLine(consumer, matrix, x1, y1, z1, x1, y1, z2, r, g, b, a);
        drawLine(consumer, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(consumer, matrix, x1, y1, z2, x2, y1, z2, r, g, b, a);

        drawLine(consumer, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a); // Top edges
        drawLine(consumer, matrix, x1, y2, z1, x1, y2, z2, r, g, b, a);
        drawLine(consumer, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(consumer, matrix, x1, y2, z2, x2, y2, z2, r, g, b, a);

        drawLine(consumer, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a); // Vertical edges
        drawLine(consumer, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(consumer, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
        drawLine(consumer, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
    }

    private static void drawLine(VertexConsumer consumer, Matrix4f matrix,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 float r, float g, float b, float a) {
        consumer.vertex(matrix, (float)x1, (float)y1, (float)z1)
                .color(r, g, b, a)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(0, 1, 0);

        consumer.vertex(matrix, (float)x2, (float)y2, (float)z2)
                .color(r, g, b, a)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(0, 1, 0);
    }
}
