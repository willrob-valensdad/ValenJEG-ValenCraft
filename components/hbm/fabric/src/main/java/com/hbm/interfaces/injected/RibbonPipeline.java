// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.interfaces.injected;

import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public interface RibbonPipeline {
    GlRenderPipeline hbm$ribbon(RenderPipeline pipeline);
}
