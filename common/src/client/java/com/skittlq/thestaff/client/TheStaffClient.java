package com.skittlq.thestaff.client;

import com.skittlq.thestaff.TheStaffCommon;

/** Client initialization shared by both loaders. */
public final class TheStaffClient {
    private TheStaffClient() {
    }

    public static void initialize() {
        TheStaffCommon.LOGGER.info("Initializing The Staff client");
        // Register loader-neutral client behavior here.
    }
}

