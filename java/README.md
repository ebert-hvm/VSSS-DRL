
# Java Client for VSSS League (Very Small Size Soccer League) robot football

This repository implements a java client to interact with the FIRASim simulator (https://github.com/VSSSLeague/FIRASim). It receives and decodes UDP data from FIRASim, implements a basic Deep Q-Learning AI and a basic PID controller and sends velocity commands to the simulator.




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
- Turn off any active firewalls.
- Run FIRASim.
- Modify the "address" parameter on "parameters.json" located at "src/main/java/resources/parameters.json", with the IPv4 or inet address in which FIRASim is running.
- In case you're using Visual Studio Code as the IDE, install the "Extension Pack for Java" and "Maven For Java" extensions, open a folder in the root directory and setup a launch.json. Now you're able to run the maven project.
