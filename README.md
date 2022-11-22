# Spring Boot 3 buildpacks with Testcontainers Cloud

## Prerequisites

This requires an account on [Testcontainers Cloud](https://www.testcontainers.cloud/)

## Quick Start

```bash
git clone https://github.com/dashaun/spring-boot-3-buildpacks-with-testcontainers-cloud
cd spring-boot-3-buildpacks-with-testcontainers-cloud
docker context use tcc
./mvnw test
docker images | grep local
```