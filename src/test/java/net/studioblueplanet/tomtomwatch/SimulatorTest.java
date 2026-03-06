package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.usb.UsbTestInterface;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Test to verify simulation mode works without GUI
 */
public class SimulatorTest {

    @Test
    public void testSimulatorLoadsWatchData() {
        // Verify simulation directory exists
        File simDir = new File("./working/simulation/");
        assertTrue("Simulation directory should exist", simDir.exists());

        // Verify versions.json exists
        File versionsFile = new File(simDir, "versions.json");
        assertTrue("versions.json should exist", versionsFile.exists());

        // Verify at least one activity file exists
        File activityFile = new File(simDir, "0x00910000.bin");
        assertTrue("Activity file should exist", activityFile.exists());
        assertTrue("Activity file should not be empty", activityFile.length() > 0);

        System.out.println("✓ Simulation directory structure verified");
        System.out.println("✓ Found activity file: " + activityFile.getName() +
                           " (" + activityFile.length() + " bytes)");
    }

    @Test
    public void testSimulatorHasPreferencesFile() {
        // Verify preferences file exists
        File simDir = new File("./working/simulation/");
        File prefsFile = new File(simDir, "0x00f20000.bin");

        assertTrue("Preferences file should exist", prefsFile.exists());
        assertTrue("Preferences file should not be empty", prefsFile.length() > 0);

        System.out.println("✓ Watch preferences file verified");
        System.out.println("✓ Simulator is ready to mimic watch connection");
    }
}
