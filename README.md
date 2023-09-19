# TomTomWatch

## Introduction
Tool for exporting GPS files from TomTom Sports Watches

Homepage: http://blog.studioblueplanet.net/?page_id=566

TomTomWatch is a tool for downloading GPS tracks from the TomTom Sports watches. It downloads the ttbin files and extracts the raw GPS data.
I also offers track smoothing, when enabled.

It contains a lot of features for maintaining the watch, like
For the more technical users it contains a Debugging menu that offers functions for file manipulation, resetting, rebooting, etc.

The software will be tested primarily on Windows. On request it can be tested on Linux (Centos). Though the USB4Java lib that is being used should support it, the software will *not* be tested on OS-X.

## Building
It uses Maven for building. Use a development environment like Netbeans or Eclipse.

### Notes
- Be sure javac and mvn are in the PATH and JAVA_HOME is set
- Build `mvn -Dmaven.test.skip package`

## Configuring
After building the target directory contains tomtomwatch.properties. In Netbeans you can use this file in the TomTomWatch root directory
during development and debugging.
The file is self-explanatory

## Disclaimer
Use the software at own risk. If not used properly, especially the debugging features, I guess it might brick your watch
(though it never happened to me so far...).

## License
This software is published under the MIT license:

Copyright (c) 2023 Jörgen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
