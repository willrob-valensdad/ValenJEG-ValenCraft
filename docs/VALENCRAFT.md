# ValenCraft and ValenJEG

ValenCraft is a modular Minecraft modpack. ValenJEG is a mini-modpack/module within it, developed toward standalone distribution as well as composition into the larger pack.

The ValenJEG repository root owns the module definition, dependency lock, pack configuration, branding and assembly. Its `components/` directory contains the gun fork and related implementation projects with scoped upstream licenses. A component's license does not replace the pack root MIT license, and the root MIT license does not replace component licenses.

Adding a ValenCraft module must resolve duplicate top-level mod IDs, version requirements and conflicting configuration values before assembly. The current assembler creates a separate ValenJEG instance; a general multi-module merger is not implemented here.
