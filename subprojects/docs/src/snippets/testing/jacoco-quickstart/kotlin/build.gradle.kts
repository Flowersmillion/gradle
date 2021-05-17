// tag::apply-plugin[]
plugins {
    // end::apply-plugin[]
    java
// tag::apply-plugin[]
    jacoco
}
// end::apply-plugin[]

// tag::jacoco-configuration[]
jacoco {
    toolVersion = "0.8.6"
    reportsDirectory.set(layout.buildDirectory.dir("customJacocoReportDir"))
}
// end::jacoco-configuration[]

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.+")
}

// tag::testtask-configuration[]
tasks.test {
    extensions.configure(JacocoTaskExtension::class) {
        destinationFile = layout.buildDirectory.file("jacoco/jacocoTest.exec").get().asFile
        classDumpDir = layout.buildDirectory.dir("jacoco/classpathdumps").get().asFile
    }
}
// end::testtask-configuration[]

// tag::testtask-dependency[]
tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}
// end::testtask-dependency[]

// tag::report-configuration[]
tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}
// end::report-configuration[]

// tag::violation-rules-configuration[]
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.5".toBigDecimal()
            }
        }

        rule {
            enabled = false
            element = "CLASS"
            includes = listOf("org.gradle.*")

            limit {
                counter = "LINE"
                value = "TOTALCOUNT"
                maximum = "0.3".toBigDecimal()
            }
        }
    }
}
// end::violation-rules-configuration[]

// tag::testtask-configuration-defaults[]
tasks.test {
    configure<JacocoTaskExtension> {
        isEnabled = true
        destinationFile = layout.buildDirectory.file("jacoco/${name}.exec").get().asFile
        includes = emptyList()
        excludes = emptyList()
        excludeClassLoaders = emptyList()
        isIncludeNoLocationClasses = false
        sessionId = "<auto-generated value>"
        isDumpOnExit = true
        classDumpDir = null
        output = JacocoTaskExtension.Output.FILE
        address = "localhost"
        port = 6300
        isJmx = false
    }
}
// end::testtask-configuration-defaults[]
