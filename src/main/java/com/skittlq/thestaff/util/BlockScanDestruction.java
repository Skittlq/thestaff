package com.skittlq.thestaff.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.skittlq.thestaff.util.TheStaffUtils.*;

public class BlockScanDestruction {
    public static void blockScanDestruction(Player player, BlockPos origin, int radiusX, int radiusY, int depth, java.util.Set<BlockPos> targets) {
        // Edge behaviour (as before)
        final float EDGE_START   = 1f;
        final float ROUGHNESS    = 0.40f;
        final float DEPTH_WOBBLE = 1.50f;
        final float EDGE_CRUMBLE = 0.08f;

        // Deterministic seed (feel free to mix in world/player if desired)
        final long seed = 1L;

        // Orthonormal basis that stays stable near vertical view
        Vec3 look = player.getLookAngle().normalize();
        Vec3 upAxis = Math.abs(look.y) > 0.95 ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
        Vec3 right = look.cross(upAxis).normalize();
        Vec3 up    = right.cross(look).normalize();

        // Work in world-space, centred on block centres to reduce aliasing
        final Vec3 originCenter = Vec3.atCenterOf(origin);

        // Slight oversampling along depth to avoid missed voxels on diagonals
        final int SAMPLES_ALONG = 2; // try 2 or 3 if you want even fewer gaps

        for (int dSample = 0; dSample < depth * SAMPLES_ALONG; dSample++) {
            // fractional step along the ray
            double d = dSample / (double) SAMPLES_ALONG;

            // gentle radius wobble
            float wobble = (fbm(0, 0, (int) Math.floor(d), seed ^ 0xA5A5A5A5L, 3) - 0.5f) * 2f;
            int rx = Math.max(1, Math.round(radiusX + wobble * DEPTH_WOBBLE));
            int ry = Math.max(1, Math.round(radiusY + wobble * DEPTH_WOBBLE));

            // precompute forward vector once per slice
            Vec3 forward = look.scale(d);

            for (int y = -ry; y <= ry; y++) {
                for (int x = -rx; x <= rx; x++) {
                    // ellipse membership in slice-local coords
                    double nx = x / (double) rx;
                    double ny = y / (double) ry;
                    double dist = Math.sqrt(nx * nx + ny * ny);
                    if (dist > 1.25) continue;

                    float edgeWeight = smoothstep(EDGE_START, 1.0f, (float) dist);
                    float n = (fbm(
                            (int) Math.floor(originCenter.x + forward.x),
                            (int) Math.floor(originCenter.y + forward.y),
                            (int) Math.floor(originCenter.z + forward.z),
                            seed, 4) - 0.5f) * 2f;

                    double jitter = n * ROUGHNESS * edgeWeight;
                    boolean crumble = edgeWeight > 0.65f &&
                            noise(
                                    (int) Math.floor(originCenter.x + forward.x) + x,
                                    (int) Math.floor(originCenter.y + forward.y) + y,
                                    (int) Math.floor(originCenter.z + forward.z) + (int) Math.floor(d),
                                    seed ^ 0x5DEECE66DL) < EDGE_CRUMBLE;

                    boolean inside = dist <= (1.0 + jitter);
                    if (!inside || crumble) continue;

                    // ---- The important bit: ONE floor at the very end ----
                    Vec3 world = originCenter
                            .add(forward)
                            .add(right.scale(x))
                            .add(up.scale(y));
                    BlockPos target = BlockPos.containing(world.x, world.y, world.z);
                    if (!target.equals(origin)) targets.add(target.immutable());
                }
            }
        }
    }
}
