# Run Maven Plugin
Used to runs method from arbitrary classes in Maven artifacts

## Goals Overview
* [run:run](https://www.kerbaya.com/run-maven-plugin/run-mojo.html) Run a Maven artifact class

## Usage

Specify the Maven artifact to run using parameter `artifact`:

`-Dartifact=<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>>`

### Runnable JAR

If the Maven artifact is a runnable JAR, no other parameters are required:

`mvn com.kerbaya:run-maven-plugin:1.1.0:run -Dartifact=com.myorg:myapp:1.0.0`

Runs the main method of runnable JAR `com.myorg:myapp:1.0.0`

### Specifying main class

Specify the main class name using parameter `className`:

`mvn com.kerbaya:run-maven-plugin:1.1.0:run -Dartifact=com.myorg:myapp:1.0.0 -DclassName=com.myorg.myapp.MyMain`

Runs main method `com.myorg.myapp.MyMain` of JAR `com.myorg:myapp:1.0.0`

### Arguments

Main method arguments can be provided using `arg.#` parameters, starting with `arg.0`:

`mvn com.kerbaya:run-maven-plugin:1.1.0:run -Dartifact=com.myorg:myapp:1.0.0 -Darg.0=zero -Darg.1=one -Darg.2=two`

Runs the main method of runnable JAR `com.myorg:myapp:1.0.0` with command-line arguments: `zero`, `one`, `two`

### Options

Java runtime options can be provided using `opt.#` parameters, starting with `opt.0`:

`mvn com.kerbaya:run-maven-plugin:1.1.0:run -Dartifact=com.myorg:myapp:1.0.0 -Dopt.0=-Xms1g -Dopt.1=-Xmx2g -Dopt.2=-Dname=value`

Runs the main method of runnable JAR `com.myorg:myapp:1.0.0` with:

* `1GB` minimum heap size (`-Xms1g`)
* `2GB` maximum heap size (`-Xmx2g`)
* System property `name` set to `value` (`-Dname=value`)
