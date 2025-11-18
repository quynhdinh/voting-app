

# Docker Image Build and Publish Guide

This project includes a centralized Makefile to build and publish Docker images for all services.

## Services

- `user:v1.0.0` – User service (port 8084)  
- `vote:v1.0.0` – Voting service (port 8082)  
- `result:v1.0.0` – Result service (port 8083)  
- `contest:v1.0.0` – Contest service (port 8081)

## Using the Makefile

### Build all images

```bash
make build-all
```

This will build all images with version `v1.0.0` and also create `latest` tags.

### Build a specific image

```bash
make build SERVICE=user
make build SERVICE=vote
make build SERVICE=result
make build SERVICE=contest
```

Or use the direct targets:

```bash
make user
make vote
make result
make contest
```

### Publish images to a registry

First, set the registry in the `REGISTRY` variable:

```bash
# For Docker Hub
make push-all REGISTRY=docker.io/your-user/

# For GitHub Container Registry
make push-all REGISTRY=ghcr.io/your-user/

# For a private registry
make push-all REGISTRY=your-registry.com/
```

Or publish a specific image:

```bash
make push SERVICE=user REGISTRY=docker.io/your-user/
```

### Use a different version

```bash
make build-all VERSION=v1.1.0
make push-all VERSION=v1.1.0 REGISTRY=docker.io/your-user/
```

### Clean local images

```bash
make clean
```

### View help

```bash
make help
```

## Full Examples

### Build all images locally

```bash
make build-all
```

### Build and publish to Docker Hub

```bash
# Make sure you're authenticated: docker login
make push-all REGISTRY=docker.io/your-user/
```

### Build and publish to GitHub Container Registry

```bash
# Make sure you're authenticated:
# echo $GITHUB_TOKEN | docker login ghcr.io -u your-user --password-stdin
make push-all REGISTRY=ghcr.io/your-user/
```

### Build a new version

```bash
make build-all VERSION=v2.0.0
make push-all VERSION=v2.0.0 REGISTRY=docker.io/your-user/
```

## Image Structure

All Docker images:

- Use multi-stage builds to optimize size  
- Are based on `eclipse-temurin:21-jdk-jammy`  
- Run as a non-root user (`appuser`)  
- Include Maven dependencies in the build stage  
- Expose each service's corresponding ports

## Verification

After building, you can verify the images:

```bash
docker images | grep -E "user|vote|result|contest"
```

And test an image:

```bash
docker run -p 8081:8081 contest:v1.0.0
```