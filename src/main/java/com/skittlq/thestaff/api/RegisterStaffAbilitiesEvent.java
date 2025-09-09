// com/skittlq/thestaff/api/RegisterStaffAbilitiesEvent.java
package com.skittlq.thestaff.api;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.abilities.StaffAbilities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public class RegisterStaffAbilitiesEvent extends Event {
    public void register(ResourceLocation blockId, BlockAbility ability) {
        StaffAbilities.register(blockId, ability);
    }
}
