// client/WebHookController.java
package com.skittlq.thestaff.client.webhook;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class WebHookController {
    public static final WebHookController INSTANCE = new WebHookController();

    private boolean active = false;
    private Vec3 anchor = null;
    private double ropeLen = 15.0;
    private boolean reelIn = false;

    private WebHookController() {}

    public void start(Vec3 anchor, double ropeLen) {
        this.active = true;
        this.anchor = anchor;
        this.ropeLen = ropeLen;
    }

    public void stop() {
        this.active = false;
        this.anchor = null;
    }

    public void setReel(boolean reel) {
        this.reelIn = reel;
    }

    public void tick(Player player) {
        if (!active || anchor == null) return;

        Vec3 eye = player.getEyePosition();
        Vec3 rope = eye.subtract(anchor);
        double d = rope.length();
        if (d < 1e-4) return;

        Vec3 rN = rope.scale(1.0 / d);
        Vec3 v = player.getDeltaMovement();

        // reel shortens rope
        if (reelIn && ropeLen > 2.0) ropeLen -= 0.3;

        double slack = d - ropeLen;
        if (slack > 0) {
            // kill outward radial
            double vAlong = v.dot(rN);
            if (vAlong > 0) v = v.subtract(rN.scale(vAlong));

            // inward pull proportional to slack
            double strength = 0.3 + slack * 0.15;
            v = v.add(rN.scale(-strength));
            player.fallDistance = 0f;
        }

        player.setDeltaMovement(v.scale(0.98)); // slight damping
    }
}
