// ValenJEG proposal. Registry id stays block_copper. Root integrates this beside block_titanium.
// The port already has assets/hbm/textures/block/block_copper.png. Do not add a second texture.
// Pinned HbmMods ad3185b registers BlockBeaconable hardness 5, resistance 20, metal sound.
// Existing method, not a new API:
// storage(String, NTMMaterial, float hardness, float resistance, SoundType, MapColor)
storage("block_copper", Mats.MAT_COPPER, 5.0F, 20.0F, SoundType.METAL, MapColor.COLOR_ORANGE);
