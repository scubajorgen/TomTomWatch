/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import hirondelle.date4j.DateTime;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests flattened TTBIN/export naming: {device}/{Type}_{YYYY-MM-DD}_{hh-mm-ss}.ext
 * @author douglascrp
 */
public class TtbinFileWriterTest
{
    private TtbinFileWriter writer;
    private File            tempDir;

    @Before
    public void setUp()
    {
        writer  =TtbinFileWriter.getInstance();
        tempDir =new File(System.getProperty("java.io.tmpdir"), "ttbinwriter-test-"+System.nanoTime());
        assertTrue(tempDir.mkdirs());
    }

    @After
    public void tearDown()
    {
        deleteRecursive(tempDir);
    }

    private void deleteRecursive(File file)
    {
        if (file==null || !file.exists())
        {
            return;
        }
        File[] children=file.listFiles();
        if (children!=null)
        {
            for (File child : children)
            {
                deleteRecursive(child);
            }
        }
        file.delete();
    }

    @Test
    public void testGetActivityFileStem()
    {
        DateTime dateTime=new DateTime("2026-09-02 19:45:00");
        assertEquals("Gym_2026-09-02_19-45-00", writer.getActivityFileStem(dateTime, "Gym"));
    }

    @Test
    public void testGetFullFileNameFlattened()
    {
        DateTime dateTime=new DateTime("2026-09-02 19:45:00");
        String fileName=writer.getFullFileName(tempDir.getAbsolutePath(), "MyWatch", dateTime, "Gym");

        assertNotNull(fileName);
        assertTrue(fileName.endsWith("Gym_2026-09-02_19-45-00.ttbin"));
        assertTrue(fileName.contains("MyWatch"+File.separator) || fileName.contains("MyWatch/"));
        // Date must be in the filename, not as a path segment
        assertFalse("Date must not be a directory segment", fileName.matches(".*[/\\\\]2026-09-02[/\\\\].*"));
        assertTrue(new File(fileName).getParentFile().exists());
    }

    @Test
    public void testGetExportFullFileNameGpx()
    {
        DateTime dateTime=new DateTime("2026-09-02 19:45:00");
        String fileName=writer.getExportFullFileName(tempDir.getAbsolutePath(), "MyWatch", dateTime, "Running", "gpx");

        assertNotNull(fileName);
        assertTrue(fileName.endsWith("Running_2026-09-02_19-45-00.gpx"));
        assertFalse(fileName.matches(".*[/\\\\]2026-09-02[/\\\\].*"));
    }
}
