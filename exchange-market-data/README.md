# exchange-quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/exchange-quarkus-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

- Messaging - RabbitMQ Connector ([guide](https://quarkus.io/guides/rabbitmq)): Connect to RabbitMQ with Reactive
  Messaging
- YAML Configuration ([guide](https://quarkus.io/guides/config-yaml)): Use YAML to configure your Quarkus application
- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code
  for Hibernate ORM via the active record or the repository pattern
- Logging JSON ([guide](https://quarkus.io/guides/logging#json-logging)): Add JSON formatter for console logging
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### YAML Config

Configure your application with YAML

[Related guide section...](https://quarkus.io/guides/config-reference#configuration-examples)

The Quarkus application configuration is located in `src/main/resources/application.yml`.

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)

`{resource=Resource{schemaUrl=null, attributes={host.name="pop-os", service.name="exchange", service.version="1.0-SNAPSHOT", telemetry.sdk.language="java", telemetry.sdk.name="opentelemetry", telemetry.sdk.version="1.39.0", webengine.name="Quarkus", webengine.version="3.15.1"}}, instrumentationScopeInfo=InstrumentationScopeInfo{name=io.opentelemetry.sdk.trace, version=null, schemaUrl=null, attributes={}}, name=processedSpans, description=The number of spans processed by the BatchSpanProcessor. [dropped=true if they were dropped due to high throughput], unit=1, type=LONG_SUM, data=ImmutableSumData{points=[ImmutableLongPointData{startEpochNanos=1730272316733736587, epochNanos=1730272436737033718, attributes={dropped=false, processorType="BatchSpanProcessor"}, value=3459, exemplars=[]}], monotonic=true, aggregationTemporality=CUMULATIVE}}
`{resource=Resource{schemaUrl=null, attributes={host.name="pop-os", service.name="exchange", service.version="1.0-SNAPSHOT", telemetry.sdk.language="java", telemetry.sdk.name="opentelemetry", telemetry.sdk.version="1.39.0", webengine.name="Quarkus", webengine.version="3.15.1"}}, instrumentationScopeInfo=InstrumentationScopeInfo{name=io.opentelemetry.sdk.trace, version=null, schemaUrl=null, attributes={}}, name=queueSize, description=The number of items queued, unit=1, type=LONG_GAUGE, data=ImmutableGaugeData{points=[ImmutableLongPointData{startEpochNanos=1730272316733736587, epochNanos=1730272436737033718, attributes={processorType="BatchSpanProcessor"}, value=150, exemplars=[]}]}}
