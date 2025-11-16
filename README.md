# Keycloak Subject OIDC Token Mapper

A tiny Keycloak custom protocol mapper that copies the user Subject (`sub`, the internal Keycloak user ID) into a configurable claim in issued OIDC tokens (ID token, access token, and/or UserInfo response).

This is useful when a downstream service needs the stable Keycloak subject identifier in a different claim name (e.g., `user_id`) while keeping the standard `sub` claim unchanged.


## Stack
- Language: Java 17
- Build tool: Apache Maven (pom.xml)
- Keycloak: SPI provider for OIDC protocol mappers (tested/targeted for Keycloak 26.x)


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


## FAQ
- Q: Is there an application entry point?
  - A: No. This is a Keycloak SPI provider discovered via ServiceLoader. The entry point is the service registration file `META-INF/services/org.keycloak.protocol.ProtocolMapper` referencing `com.cpuschma.SubjectOIDCTokenMapper`.
