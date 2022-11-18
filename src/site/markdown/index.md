# Run Maven Plugin
Used to runs method from arbitrary classes in Maven artifacts

## Goals Overview
* [run:run](./run-mojo.html) Run a Maven artifact class

## Usage

### Runnable JAR

`mvn com.kerbaya:run-maven-plugin:1.0.0:run -Dartifact=com.myorg:myapp:1.0.0`

Runs the main method of runnable JAR `com.myorg:myapp:1.0.0`

### Specifying main class

`mvn com.kerbaya:run-maven-plugin:1.0.0:run -Dartifact=com.myorg:myapp:1.0.0 -DclassName=com.myorg.myapp.MyMain`

Runs main method `com.myorg.myapp.MyMain` of JAR `com.myorg:myapp:1.0.0`

### Arguments

`mvn com.kerbaya:run-maven-plugin:1.0.0:run -Dartifact=com.myorg:myapp:1.0.0 -Darg.0=zero -Darg.1=one -Darg.2=two`

Runs the main method of runnable JAR `com.myorg:myapp:1.0.0` with command-line arguments: `zero`, `one`, `two`
