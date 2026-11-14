# Files to upload to CurseForge

ValenJEG is the mini-modpack. Use its existing **Valen JEG - ValenCraft** modpack project, ID **1803139**.

These are the planned release filenames. The release ZIPs have **not been produced yet**; the required ports and complete pack still need qualification.

| File | Where it goes | Purpose |
| --- | --- | --- |
| `ValenJEG-0.1.0-mc26.3-Fabric-client.zip` | Main modpack file | The player download, exported from the finished CurseForge App profile |
| `ValenJEG-0.1.0-mc26.3-Fabric-server.zip` | Additional File attached to that client release, marked as server files | The matching dedicated-server bundle |
| `valenjeg-logo.png` | Project avatar/media | Owner-supplied branding; already available under `branding/` |
| Release changelog | Changelog field for the uploaded release | What actually changed and was tested |

Use the [GitHub repository](https://github.com/willrob-valensdad/ValenJEG-ValenCraft) in the project's Source setting. Source code remains there; the GitHub source ZIP, Gradle `sources.jar`, development JARs and a `.mrpack` are not the main CurseForge modpack upload.

The client ZIP must have `manifest.json` and `overrides/` directly at its root. Export the qualified profile through the CurseForge App and retain its generated manifest. This follows CurseForge's [submission format](https://support.curseforge.com/support/solutions/articles/9000197908-exporting-a-modpack-for-curseforge-project-submission) and [moderation policy](https://support.curseforge.com/support/solutions/articles/9000197279-moderation-policies).

Before delivery, every custom port must have a documented distribution route: a hosted CurseForge component file, or an approved non-CurseForge dependency. Hosted files belong in the app-generated manifest; approved external files may go in `overrides/mods/`. Their original licenses and author credits remain applicable. See the same [official submission guide](https://support.curseforge.com/support/solutions/articles/9000197908-exporting-a-modpack-for-curseforge-project-submission).

The server bundle will contain matching server-side mod versions, shared gameplay configs and setup/start instructions. It will use fresh server defaults and will not contain an existing world, player data, credentials or private server settings. The final delivery will include exact filenames, SHA-256 hashes and release-specific validation results.

Current remaining work: finish required donor ports, pin the remaining dependency inputs, qualify the complete client/server pack, verify distribution availability, and export the final client profile. This document is an upload plan, not a claim of a finished build or CurseForge approval.
