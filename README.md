# TomTomWatch

## Introduction
Tool for exporting GPS files from TomTom Sporexts Watches

Homepage: http://blog.studioblueplanet.net/?page_id=566

TomTomWatch is a tool for downloading GPS tracks from the TomTom Sports watches. It downloads the ttbin files and extracts the raw GPS data.
I also offers track smoothing, when enabled.

It contains a lot of features for maintaining the watch, like 
For the more technical users it contains a Debugging menu that offers functions for file manipulation, resetting, rebooting, etc.

## Building
It uses Maven for building. Use a development environment like Netbeans or Eclipse. 

## Configuring
After building the target directory contains tomtomwatch.properties. In Netbeans you can use this file in the TomTomWatch root directory 
during development and debugging.
The file is self-explanatory

## Disclaimer
Use the software at own risk. If not used properly, especially the debugging features, I guess it might brick your watch 
(though it never happened to me so far...). 
