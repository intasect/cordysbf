# Introduction #
The main purpose of the build framework is to make the development process of Cordys applications scalable (with more people), distributed (on more machines, in remote locations) and repeatable (predictable result).

As long as a developer works standalone, developing alone and there is no need to transfer this to another system, the default Cordys development environment is sufficient. The moment he/she starts developing in a team and with source control based versioning the build framework is needed. Basic idea behind the build system is transferring the Cordys repository data to files in a source code management system like Subversion. Subversion adds the team development and repeatability to the process.

Finally a build tool (Ant) is added to combine the Cordys content, Java code and web development files into a distributable and tested result. This process can be automated.

The build framework 1.x versions are Cordys C3 based.

# Cordys build and development automation EcoSystem #
For Bop 4.1 CU7.1 and up there is a new Build Framework available: https://wiki.cordys.com/x/gsNsDg