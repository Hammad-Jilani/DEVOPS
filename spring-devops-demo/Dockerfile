# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — BUILD
# Uses a full JDK + Maven image to compile the source and package the fat JAR.
# This stage is discarded after the build; nothing from it leaks into the final
# image except the JAR file we explicitly copy across.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set a working directory inside the container for this stage
WORKDIR /build

# ── Copy dependency descriptors first ────────────────────────────────────────
# Copying pom.xml before the source code lets Docker cache the dependency-
# download layer separately. If only source files change (not pom.xml), the
# `mvn dependency:go-offline` layer is reused from cache — much faster builds.
COPY pom.xml .

# Download all dependencies into the local Maven cache (.m2).
# `--no-transfer-progress` suppresses the noisy download progress lines in CI logs.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --no-transfer-progress dependency:go-offline

# ── Copy the full source tree ─────────────────────────────────────────────────
COPY src ./src

# ── Build and package ─────────────────────────────────────────────────────────
# -DskipTests: tests run in a dedicated CI step, not inside Docker build.
# The fat JAR lands at target/spring-devops-demo.jar (set by <finalName> in pom.xml).
RUN --mount=type=cache,target=/root/.m2 \
    mvn --no-transfer-progress package -DskipTests


# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — RUNTIME
# Minimal JRE-only image. No compiler, no Maven, no source code.
# Significantly smaller attack surface and image size (~100 MB vs ~500 MB).
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# ── Security: run as a non-root user ─────────────────────────────────────────
# Running as root inside a container is a security risk.
# We create a dedicated system user and group called "spring".
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# ── Copy only the fat JAR from the builder stage ──────────────────────────────
COPY --from=builder /build/target/spring-devops-demo.jar app.jar

# Give the spring user ownership of the app directory
RUN chown -R spring:spring /app

# Switch to the non-root user for all subsequent RUN/CMD instructions
USER spring

# ── Document the port the app listens on ─────────────────────────────────────
# This is informational only — the actual port binding happens in docker run / compose.
EXPOSE 8091

# ── Health check ──────────────────────────────────────────────────────────────
# Docker (and ECS/Kubernetes) uses this to know if the container is healthy.
# Polls /actuator/health every 30s; allows 60s startup time; 3 failures = unhealthy.
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8091/actuator/health | grep -q '"status":"UP"' || exit 1

# ── JVM tuning for containers ─────────────────────────────────────────────────
# -XX:+UseContainerSupport   : respect cgroup memory/CPU limits (default on JDK 17+)
# -XX:MaxRAMPercentage=75.0  : use up to 75% of the container's RAM for the heap
# -Djava.security.egd=...    : faster startup by using /dev/urandom for entropy
# Spring profile can be overridden at runtime: docker run -e SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# ── Entrypoint ────────────────────────────────────────────────────────────────
# Shell form so $JAVA_OPTS is expanded.
# Any env vars set at runtime (docker run -e ...) override application.properties.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]