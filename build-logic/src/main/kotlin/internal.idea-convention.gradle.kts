import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.Application

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext")
}

idea {
    project {
        settings {
            runConfigurations {
                create<Gradle>("Gradle Clean [natspring]") {
                    taskNames = listOf("clean")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Gradle>("Gradle Build [natspring]") {
                    taskNames = listOf("spotlessApply build")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Gradle>("Gradle Format Code [natspring]") {
                    taskNames = listOf("spotlessApply")
                    projectPath = rootProject.rootDir.absolutePath
                }

                // pre-configured run configurations for example applications
                create<Application>("Run [example-event-driven-telemetry]") {
                    moduleName = "natspring.examples.example-event-driven-telemetry.main"
                    mainClass = "org.example.natspring.telemetry.TelemetryApplication"
                }
                create<Application>("Run [example-jetstream]") {
                    moduleName = "natspring.examples.example-jetstream.main"
                    mainClass = "org.example.natspring.jetstream.JetStreamListenerExample"
                }
                create<Application>("Run [example-jetstream-deadletter]") {
                    moduleName = "natspring.examples.example-jetstream-deadletter.main"
                    mainClass = "org.example.natspring.jetstreamdeadletter.JetStreamDeadLetterExample"
                }
                create<Application>("Run [example-listener]") {
                    moduleName = "natspring.examples.example-listener.main"
                    mainClass = "org.example.natspring.listener.NatsListenerExample"
                }
                create<Application>("Run [example-listener-deadletter]") {
                    moduleName = "natspring.examples.example-listener-deadletter.main"
                    mainClass = "org.example.natspring.listenerdeadletter.ListenerDeadLetterExample"
                }
                create<Application>("Run [example-request]") {
                    moduleName = "natspring.examples.example-request.main"
                    mainClass = "org.example.natspring.request.RequestReplyExample"
                }
            }
        }
    }
}
