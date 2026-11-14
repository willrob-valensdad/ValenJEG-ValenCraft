# Component source and current status

ValenJEG is a mini-modpack inside the modular ValenCraft modpack. This repository includes its contained mod projects and integration code.

| Component | Source included | Current build evidence |
| --- | --- | --- |
| ValenJEG core | `components/valenjeg-core` | Independently built source checkpoint passed `check jar sourcesJar`; the full revised pack remains unqualified |
| hbm | `components/hbm` (69481 files) | port in progress; no complete 26.3 build |
| ad-astra | `components/ad-astra` (2719 files) | Ad Astra port in progress; Common Storage Lib JAR builds |
| scorched-guns | `components/scorched-guns` (15848 files) | port in progress; compiler failures remain |
| combined-arms | `components/combined-arms` (10183 files) | Combined Arms + Superb Warfare port incomplete; SEM source missing |
| create-fly | `components/create-fly` (16011 files) | existing 26.3 port; broader rendering acceptance remains incomplete |
| spacereloaded | `components/spacereloaded` (3678 files) | existing 26.3 port; individual native gameplay tests recorded privately |
| Cross-mod compatibility | `components/compatibility` | Existing source, port patches and validation tooling; no new blanket native claim |

`components/sources.json` records pinned upstream commits and file inventories. The public source snapshots preserve full implementation systems; unfinished ports are not feature-stripped to create a successful build.

Common Storage Lib source is under `components/ad-astra/deps/common-storage-lib`; Superb Warfare source is under `components/combined-arms/dependencies/superbwarfare/port-26.3`. Simple Enemy Mod source is under `components/combined-arms/dependencies/simpleenemymod`, pinned to the maintained NeoAlive 1.21.1 fork with original NekoYuni credits. Its 26.3 Fabric migration remains pending.

Unmodified third-party dependencies are pinned in `mods.lock.json`, rather than copied into this repository as unrelated source projects. Superseded port attempts, provider logs, local API binaries, launcher/account data and worlds are retained privately where applicable and are not runtime implementation.

Ad Astra reserved assets and the reserved HBM upstream icon are represented by exact source-archive paths and hashes. No rights to those assets are claimed by the pack MIT license. 98 reserved asset inputs did not match the pinned original archive and remain explicitly unresolved.
