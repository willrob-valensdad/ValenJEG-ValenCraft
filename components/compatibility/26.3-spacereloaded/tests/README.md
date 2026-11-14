# Disposable SpaceReloaded contract

Build with `build_probe.py --rei-build <recipe build directory> --gravity-build <gravity companion build directory> --output <new directory>`.
The builder verifies the pinned companion and Space inputs, and compiles with
4 JVM processors and 1 GiB. It never launches or installs anything.

Install only into a stopped disposable world copy. Registry/recipe checks require
`-Dvalen.spacereloaded.probe=true` and `-Dvalen.spacereloaded.expected=true`.
Add `-Dvalen.gravity.probe=true` for the gravity cases. The control runs without
the Space JAR, sets expected=false and adds `-Dvalen.gravity.absence=true`.

After tick100, the fixture checks18 native feature codec roundtrips, six recipe
families and236 exact REI display JSON/network roundtrips, and four dimensions.
Gravity checks run at an existing10-tick updater boundary: actual native player
teleports, Fabric join/respawn callback invocation, both modifier update orders,
ring entry/exit in Moon and Nero worlds, zero Nero ambient factor, unrelated
modifiers and unchanged crew exposure. Ring hub bounds/angular velocity are
explicit test setup, not a claim of player-built ring acceptance. The absence
control explicitly invokes the transformed Nero updater, proving the optional
Space references are not loaded on that path.

Read `spacereloaded-contracts.json`; JVM exit0 alone is not a pass. Preserve failures,
including the earlier test-set ring/ambient position correction. Save and stop,
verify the exact PID is absent, and remove the fixture before production-only
reopen proof. The gravity gates do not prove rocket flight, oxygen, transactions,
terrain distributions, client screens or Photon.

For native transactions, add `--transfers` when building and launch with
`-Dvalen.space.transfers=true`. The fixture checks commit/abort dirty-state,
real power endpoints, three standard propellant variants and serializes measured
before-save expectations. Use those values for native `execute if data block`
readback after removing the fixture and restarting; never assume a requested
transfer amount bypasses a machine's native rate limit.

Add `--gameplay` with `--transfers` and `-Dvalen.space.world=true` for sampled
native Moon/Mars/asteroid generation, survival oxygen and real assembled-rocket
flight. The launch chunk is loaded 80 ticks before assembly. Packet-sink players
send native loaded/teleport acknowledgements; independent damage cases avoid
vanilla same-tick damage cooldown. Normal author ignition/input/physics carries
the pilot to orbit and back; no position/velocity injection or private transition
call is used. The extended fixture also brakes to a native soft landing and invokes public
sneak disassembly, checking restored blocks and exact fuel conservation. Client
graphics are separate. Read both `space-transfer-contracts.json` and
`space-world-contracts.json`, preserve complete logs, save/stop and verify PID
absence. These fixtures are never packaged in a production artifact.


Use `--oxygen` with `-Dvalen.space.oxygen=true` for the separate cross-mod
oxygen contracts. These exercise the actual transformed hazard methods, both
callback orders, finite supplies, exhaustion, partial equipment, temperature
cost, a real sealed room filled through native gas APIs, and finite rocket
cabins. The packet-sink players retain normal survival and damage behavior.
The optional-absence control uses the regular fixture with
`-Dvalen.gravity.absence=true` and Space removed; it executes both transformed
Nero gravity and oxygen methods. Neither fixture is part of a production JAR.
