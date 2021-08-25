package com.bconlon.ignitewithspyglass;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class IgniteWithSpyglassConfig
{
    public static class Common
    {
        public final ForgeConfigSpec.ConfigValue<Integer> flammability_timer;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("Gameplay");
            this.flammability_timer = builder
                    .comment("Sets how long it takes for a mob to be set on fire.")
                    .define("Flammability Timer", 400);
            builder.pop();
        }
    }

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
    }
}
