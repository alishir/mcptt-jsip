# mcptt-jsip
MCPTT service using jain-sip stack.

# Prerequisites
1. Install JDK1.8:
```sh
$ sudo apt-get update
$ sudo apt-get install openjdk-8-jdk
```
2. There's **no** need to install Gradle as it's automatically downloaded and installed after running the instructions in the below section.

# Running the project
This project depends on JDK1.8 (`openjdk-8-jdk`, if you want to install OpenJDK), we couldn't use latest version of JDK because jain-sip is based on JDK1.8.

In order to run the project execute `./gradlew run` in the root of project. After successfully executing the command, you should see a "80% EXECUTING > :run" message in the output, where a timer is also ticking.

**The instructions have been tested on Debian 4.18.10 with OpenJDK 1.8 (openjdk-8-jdk).**
