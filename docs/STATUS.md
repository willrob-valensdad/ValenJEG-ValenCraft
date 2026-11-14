# Development status

This is the ValenJEG mini-modpack source project, within the modular ValenCraft modpack. The core gun fork is one component. Source import, compilation, native testing and installation are separate states.

The core source checkpoint was built with `check jar sourcesJar`. HBM, Ad Astra, Scorched Guns and Combined Arms/Superb Warfare ports are present but unfinished. The dependency lock also lists required inputs not yet pinned or acquired. The full repaired mini-modpack has not passed native qualification and is not presented as an installable release.

The pack assembler validates hashes, preserves upstream JAR bytes, copies the maintained gameplay configs and refuses existing destinations. It stops on pending inputs. See [components](COMPONENTS.md) and [building](BUILDING.md).
