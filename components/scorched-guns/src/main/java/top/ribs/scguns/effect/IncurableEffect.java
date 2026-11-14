package top.ribs.scguns.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Author: MrCrayfish
 *
 * <p>Minecraft/NeoForge 26.2 removed the old per-effect cure API. Removal protection
 * for this effect is handled by ModEvents via MobEffectEvent.Remove so timed
 * expiry still works while explicit cure/removal attempts are rejected.</p>
 */
public class IncurableEffect extends MobEffect
{
    public IncurableEffect(MobEffectCategory typeIn, int liquidColorIn)
    {
        super(typeIn, liquidColorIn);
    }
}
