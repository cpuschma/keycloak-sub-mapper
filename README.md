# Keycloak Subject OIDC Token Mapper

A tiny Keycloak custom protocol mapper that copies the user Subject (`sub`, the internal Keycloak user ID) into a configurable claim in issued OIDC tokens (ID token, access token, and/or UserInfo response).

This is useful when a downstream service needs the stable Keycloak subject identifier in a different claim name (e.g., `user_id`) while keeping the standard `sub` claim unchanged.


## Stack
- Language: Java 17
- Build tool: Apache Maven (pom.xml)
- Keycloak: SPI provider for OIDC protocol mappers (tested/targeted for Keycloak 26.x)
- Packaging: JAR (optionally an "jar-with-dependencies" via `maven-assembly-plugin`)


## Requirements
- JDK 17
- Maven 3.9+
- Docker or Podman (optional, for the provided Makefile targets)


## Build
You can build either using Maven directly or via the provided Makefile (which builds inside a containerized Maven image).

- Maven:
  ```bash
  mvn clean package
  ```

- Makefile (uses Docker or Podman, whichever is available):
  ```bash
  make build
  ```

Artifacts are produced under `target/`:
- `keycloak-sub-mapper-<version>.jar`
- `keycloak-sub-mapper-<version>-jar-with-dependencies.jar`


## Run / Try in Keycloak (Container)
A convenience target is provided to spin up a Keycloak 26.x container with the built provider mounted in:

```bash
make start-dev
```
This will:
- Mount a JAR from `./target` into `/opt/keycloak/providers/` inside the container
- Start Keycloak in dev mode on `http://localhost:8080`
- Bootstrap admin credentials via env vars:
  - `KC_BOOTSTRAP_ADMIN_USERNAME=admin`
  - `KC_BOOTSTRAP_ADMIN_PASSWORD=admin`

Login at http://localhost:8080 with the admin credentials and configure the mapper in a client as shown below.

Note: The exact jar name mounted by `start-dev` is currently hardcoded in the `Makefile`. See the TODOs section to reconcile versions and artifact names.


## Installation (Manual)
If you run Keycloak yourself (not via the Makefile):
1. Copy the built provider JAR into your Keycloak `providers/` directory, e.g.:
   ```bash
   cp target/keycloak-sub-mapper-<version>-jar-with-dependencies.jar /opt/keycloak/providers/
   ```
2. Start or restart Keycloak. The provider should be auto-discovered.


## Usage
After the provider is deployed:
1. In the Keycloak Admin Console, go to a client → Mappers → Create.
2. Mapper Type: `Map subject to claim`.
3. Configure:
   - `Token Claim Name`: the target claim (e.g., `user_id`).
   - `Add to ID token` / `Add to access token` / `Add to userinfo`: enable where you need the claim.

The mapper will write the current user's Keycloak subject ID into the configured claim.


## Scripts / Makefile Targets
- `make build` — build the project using containerized Maven and cache `.m2` locally
- `make start-dev` — run Keycloak in dev mode with the provider mounted from `target/`

Variables in `Makefile` you can override via environment (defaults shown in file):
- `KEYCLOAK_VERSION` (default: 26.4.4)
- `JAVA_VERSION` (default: 17)
- `MAVEN_VERSION` (default: 3.9.11)


## Tests
There are currently no automated tests in this repository. TODO: add unit tests and/or integration tests against a Keycloak container.


## Version Notes and Known Inconsistencies
There are a few version/artifact name mismatches in the current repo state:
- `pom.xml` sets:
  - `<version>26.0.0</version>` for the artifact
  - `<keycloak.version>26.0.0</keycloak.version>` for dependencies
- `target/` contains artifacts named like `keycloak-sub-mapper-26.4.0*.jar` (suggesting a different version was built at some point)
- `Makefile` uses `KEYCLOAK_VERSION := 26.4.4` and mounts a jar named `keycloak-sub-mapper-1.0-SNAPSHOT-jar-with-dependencies.jar`
- `maven-assembly-plugin` manifest `mainClass` points to `de.lecos.keycloak.authenticator` which does not exist in this repo and is not needed for a Keycloak provider

TODOs:
- [ ] Align the project artifact version in `pom.xml` with the intended release (e.g., 26.4.x if matching Keycloak 26.4.x)
- [ ] Align `keycloak.version` property with the Keycloak runtime version you target (e.g., 26.4.x)
- [ ] Update the `Makefile` to mount the correct built jar name from `target/`
- [ ] Consider removing the `mainClass` from the assembly plugin configuration, as this is a library/provider, not a standalone app


## License
No license file is present. TODO: add a LICENSE (e.g., Apache-2.0, MIT, or your preferred license) and update this section accordingly.


## FAQ
- Q: Is there an application entry point?
  - A: No. This is a Keycloak SPI provider discovered via ServiceLoader. The entry point is the service registration file `META-INF/services/org.keycloak.protocol.ProtocolMapper` referencing `de.lecos.SubjectOIDCTokenMapper`.
- Q: Do I need the `-jar-with-dependencies` assembly?
  - A: Often not for Keycloak providers that depend only on Keycloak APIs (provided scope). If you add third-party runtime deps, the fat jar may help. Prefer the plain jar plus placing any needed libs into `/opt/keycloak/providers`.

