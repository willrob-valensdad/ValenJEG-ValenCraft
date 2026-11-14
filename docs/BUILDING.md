# Building ValenJEG

The pack root is not a Gradle mod project. `components/` contains separate implementation projects and compatibility patches. Build the appropriate components with Java 25 and the API inputs documented in their build files, then update `mods.lock.json` from the exact resulting JAR bytes.

`python3 scripts/pack.py check --side client` and `--side server` validate the dependency inventory. Pending ports remain listed until complete artifacts exist and are qualified. Neither a source snapshot nor a successful core JAR build establishes that the full mini-modpack runs.

Use `assemble --inputs DIR --output NEW_DIR` to create an instance from hash-pinned JARs and the shared/side overrides. Add `--download` to permit verified HTTPS downloads into the local cache. Assembly refuses an existing destination and fails on missing or mismatched inputs.

`mrpack --side client --output ValenJEG.mrpack` requires all ports resolved and every selected artifact published at a real HTTPS URL. No invented CurseForge project/file IDs or placeholder downloads are used. A CurseForge release archive is not supplied until the required artifacts and distribution arrangements exist.

Upstream source paths and component licenses are recorded in `components/sources.json`. Some upstream artwork is reserved; `scripts/materialize_assets.py` retrieves only explicitly pinned files from their original source archive into a local checkout. Those assets are not committed under the pack MIT license.

The initial independently built core source remains preserved at branch `source/valenjeg-core-r61`. Its `check jar sourcesJar` checks passed; the full donor ports remain in progress. The JEG lock artifact is tied to that source checkpoint, while `components/` records the newer development sources.
