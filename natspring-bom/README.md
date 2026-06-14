# Natspring BOM

[![Sonatype](https://img.shields.io/maven-central/v/io.github.malczuuu.natspring/natspring-bom)][maven-central]

Bill of Materials for the Natspring project. Import it to align versions of all Natspring modules without specifying
each version individually.

## Dependency

Gradle:

```kotlin
dependencies {
    implementation(platform("io.github.malczuuu.natspring:natspring-bom:{version}"))
    
    implementation("io.github.malczuuu.natspring:natspring-autoconfigure")
    implementation("io.github.malczuuu.natspring:natspring-core")
    implementation("io.github.malczuuu.natspring:natspring-namastack-outbox")
    implementation("io.github.malczuuu.natspring:natspring-starter")

    implementation("io.github.amadeusitgroup.testcontainers:nats")
    implementation("io.micrometer:micrometer-core")
    implementation("io.nats:jnats")
    implementation("org.jspecify:jspecify")
    implementation("org.slf4j:slf4j-api")
    implementation("tools.jackson.core:jackson-databind")

    testImplementation("io.github.malczuuu.natspring:natspring-starter-test")
}
```

Maven:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.malczuuu.natspring</groupId>
            <artifactId>natspring-bom</artifactId>
            <version>{version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
<dependencies>
    <dependency>
        <groupId>io.github.malczuuu.natspring</groupId>
        <artifactId>natspring-autoconfigure</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.malczuuu.natspring</groupId>
        <artifactId>natspring-core</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.malczuuu.natspring</groupId>
        <artifactId>natspring-namastack-outbox</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.malczuuu.natspring</groupId>
        <artifactId>natspring-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.malczuuu.natspring</groupId>
        <artifactId>natspring-starter</artifactId>
    </dependency>
    
    <dependency>
        <groupId>io.github.amadeusitgroup.testcontainers</groupId>
        <artifactId>nats</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-core</artifactId>
    </dependency>
    <dependency>
        <groupId>io.nats</groupId>
        <artifactId>jnats</artifactId>
    </dependency>
    <dependency>
        <groupId>org.jspecify</groupId>
        <artifactId>jspecify</artifactId>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
    <dependency>
        <groupId>tools.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <dependency>
        <groupId>io.github.malczuuu.natspring</groupId>
        <artifactId>natspring-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

[maven-central]: https://central.sonatype.com/artifact/io.github.malczuuu.natspring/natspring-bom
