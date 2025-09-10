package com.skittlq.thestaff.util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class DesktopPlacedData extends SavedData {
    public static final String ID = "thestaff_desktop_placed";

    // ── Data ─────────────────────────────────────────────────────────
    private boolean placed;

    // Codec: single boolean field "placed"
    public static final Codec<DesktopPlacedData> CODEC = RecordCodecBuilder.create(i ->
            i.group(Codec.BOOL.fieldOf("placed").forGetter(d -> d.placed))
                    .apply(i, DesktopPlacedData::new)
    );

    // SavedDataType binds id + supplier + codec
    private static final SavedDataType<DesktopPlacedData> TYPE =
            new SavedDataType<>(ID, () -> new DesktopPlacedData(false), CODEC);

    // Ctors used by codec / supplier
    private DesktopPlacedData(boolean placed) { this.placed = placed; }
    private DesktopPlacedData() { this(false); }

    // Access
    public static DesktopPlacedData get(ServerLevel level) {
        // In 1.21.x, DataStorage has computeIfAbsent(SavedDataType)
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isPlaced() { return placed; }
    public void markPlaced() { this.placed = true; this.setDirty(); }
}
