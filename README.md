
# Java Client for VSSS League (Very Small Size Soccer League) robot football

This repository implements a java client to interact to FIRASim simulator (https://github.com/VSSSLeague/FIRASim). It receives and decode UDP data from FIRASim, implements a basic AI and basic PID controller and sends velocity commands to the simulator.




## Authors

- [@ebert-hvm](https://www.github.com/ebert-hvm)
- [@IvoLinux](https://www.github.com/IvoLinux)
- [@mpdscamp](https://www.github.com/mpdscamp)


## Requirements
- Java 1.8.x
- Apache Maven 3.9.4
- [FIRASim](https://github.com/VSSSLeague/FIRASim/blob/master/INSTALL.md)
## Usage
### Compilation
Open the command line and execute:
```
mvn clean install
```
This will generate the protobuf classes needed and build the project. 

### Running the program
- Turn off any firewalls active
- Run FIRASim
- Modify the "address" parameter on the "parameters.json" located at "src/main/java/resources/parameters.json" with the IPv4 or inet address in which FIRASim is running
- In case your using Visual Studio Code as IDE, install the "Extension Pack for Java" and "Maven For Java" extensions, open a folder in the root directory and setup a launch.json. Now you're able to run the maven project.