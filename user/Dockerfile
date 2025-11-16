# Build stage: use Temurin 21 and install Maven so we can build inside the image
FROM eclipse-temurin:21-jdk AS build

ARG MAVEN_VERSION=3.10.1
ARG MAVEN_HOME=/opt/maven

# install a small set of tools and Maven binary
RUN apt-get update && apt-get install -y --no-install-recommends \
		curl ca-certificates tar gzip && \
		mkdir -p /opt && \
		curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
			| tar -xz -C /opt && \
		mv /opt/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
		ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn && \
		rm -rf /var/lib/apt/lists/*

WORKDIR /src
# copy only pom first for dependency caching
COPY pom.xml mvnw* ./
COPY .mvn .mvn
RUN mvn -B -DskipTests dependency:go-offline

# copy sources and build
COPY src ./src
RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# create non-root user
RUN useradd --create-home --shell /bin/bash appuser

# copy jar from build stage
COPY --from=build /src/target/*.jar app.jar
RUN chown appuser:appuser /app/app.jar
USER appuser

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]