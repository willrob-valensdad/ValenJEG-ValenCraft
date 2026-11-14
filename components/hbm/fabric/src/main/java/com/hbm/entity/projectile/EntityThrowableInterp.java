// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.level.Level;

public abstract class EntityThrowableInterp extends EntityThrowableNT {

    public EntityThrowableInterp(EntityType<? extends EntityThrowableInterp> type, Level level) {
        super(type, level);
    }

    @Override
    protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this, 3 + approachNum());
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            super.tick();
        } else {
            this.getInterpolation().interpolate();
            this.baseTick();
        }
    }

    public int approachNum() {
        return 0;
    }
}
