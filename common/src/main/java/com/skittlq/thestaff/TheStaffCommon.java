package com.skittlq.thestaff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loader-neutral initialization shared by Fabric and NeoForge. */
public final class TheStaffCommon {
    public static final String MOD_ID = "thestaff";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private TheStaffCommon() {
    }

    public static void initialize() {
        LOGGER.info("Initializing The Staff");
        // Register loader-neutral content and services here.
    }
}

