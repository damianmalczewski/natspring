# Natspring BOM

[![Sonatype](https://img.shields.io/maven-central/v/io.github.malczuuu.natspring/natspring-bom)][maven-central]

Bill of Materials for the Natspring project. Import it to align versions of all Natspring modules without specifying
each version individually.

## Dependency

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
        <artifactId>natspring-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.malczuuu.natspring</groupId>
        <artifactId>natspring-starter</artifactId>
    </dependency>

    <dependency>
        <groupId>io.github.malczuuu.natspring</groupId>
        <artifactId>natspring-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

```kotlin
dependencies {
    implementation(platform("io.github.malczuuu.natspring:natspring-bom:{version}"))
    
    implementation("io.github.malczuuu.natspring:natspring-autoconfigure")
    implementation("io.github.malczuuu.natspring:natspring-core")
    implementation("io.github.malczuuu.natspring:natspring-starter")

    testImplementation("io.github.malczuuu.natspring:natspring-starter-test")
}
```

[maven-central]: https://central.sonatype.com/artifact/io.github.malczuuu.natspring/natspring-bom
