
Gatling Gradle Plugin
=====================

This plugin allows the use of the Gatling load testing tool (written in Scala) from within Gradle.

# Configuration

The configuration elements within the gradle scripts is below:

```
gatling {
    list true
    dryRun false
    include "com.yourcompany.package.scenario.*"
    exclude ".*"
}
```

The semantics of each element is:

* list: Print out the list of classes found and whether each will be run.
* dryRun: If true, do not run the scenarios found.
* include: Java regex for scenarios to include.
* exclude: Java regex for scenarios to exclude.

All scenarios are run except those _excluded_ that are also not _included_. This is equivalent to the following statuses:
* included only: executed
* exclude only: not executed
* none: executed
* both: executed

# Tasks

The plugin adds a _gatlingRun_ task that run scenarios.  You can make the _test_ task depend on the _gatlingRun_ task as per the example below. 

# Example

An example build.gradle file is below:

```
buildscript {
    repositories {
        mavenCentral()
        maven { url 'http://repository.excilys.com/content/groups/public' }
        maven { url "https://oss.sonatype.org/content/groups/public" }
    }
    dependencies {
        classpath "com.github.mperry:gradle-gatling-plugin:0.1-SNAPSHOT"
    }
}

apply plugin: 'scala'
apply plugin: "com.github.mperry.gatling"

defaultTasks "build"

ext {
    gatlingVersion = "1.5.6"
}

repositories {
    mavenCentral()
    maven { url 'http://repository.excilys.com/content/groups/public' }
    maven { url "https://oss.sonatype.org/content/groups/public" }
    mavenLocal()
}


gatling {
    list true
    dryRun false
    include "com.yourcompany.scenario.*" // include all simulations in this package
    exclude ".*"
}

test.dependsOn gatlingRun

dependencies {
    compile "com.github.mperry:gradle-gatling-plugin:0.1-SNAPSHOT"
}

```

# License 

Licensed under [Apache Version 2](https://www.apache.org/licenses/LICENSE-2.0.html).
