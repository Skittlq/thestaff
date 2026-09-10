package com.skittlq.thestaff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TheStaffCommonTest {
    @Test
    void modIdIsStable() {
        assertEquals("thestaff", TheStaffCommon.MOD_ID);
    }
}

