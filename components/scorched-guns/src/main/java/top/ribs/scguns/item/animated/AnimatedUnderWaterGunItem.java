package top.ribs.scguns.item.animated;

import net.minecraft.sounds.SoundEvent;

public class AnimatedUnderWaterGunItem extends AnimatedGunItem{
    public AnimatedUnderWaterGunItem(Properties properties, String path,
                              SoundEvent reloadSoundMagOut,
                              SoundEvent reloadSoundMagIn,
                              SoundEvent reloadSoundEnd,
                              SoundEvent boltPullSound,
                              SoundEvent boltReleaseSound) {
        this(properties, 13, path, reloadSoundMagOut, reloadSoundMagIn, reloadSoundEnd, boltPullSound, boltReleaseSound);
    }

    protected AnimatedUnderWaterGunItem(Properties properties, int enchantability, String path,
                                 SoundEvent reloadSoundMagOut,
                                 SoundEvent reloadSoundMagIn,
                                 SoundEvent reloadSoundEnd,
                                 SoundEvent boltPullSound,
                                 SoundEvent boltReleaseSound) {
        super(properties, enchantability, path, reloadSoundMagOut, reloadSoundMagIn, reloadSoundEnd, boltPullSound, boltReleaseSound);
    }
}
