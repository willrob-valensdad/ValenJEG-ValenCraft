# Building the mod component

Use Java 25, Python 3 and the included Gradle wrapper. Run commands from `mod/`.

The build resolves Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3 and GeckoLib through the configured repositories. Full game qualification uses the project's pack dependency set, including its newer Fabric API.

Supply these real API JARs in `mod/compile-inputs/` or set the listed Gradle properties to their paths. They are compile/test inputs, not bundled dependency binaries.

| Property | Default filename |
| --- | --- |
| `valen_nero_api` | `nerospace-fabric-26.3-1.6.0.jar` |
| `valen_nerolandcore_api` | `nerolandcore-fabric-26.3-1.15.0.jar` |
| `valen_energy_api` | `energy-5.0.0.jar` |
| `valen_architectury_api` | `architectury-fabric-22.0.5.jar` |
| `valen_rei_api` | `RoughlyEnoughItems-26.3.824.jar` |
| `valen_basic_math_api` | `basic-math-0.6.1.jar` |

```sh
cd mod
./gradlew check jar sourcesJar
```

Outputs are in `mod/build/libs/`. The mod requires actual compatible runtime dependencies; an assembly passing compilation does not qualify a full pack. HBM is an explicit dependency of the current development source because the repaired structures use native HBM blocks. Its 26.3 port and other donor ports remain under development.

Private fixture launchers, saved worlds and operational evidence are outside this public source export.
