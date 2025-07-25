package MagicUtils.magicutils.client.ui.custom.overlay;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.Set;

public class ChestOverlayRenderer {
    private static final float LINE_THICKNESS = 0.1f; // Visible thickness of the lines

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!ChestHighlighter.shouldRender()) return;

            MatrixStack matrices = context.matrixStack();
            Vec3d camPos = context.camera().getPos();
            // Use solid layer instead of lines
            VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getSolid());

            for (Set<BlockPos> blocks : ChestHighlighter.getHighlightedChests()) {
                Box box = createBoxFromPositions(blocks);
                if (box == null) continue;

                box = box.expand(0.002);
                renderThickBox(consumer, matrices, box, camPos, 1f, 0f, 0f, 0.7f);
            }
        });
    }

    private static Box createBoxFromPositions(Set<BlockPos> blocks) {
        if (blocks.size() == 1) {
            BlockPos pos = blocks.iterator().next();
            return new Box(pos);
        } else if (blocks.size() == 2) {
            Iterator<BlockPos> it = blocks.iterator();
            BlockPos pos1 = it.next();
            BlockPos pos2 = it.next();

            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());

            int maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
            int maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
            int maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

            return new Box(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return null;
    }

    private static void renderThickBox(VertexConsumer consumer, MatrixStack matrices, Box box,
                                       Vec3d camPos, float r, float g, float b, float a) {
        // Camera-relative coordinates
        double x1 = box.minX - camPos.x;
        double y1 = box.minY - camPos.y;
        double z1 = box.minZ - camPos.z;
        double x2 = box.maxX - camPos.x;
        double y2 = box.maxY - camPos.y;
        double z2 = box.maxZ - camPos.z;

        // Render each edge as a thick rectangle
        renderThickEdge(consumer, matrices, x1, y1, z1, x2, y1, z1, 0, 1, 0, r, g, b, a); // Bottom X
        renderThickEdge(consumer, matrices, x1, y1, z1, x1, y2, z1, 0, 0, 1, r, g, b, a); // Bottom Y
        renderThickEdge(consumer, matrices, x1, y1, z1, x1, y1, z2, 0, 1, 0, r, g, b, a); // Bottom Z

        renderThickEdge(consumer, matrices, x2, y1, z1, x2, y2, z1, 0, 0, 1, r, g, b, a); // XZ Y
        renderThickEdge(consumer, matrices, x2, y1, z1, x2, y1, z2, 0, 1, 0, r, g, b, a); // XY Z

        renderThickEdge(consumer, matrices, x1, y2, z1, x2, y2, z1, 0, 1, 0, r, g, b, a); // Top X
        renderThickEdge(consumer, matrices, x1, y2, z1, x1, y2, z2, 0, 1, 0, r, g, b, a); // Top Z

        renderThickEdge(consumer, matrices, x1, y1, z2, x2, y1, z2, 0, 1, 0, r, g, b, a); // Front X
        renderThickEdge(consumer, matrices, x1, y1, z2, x1, y2, z2, 0, 0, 1, r, g, b, a); // Front Y

        renderThickEdge(consumer, matrices, x2, y2, z2, x2, y2, z1, 0, 1, 0, r, g, b, a); // Back Z
        renderThickEdge(consumer, matrices, x2, y2, z2, x2, y1, z2, 0, 0, 1, r, g, b, a); // Back Y
        renderThickEdge(consumer, matrices, x2, y2, z2, x1, y2, z2, 0, 1, 0, r, g, b, a); // Back X
    }

    private static void renderThickEdge(VertexConsumer consumer, MatrixStack matrices,
                                        double x1, double y1, double z1,
                                        double x2, double y2, double z2,
                                        double nx, double ny, double nz,
                                        float r, float g, float b, float a) {
        // Calculate direction vector
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        // Normalize the direction vector
        double length = Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (length == 0) return;

        dx /= length;
        dy /= length;
        dz /= length;

        // Calculate perpendicular vectors for thickness
        double perpX1, perpY1, perpZ1;
        double perpX2, perpY2, perpZ2;

        if (dy != 0) {
            // For vertical lines, use different perpendicular
            perpX1 = LINE_THICKNESS;
            perpY1 = 0;
            perpZ1 = 0;

            perpX2 = 0;
            perpY2 = 0;
            perpZ2 = LINE_THICKNESS;
        } else {
            // For horizontal lines, use vertical perpendicular
            perpX1 = 0;
            perpY1 = LINE_THICKNESS;
            perpZ1 = 0;

            perpX2 = 0;
            perpY2 = 0;
            perpZ2 = LINE_THICKNESS;
        }

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Matrix3f normalMatrix = matrices.peek().getNormalMatrix();

        // Calculate the four corners of the thick line
        float[] corners = {
                (float)(x1 + perpX1 + perpX2), (float)(y1 + perpY1 + perpY2), (float)(z1 + perpZ1 + perpZ2),
                (float)(x1 - perpX1 + perpX2), (float)(y1 - perpY1 + perpY2), (float)(z1 - perpZ1 + perpZ2),
                (float)(x2 - perpX1 - perpX2), (float)(y2 - perpY1 - perpY2), (float)(z2 - perpZ1 - perpZ2),
                (float)(x2 + perpX1 - perpX2), (float)(y2 + perpY1 - perpY2), (float)(z2 + perpZ1 - perpZ2)
        };

        // Transform the corners by the matrix
        for (int i = 0; i < 4; i++) {
            int idx = i * 3;
            float x = corners[idx];
            float y = corners[idx+1];
            float z = corners[idx+2];

            // Manual matrix multiplication
            corners[idx]   = matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
            corners[idx+1] = matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
            corners[idx+2] = matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32();
        }

        // Transform normal
        float normalX = (float)nx;
        float normalY = (float)ny;
        float normalZ = (float)nz;
        float tx = normalMatrix.m00() * normalX + normalMatrix.m10() * normalY + normalMatrix.m20() * normalZ;
        float ty = normalMatrix.m01() * normalX + normalMatrix.m11() * normalY + normalMatrix.m21() * normalZ;
        float tz = normalMatrix.m02() * normalX + normalMatrix.m12() * normalY + normalMatrix.m22() * normalZ;

        // Render the quad
        consumer.vertex(corners[0], corners[1], corners[2])
                .color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(tx, ty, tz);

        consumer.vertex(corners[3], corners[4], corners[5])
                .color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(tx, ty, tz);

        consumer.vertex(corners[6], corners[7], corners[8])
                .color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(tx, ty, tz);

        consumer.vertex(corners[9], corners[10], corners[11])
                .color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(tx, ty, tz);
    }
}