# TomTomWatch

## Introduction
Tool for exporting GPS files from TomTom Sports Watches

Homepage: http://blog.studioblueplanet.net/?page_id=566

TomTomWatch is a tool for downloading GPS tracks from the TomTom Sports watches. It downloads the ttbin files and extracts the raw GPS data.
I also offers track smoothing, when enabled.

It contains a lot of features for maintaining the watch, like
For the more technical users it contains a Debugging menu that offers functions for file manipulation, resetting, rebooting, etc.

## Prerequisites

The software will be tested primarily on **Windows**. On request it can be tested on Linux (Centos). Though the USB4Java lib that is being used should support it, the software will *not* be tested on OS-X.

The software requires a HD display size of a least **1920x1080 pixels**. It may not fit on smaller screens.

## Installation and Running

### Prerequisites
- **Java 8 or later** — download from [java.com](https://www.java.com/download/) or [OpenJDK](https://openjdk.org/)
- Verify with: `java -version`

### Step 1 — Download the application
Go to the [Releases page](https://github.com/ravi-bytes/TomTomWatch/releases) and download:
- `TomTomWatch.jar` — the application (all dependencies included)
- `tomtomwatch.properties` — the configuration file (also in this repo root)

### Step 2 — Place files in the same folder
```
my-folder/
  TomTomWatch.jar
  tomtomwatch.properties
```

### Step 3 — Run
```bash
java -jar TomTomWatch.jar
```
On Windows you can also double-click `TomTomWatch.jar` if Java is associated with `.jar` files.

## Disclaimer
**Though I have used the software myself for years and I have tested it thorougly, usage might damage your watch. The software was created without help or specifications of TomTom, so it may contain unforseen issues. This particularly holds for the features under the 'Debugging' menu, which should be regaded as experimental.**

When encountering problems don't forget to read the FAQ below.

## Strava Integration

Sync your TomTom activities directly to Strava from the **File → Sync to Strava** menu.

### Step 1 — Create a Strava API application
1. Go to [strava.com/settings/api](https://www.strava.com/settings/api)
2. Create an application (name and website can be anything)
3. Set **Authorization Callback Domain** to `localhost`
4. Note your **Client ID** and **Client Secret**

### Step 2 — Create a `.env` file
Create a file named `.env` in the same folder as `TomTomWatch.jar`:
```
STRAVA_CLIENT_ID=your_client_id
STRAVA_CLIENT_SECRET=your_client_secret
STRAVA_ACCESS_TOKEN=
STRAVA_REFRESH_TOKEN=
STRAVA_TOKEN_EXPIRES_AT=0
```
Leave the token fields blank — they are filled in automatically on first use.

### Step 3 — Sync activities
1. Download activities from your watch as normal
2. Click **File → Sync to Strava**
3. On first run: follow the OAuth prompt to authorize the app in your browser
4. Select the date range to sync
5. The app uploads new activities and skips duplicates automatically

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

## FAQ

### What do I need to run the software?
You need a Java Runtime Environment or JRE to run the .jar file. Version 8 will do. Download at [java.com](https://www.java.com/download/ie_manual.jsp) or [OpenJDK](https://openjdk.org/). Read instructions on my [blog](https://blog.studioblueplanet.net/software/tomtomwatch).

### What do I need for building the software?
You need a Java SDK. Version 8. Download at [java.com](https://www.java.com/download/ie_manual.jsp) or [OpenJDK](https://openjdk.org/).
For building you need Maven. For developing I use Netbeans SDE. But use any SDE you like...

### Does the softare run under Linux?
Yes it does, though I mainly test it on Windows.
On linux the user should have the rights to use the USB port. Also the USB devices should be registered under /etc/udev/rules.d/.
Refer to [ttwach](https://github.com/ryanbinns/ttwatch) for the steps to take or browse the internet for your distro.

### Does the software run on Apple?
I've reports it does. I do not support it, since I don't have Apple stuff.

### When I choose 'Activity summary' I see my tracks, but I cannot download them
If you cannot download tracks, then there are no **track files** on the watch.
They get downloaded _and deleted_ when you connnected to TomTom MySports. The 'Activity Summary' shows the last 10 logged activities from the history files. 'Activity History' shows a summary of logged activities from summary files.
To check: 
1. Go outside
2. Log an activity
3. Connect watch tot TomTomWatch and press 'Download': the track should be downloaded

### My watch gets slow
When you log more and more activities, the watch appears to start responding slowly when starting a new activity. 
Erasing the track files by pressing the 'Erase' button solves the problem. This erases the track files.
Unlike TomTom MySports, TomTomWatch does not automatically erase all tracks when downloaded. You have to do it manually.

### Upload Workout from JSON is greyed out
Upload Workouts from JSON is only supported for a number of types of TomTom watches: it is only supported for watches that I have tested.
Note that it is an _experimental_ feature. Therefore it is under the debugging menu. I can imagine you might damage your watch when you've errors in the JSON.

### What does 'Sync time offset' mean?
The watch synchronizes its time to the GPS satellites. So going outside and starting to log an activity is sufficient to synchronize the clock. However, depending on your time zone or daylight saving period you might want to update the watch accordingly. For this you can use tye 'Sync Time Offset' feature. It takes the offset from your PC and asks you if it should update the watch accordingly. So: go outside, sync the watch and than use this option.

### There is no height value in the GPX files?
TomTom watches do as far as I know not register heigth, apart from the Adventurer.
TomTomWatch can add height to the GPX files by reading the height from a Google service based on the coordinate. See [my blog](https://blog.studioblueplanet.net/software/tomtomwatch).
Set heightService=google and fill in the API key in heightServiceKey in the properties file tomtomwatch.properties.

### The Watch serial number is not shown?
Known issue. Replace the USB driver by 'WinUSB' using the [Zadig tool](https://zadig.akeo.ie/). Only do this for the Watch. Beware that other programs using the watch won't work anymore.

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
