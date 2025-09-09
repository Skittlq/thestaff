package com.skittlq.thestaff.util;

public class TheStaffUtils {
    public static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }

    public static float smoothstep(float a, float b, float x) {
        float t = clamp01((x - a) / (b - a));
        return t * t * (3f - 2f * t);
    }

    public static long xorshift64(long x) {
        x ^= (x << 13);
        x ^= (x >>> 7);
        x ^= (x << 17);
        return x;
    }

    public static float noise(int x, int y, int z, long seed) {
        long h = seed
                ^ (x * 0x9E3779B97F4A7C15L)
                ^ (y * 0xC2B2AE3D27D4EB4FL)
                ^ (z * 0x165667B19E3779F9L);
        h = xorshift64(h);
        // Map to [0..1] with good mantissa spread
        return ((h >>> 11) & ((1L << 53) - 1)) / (float) ((1L << 53) - 1);
    }

    public static float fbm(int x, int y, int z, long seed, int octaves) {
        float amp = 1f, sum = 0f, out = 0f;
        int sx = x, sy = y, sz = z;
        long s = seed;
        for (int i = 0; i < octaves; i++) {
            out += noise(sx, sy, sz, s) * amp;
            sum += amp;
            amp *= 0.5f;
            sx = sx * 2 + 5; sy = sy * 2 + 7; sz = sz * 2 + 11;
            s ^= 0x9E3779B97F4A7C15L; // decorrelate per octave
        }
        return out / sum;
    }
}
