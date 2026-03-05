# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TomTomWatch is a Java Swing desktop application for downloading GPS tracks from TomTom Sports watches. It downloads ttbin files, extracts raw GPS data, offers track smoothing, and provides watch maintenance features including debugging functions for file manipulation, resetting, and rebooting.

**Primary platforms**: Windows (primary), Linux (Centos on request)
**Display requirement**: Minimum 1920x1080 pixels
**Warning**: Features under the 'Debugging' menu are experimental and may damage the watch if used improperly

## Technology Stack

- **Java**: Version 8 (Java 1.8)
- **Build tool**: Maven
- **UI framework**: Swing (org.jdesktop.application framework)
- **USB communication**: usb4java / usb4java-javax (1.3.0)
- **GPS/XML formats**: GPX and TCX output support
- **Protobuf**: Google Protocol Buffers for binary formats
- **Testing**: JUnit with PowerMock and Mockito
- **Map integration**: JXMapViewer2 (OpenStreetMap and Google Maps support)

## Build Commands

### Prerequisites
- Ensure `javac` and `mvn` are in PATH
- Set `JAVA_HOME` environment variable
- Maven must be installed (not currently in PATH on this machine)

### Core Commands

```bash
# Build (skip tests)
mvn -Dmaven.test.skip package

# Build with tests
mvn clean package

# Install dependencies and build
mvn clean install

# Run tests only
mvn test

# Generate Javadoc
mvn javadoc:jar
```

### Build Artifacts
- **JAR location**: `target/TomTomWatch.jar` (assembled with dependencies)
- **Dependencies**: Copied to `target/lib/`
- **Configuration**: `target/tomtomwatch.properties` (copied from `src/main/resources_run/`)

## Running the Application

```bash
# Run the assembled JAR
java -jar target/TomTomWatch.jar

# Development mode (in Netbeans or Eclipse)
# Use tomtomwatch.properties in project root for configuration
```

## Architecture

### Package Structure

The codebase is organized into six main packages:

1. **`net.studioblueplanet.tomtomwatch`** - Main application and UI layer
   - `TomTomWatch.java`: Main application entry point (extends SingleFrameApplication)
   - `TomTomWatchView.java`: Primary UI view controller
   - `CommunicationProcess.java`: Coordinates watch communication in separate thread
   - `ActivityData.java`: Represents downloaded GPS activities
   - `GpxReader.java`: Reads GPX files for route planning
   - Map implementations: `MapOsm.java`, `MapGoogle.java`
   - Workout/Race/Route management classes
   - Protocol Buffer generated classes: `RouteProto.java`, `TrackerProto.java`, `WorkoutProto.java`

2. **`net.studioblueplanet.usb`** - USB communication layer
   - `UsbInterface.java`: High-level watch communication interface
   - `UsbConnection.java`: Low-level USB connection handling
   - `WatchInterface.java`: Abstract interface for watch operations
   - `UsbFile.java`: Represents files on the watch
   - `UsbPacket.java`: USB protocol packet structure
   - `UsbTestInterface.java`: Simulation/testing interface

3. **`net.studioblueplanet.ttbin`** - TomTom binary file format parsing
   - `TomTomReader.java`: Reads ttbin files from watch (singleton)
   - `Activity.java`: Represents a parsed activity
   - `ActivityRecord.java`, `ActivitySegment.java`: Activity data structures
   - `GpxWriter.java`, `TcxWriter.java`: Export to GPS formats
   - `TrackSmoother.java`: Kalman filter for GPS track smoothing
   - `TtbinFileDefinition.java`, `TtbinHeader.java`: Binary format definitions

4. **`net.studioblueplanet.generics`** - Utility classes
   - `ToolBox.java`: General utility methods
   - `PolyLineEncoder.java`: Google Polyline encoding
   - Executor implementations: `DirectExecutor`, `SerialExecutor`, `ThreadExecutor`
   - `GitBuildInfo.java`: Build information from Git

5. **`net.studioblueplanet.settings`** - Configuration management
   - `ConfigSettings.java`: Loads and manages tomtomwatch.properties

6. **`net.studioblueplanet.logger`** - Logging
   - `DebugLogger.java`: Application logging with configurable levels

### Key Design Patterns

- **Singleton**: `TomTomReader` uses singleton pattern for ttbin file reading
- **Observer/Listener**: `ProgressListener` interface for async USB operations
- **Dependency Injection**: `DependencyInjector.java` for testing/simulation mode
- **Thread Management**: Separate thread for watch communication via `CommunicationProcess`
- **Strategy Pattern**: Multiple map providers (OSM, Google) via `Map` interface

### Configuration System

The application is configured via `tomtomwatch.properties`:
- Debug levels: off, error, info, debug
- File paths: GPX output, ttbin files, routes, debug files
- Track processing: smoothing (Kalman filter), compression (Douglas-Peucker)
- Height service: Google Elevation API integration
- Map service: OpenStreetMap or Google Maps
- GPS Quick Fix: Ephemeris file configuration
- Export formats: GPX or TCX with optional Garmin/custom extensions

**Important**: After building, `target/tomtomwatch.properties` must be configured. During development in Netbeans, use the file in the project root.

## Testing

### Test Structure
- Tests located in `src/test/java/` mirroring main package structure
- Uses JUnit with PowerMock and Mockito for mocking
- Test configuration: `src/test/resources/tomtomwatch.properties`

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TomTomReaderTest

# Run with specific test method
mvn test -Dtest=TomTomReaderTest#testReadActivity
```

## USB Communication Protocol

The watch communication uses a custom protocol over USB:
- Commands: Open file (read/write), delete file, read/write data, get file size, format watch, reset device, etc.
- File types identified by IDs: 0x00F20000 (preferences), 0x00910000-0x00920000 (activities), etc.
- Packet-based communication via `UsbPacket`
- Progress monitoring via `ProgressListener` callbacks

## Development Notes

### Simulation Mode
Set `simulation=true` in properties file to test without a physical watch. Simulation files stored in `simulationPath` directory.

### Debugging Menu
Controlled by `debuggingMenu=false` in properties. Contains experimental features for advanced users that may damage the watch.

### Protocol Buffer Files
Three generated files are excluded from Javadoc:
- `RouteProto.java`
- `TrackerProto.java`
- `WorkoutProto.java`

### Git Build Information
The maven plugin `git-commit-id-plugin` generates `build.properties` with Git metadata at build time.

## Common Development Tasks

### Adding a new export format
1. Create writer class in `net.studioblueplanet.ttbin` (see `GpxWriter`, `TcxWriter`)
2. Implement format-specific track/waypoint writing
3. Add format option to `exportFileFormat` in properties
4. Update UI to offer new format

### Modifying USB protocol
1. Update command constants in `UsbInterface.java`
2. Modify packet handling in `UsbConnection.java`
3. Test in simulation mode before testing with real watch

### Adding watch maintenance features
1. Add command method to `WatchInterface.java`
2. Implement in `UsbInterface.java`
3. Add UI controls in `TomTomWatchView.java`
4. Connect via `CommunicationProcess.java` threading

## License

MIT License - Copyright (c) 2023 Jörgen
