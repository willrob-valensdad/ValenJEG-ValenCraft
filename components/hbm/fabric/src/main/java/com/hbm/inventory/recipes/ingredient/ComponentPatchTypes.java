// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.inventory.recipes.ingredient;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;

/** 26.3 component patches expose split added and removed sets instead of entrySet. */
public final class ComponentPatchTypes {
    private ComponentPatchTypes() {}

    public static List<Map.Entry<DataComponentType<?>, Object>> of(DataComponentPatch patch) {
        DataComponentPatch.SplitResult split = patch.split();
        List<Map.Entry<DataComponentType<?>, Object>> entries = new ArrayList<>();
        for (TypedDataComponent<?> component : split.added()) {
            entries.add(new AbstractMap.SimpleImmutableEntry<>(component.type(), component.value()));
        }
        for (DataComponentType<?> type : split.removed()) {
            entries.add(new AbstractMap.SimpleImmutableEntry<>(type, null));
        }
        return entries;
    }
}
