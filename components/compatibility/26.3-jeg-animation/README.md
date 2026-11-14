# JEG animation compatibility for Minecraft 26.3

The pinned JEG release loses five animations during GeckoLib 5 resource reload because 28 vector entries contain easing names as their three numeric/Molang components. Those names occur in author source from initial file introduction. The original author build used GeckoLib 4.4.4, whose actual parser evaluates each unknown name as 0. The repair writes that already-existing behavior explicitly; it does not invent replacement keyframes or delete animations.

The builder changes only five animation JSON payloads. It preserves all valid easing fields, coordinates, timings, sounds/events, geometry, gameplay classes and 4933 other ZIP payloads. Licenses and author attribution remain unchanged. Source is GPL3; original art remains author-owned. No art files are copied into this repository.

## Build

Run build.py with --input <pinned-author.jar> --output <new-output-directory>. The exact author SHA256 or the already accepted template-upgraded SHA256 is checked. The install candidate uses the latter so native26.3 structure conversions remain intact. The output receipt proves entry-set equality and exact byte equality of every other payload.

## Native regression

AnimationBakeCheck.java runs both actual dependency versions. Supply the existing 26.3 client/library classpath plus pinned GeckoLib 5.5.7, original GeckoLib 4.4.4 and its nested mclib-20.jar. Compile/run with two active CPUs and 1GiB heap, nice15. Invoke AnimationBakeCheck <original.jar> <candidate.jar> <result.json>.

The original reproduces exactly the five client errors, 388/393 successfully baked animations across 46 files. The candidate bakes 393/393. The test also asserts actual Gecko4 evaluations of easeInSine, easeOutSine, easeOutQuad and easeInOutSine are 0. Receipt pointers/hashes are in acceptance.json. This is actual-parser acceptance, not yet a new client reload or gameplay visual claim.
