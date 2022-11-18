# Run Maven Plugin
Runs methods from arbitrary classes in Maven artifacts

## Goals Overview
* [run:run](./run-mojo.html) Runs a method from an arbitrary class in a Maven artifact

## Usage
`mvn com.kerbaya:run-maven-plugin:1.0.0:run -Dartifact=com.myorg:myapp:1.0.0`

Runs the main method of runnable JAR `com.myorg:myapp:1.0.0`
