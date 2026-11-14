// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/** Unit config for features that previously used NoneFeatureConfiguration. */
public record HbmUnit() {
    public static final HbmUnit INSTANCE = new HbmUnit();
    public static final Codec<HbmUnit> CODEC = MapCodec.unit(INSTANCE).codec();
}
