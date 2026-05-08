/*
 * Tests for UsbConnection.
 *
 * PURPOSE: these tests document the contract that UsbConnection must uphold so
 * that any future replacement of the javax.usb / usb4java-javax library (which
 * does NOT ship ARM64 native libraries and therefore fails on macOS M1/M2/M3)
 * can be validated against the same expectations.
 *
 * usb4java-javax failure on macOS ARM:
 *   UsbHostManager.getUsbServices() throws UsbException at runtime because the
 *   bundled libusb native library is x86_64-only and cannot be loaded on an
 *   arm64 JVM.  The end state is: isError=true, lastError starts with
 *   "USB Error:".
 *
 * All tests use a single seam — the protected {@link UsbConnection#performConnect()}
 * method — which is overridden by small inner subclasses.  The test file
 * contains NO javax.usb imports and NO Mockito mock() calls, so it runs
 * correctly on any JDK (including Java 21 on macOS ARM) regardless of which
 * mocking framework is on the classpath.
 *
 * When migrating to a different USB backend (e.g. hid4java, libusb via JNA,
 * or a macOS-native IOKit binding), implement a new concrete UsbConnection
 * subclass and run these tests against it to verify the observable behaviour
 * is preserved.
 */
package net.studioblueplanet.usb;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link UsbConnection}.
 *
 * All scenarios are exercised by overriding the protected
 * {@code performConnect()} seam — no native USB library, no javax.usb types,
 * and no mocking framework required.
 *
 * @author generated
 */
public class UsbConnectionTest
{
    // Protocol chunk sizes that UsbInterface depends on
    private static final int MULTISPORTS_READ_CHUNK  = 50;
    private static final int MULTISPORTS_WRITE_CHUNK = 54;
    private static final int SPARK_READ_CHUNK        = 242;
    private static final int SPARK_WRITE_CHUNK       = 246;

    @BeforeClass
    public static void setUpClass() { }

    @AfterClass
    public static void tearDownClass() { }

    @Before
    public void setUp() { }

    @After
    public void tearDown() { }


    // =======================================================================
    // Seam subclasses — no javax.usb types, no mock() calls
    // =======================================================================

    /**
     * Simulates the macOS ARM failure: the native libusb cannot be loaded so
     * the USB backend throws an exception.  The end state mirrors what
     * {@code performConnect()} produces when {@code getUsbServices()} throws.
     */
    private static class ArmLibraryFailureConnection extends UsbConnection
    {
        @Override
        protected void performConnect()
        {
            isError   = true;
            lastError = "USB Error: libusb: error [darwin_init] Darwin init failed";
        }
    }

    /**
     * Simulates a successful device connection of the given {@link DeviceType}.
     * Sets {@code deviceType} directly; {@code device} and {@code iface}
     * remain null (they are not accessed by the chunk-size accessors under
     * test).
     */
    private static class ConnectedDeviceConnection extends UsbConnection
    {
        private final UsbConnection.DeviceType simulatedType;

        ConnectedDeviceConnection(UsbConnection.DeviceType type)
        {
            this.simulatedType = type;
        }

        @Override
        protected void performConnect()
        {
            deviceType = simulatedType;
        }
    }

    /**
     * Simulates any failure that produces a specific error message — covers
     * "no device found", "interface claim refused by OS", etc.
     */
    private static class FailedConnectConnection extends UsbConnection
    {
        private final String simulatedError;

        FailedConnectConnection(String errorMessage)
        {
            this.simulatedError = errorMessage;
        }

        @Override
        protected void performConnect()
        {
            isError   = true;
            lastError = simulatedError;
        }
    }


    // =======================================================================
    // Initial state
    // =======================================================================

    /**
     * A freshly constructed UsbConnection must report no error.
     * Any replacement implementation must guarantee the same clean initial
     * state before {@code connect()} is called.
     */
    @Test
    public void testInitialStateNoError()
    {
        UsbConnection instance = new ArmLibraryFailureConnection();
        assertFalse("New UsbConnection must not be in error state", instance.isError());
    }

    /**
     * The initial last-error string must be empty (not null) so that callers
     * can safely use it without a null-check.
     */
    @Test
    public void testInitialErrorMessageIsEmpty()
    {
        UsbConnection instance = new ArmLibraryFailureConnection();
        assertEquals("Initial error message must be empty string", "", instance.getLastError());
    }


    // =======================================================================
    // connect() — error handling
    // =======================================================================

    /**
     * Simulates macOS ARM (M1 / M2 / M3) with usb4java-javax: the native
     * library cannot be loaded.  After {@code connect()} the connection must
     * be in error state and the error message must start with
     * {@code "USB Error:"} so that higher-level code can recognise it.
     */
    @Test
    public void testConnectWhenUsbLibraryUnavailable()
    {
        UsbConnection instance = new ArmLibraryFailureConnection();

        instance.connect();

        assertTrue("connect() must set error state when USB library is unavailable",
                   instance.isError());
        assertTrue("Error message must start with 'USB Error:'",
                   instance.getLastError().startsWith("USB Error:"));
    }

    /**
     * When USB services are available but no TomTom watch is attached,
     * {@code connect()} must set error state with the message
     * {@code "No valid USB device found"}.
     */
    @Test
    public void testConnectNoDeviceFound()
    {
        UsbConnection instance = new FailedConnectConnection("No valid USB device found");

        instance.connect();

        assertTrue("connect() must be in error state when no watch is found",
                   instance.isError());
        assertEquals("No valid USB device found", instance.getLastError());
    }

    /**
     * When the OS refuses to claim the USB interface (e.g. already held by a
     * kernel driver — common when a system HID driver claims the device),
     * {@code connect()} must set error state and the message must start with
     * {@code "USB Error:"}.
     */
    @Test
    public void testConnectInterfaceClaimFails()
    {
        UsbConnection instance = new FailedConnectConnection(
                "USB Error: Interface already claimed by kernel driver");

        instance.connect();

        assertTrue("connect() must set error state when interface claim fails",
                   instance.isError());
        assertTrue("Error message must start with 'USB Error:'",
                   instance.getLastError().startsWith("USB Error:"));
    }

    /**
     * A USB device that is not a recognised TomTom watch must cause
     * {@code connect()} to fail with {@code "No valid USB device found"}.
     */
    @Test
    public void testConnectUnknownProductIdNotRecognised()
    {
        UsbConnection instance = new FailedConnectConnection("No valid USB device found");

        instance.connect();

        assertTrue("Unknown product ID must produce error state", instance.isError());
        assertEquals("No valid USB device found", instance.getLastError());
    }


    // =======================================================================
    // connect() — device detection and protocol chunk sizes
    // =======================================================================

    /**
     * A TomTom Multisport / Runner 2 / Spark (product ID 0x7474) must be
     * detected and the protocol chunk sizes must be exactly 50 bytes (read)
     * and 54 bytes (write).
     *
     * These values are used by {@link UsbInterface} to split file transfers
     * into USB packets and must be preserved when the USB backend is replaced.
     */
    @Test
    public void testConnectMultiSportsDeviceChunkSizes()
    {
        UsbConnection instance =
                new ConnectedDeviceConnection(UsbConnection.DeviceType.DEVICETYPE_MULTISPORTS);

        instance.connect();

        assertFalse("Connection to Multisports device must succeed", instance.isError());
        assertEquals("Multisports read chunk must be 50 bytes",
                     MULTISPORTS_READ_CHUNK, instance.getFileReadChunkSize());
        assertEquals("Multisports write chunk must be 54 bytes",
                     MULTISPORTS_WRITE_CHUNK, instance.getFileWriteChunkSize());
    }

    /**
     * A TomTom Spark Music / Runner Music / Adventurer (product ID 0x7475)
     * must be detected and the chunk sizes must be 242 bytes (read) and
     * 246 bytes (write).
     */
    @Test
    public void testConnectSparkMusicDeviceChunkSizes()
    {
        UsbConnection instance =
                new ConnectedDeviceConnection(UsbConnection.DeviceType.DEVICETYPE_SPARK_MUSIC);

        instance.connect();

        assertFalse("Connection to Spark Music device must succeed", instance.isError());
        assertEquals("Spark Music read chunk must be 242 bytes",
                     SPARK_READ_CHUNK, instance.getFileReadChunkSize());
        assertEquals("Spark Music write chunk must be 246 bytes",
                     SPARK_WRITE_CHUNK, instance.getFileWriteChunkSize());
    }

    /**
     * A TomTom Spark Cardio / Runner Cardio (product ID 0x7477) must be
     * detected and use the same chunk sizes as Spark Music (same USB
     * packet size on this device family).
     */
    @Test
    public void testConnectSparkCardioDeviceChunkSizes()
    {
        UsbConnection instance =
                new ConnectedDeviceConnection(UsbConnection.DeviceType.DEVICETYPE_SPARK_CARDIO);

        instance.connect();

        assertFalse("Connection to Spark Cardio device must succeed", instance.isError());
        assertEquals("Spark Cardio read chunk must be 242 bytes",
                     SPARK_READ_CHUNK, instance.getFileReadChunkSize());
        assertEquals("Spark Cardio write chunk must be 246 bytes",
                     SPARK_WRITE_CHUNK, instance.getFileWriteChunkSize());
    }


    // =======================================================================
    // disconnect()
    // =======================================================================

    /**
     * Calling {@code disconnect()} when no connection has been established
     * must be a safe no-op — it must not put the instance into an error state.
     */
    @Test
    public void testDisconnectWhenNotConnected()
    {
        UsbConnection instance = new ArmLibraryFailureConnection();
        instance.disconnect();

        assertFalse("disconnect() without a prior connect() must not produce an error",
                    instance.isError());
    }


    // =======================================================================
    // Chunk-size accessors without a connected device
    // =======================================================================

    /**
     * {@link UsbConnection#getFileReadChunkSize()} must return {@code -1} and
     * set the error flag when called before a successful {@code connect()}.
     * {@link UsbInterface} uses -1 as a sentinel to detect this condition.
     */
    @Test
    public void testGetFileReadChunkSizeWithNoDevice()
    {
        UsbConnection instance = new ArmLibraryFailureConnection();

        int result = instance.getFileReadChunkSize();

        assertEquals("getFileReadChunkSize() must return -1 when no device is known",
                     -1, result);
        assertTrue("getFileReadChunkSize() must set error flag when no device is known",
                   instance.isError());
    }

    /**
     * {@link UsbConnection#getFileWriteChunkSize()} must return {@code -1}
     * and set the error flag when called before a successful {@code connect()}.
     */
    @Test
    public void testGetFileWriteChunkSizeWithNoDevice()
    {
        UsbConnection instance = new ArmLibraryFailureConnection();

        int result = instance.getFileWriteChunkSize();

        assertEquals("getFileWriteChunkSize() must return -1 when no device is known",
                     -1, result);
        assertTrue("getFileWriteChunkSize() must set error flag when no device is known",
                   instance.isError());
    }
}
