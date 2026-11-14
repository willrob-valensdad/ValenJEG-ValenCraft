package com.atsuishio.superbwarfare.client.renderer.molang;

import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.value.Variable;

import java.util.function.DoubleSupplier;

public class MolangVariable {

    public static final String SBW_SYSTEM_TIME = "sbw.system_time";

    public static void register() {
        register(SBW_SYSTEM_TIME, () -> 0);
    }

    private static void register(String name, DoubleSupplier supplier) {
        MathParser.registerVariable(new Variable(name, supplier));
    }
}
