# Cosmo Cats Marketplace

Spring Boot REST API for an in-memory product catalog.

## Requirements

- JDK 17 or later.
- Maven is provided by the Maven Wrapper.

## Run

```shell
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

The product API is available at `/api/products`. The OpenAPI contract is in
`src/main/resources/api-specs/consumer-api-spec.yaml`.

## Tests and coverage

```shell
./mvnw clean verify
```

Tests follow the Maven layout in `src/test/java`, mirroring the application
packages in `src/main/java`:

- Service unit tests use JUnit Jupiter, Mockito, and AssertJ without starting Spring.
- MVC tests use `@WebMvcTest`, `MockMvc`, and `@MockitoBean` to verify HTTP responses,
  request validation, pagination boundaries, and error responses.
- Repository, mapper, cart, product, and order tests exercise their real implementations.
- The application test verifies a complete product lifecycle with the real Spring beans.

JaCoCo runs with the tests and generates HTML, XML, and CSV reports during
`verify`. The build requires at least 80% line coverage and 80% branch coverage
across the application, without package or class exclusions.

- Coverage report: `target/site/jacoco/index.html`.
- Test results: `target/surefire-reports`.
- Run a test class: `./mvnw -Dtest=ProductServiceTest test`.

Mockito is configured as a JVM agent alongside JaCoCo, avoiding dynamic agent
attachment during tests.

If Maven cannot remove an existing `target` directory on Windows, stop any
process using the build output, then run from the project root in PowerShell:

```powershell
Remove-Item -LiteralPath .\target -Recurse -Force
.\mvnw.cmd clean verify
```

## Continuous integration

The CI workflow runs `clean verify` on Temurin 17 and 25 for pull requests,
pushes to `main` and `lab2`, and manual runs. Test and coverage reports are
uploaded separately for each Java version and retained for 14 days.
