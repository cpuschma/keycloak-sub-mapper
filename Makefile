KEYCLOAK_VERSION := 26.4.4
JAVA_VERSION := 17
MAVEN_VERSION := 3.9.11

default: build

CONTAINER_CMD := $(shell command -v podman 2>/dev/null)
ifeq ($(CONTAINER_CMD),)
    CONTAINER_CMD := $(shell command -v docker 2>/dev/null)
endif

# Check if we found either command
ifeq ($(CONTAINER_CMD),)
    $(error Neither podman nor docker found in PATH)
endif

MAVEN_CMD := $(CONTAINER_CMD) run \
	--rm \
	-it \
	--name maven \
	-v "./:/src:z" \
	-v "./.m2:/root/.m2:z" \
	-w /src \
	docker.io/library/maven:$(MAVEN_VERSION)-eclipse-temurin-$(JAVA_VERSION)\
	mvn

build:
	@mkdir -p .m2 # Create maven cache directory
	@$(MAVEN_CMD) clean package

start-dev:
	-@$(CONTAINER_CMD) rm -f keycloak
	@$(CONTAINER_CMD) run \
		--name keycloak \
		--rm \
		-it \
		-p 8080:8080 \
		-e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
		-e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
		-v "./target:/opt/keycloak/providers:z" \
		quay.io/keycloak/keycloak:$(KEYCLOAK_VERSION) \
		start-dev