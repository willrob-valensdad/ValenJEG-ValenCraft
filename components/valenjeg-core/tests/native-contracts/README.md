# Native contract source

These opt-in contracts are source from copied-game tests, not an automatically runnable public test suite. They retain fixture, actor, hash and action guards. Configure `valenjeg.fixtureRoot` with an absolute path to a dedicated copied fixture and `valenjeg.coreSourceRoot` with the component source root. Historical evidence inputs are listed in `inputs.json`; owner-private receipts are not redistributed or fabricated. Unconfigured paths fail the original fixture checks. These sources are kept outside the normal Gradle test source set.
