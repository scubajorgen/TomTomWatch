/*
    Copyright (c) 2023 Jorgen

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
 */
package net.studioblueplanet.tomtomwatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.GpxWriter;
import net.studioblueplanet.ttbin.TcxWriter;
import net.studioblueplanet.ttbin.TomTomReader;

/**
 * Batch converter: .ttbin with GPS -&gt; .gpx ; indoor/Gym (time+HR) -&gt; .tcx.
 */
public class TtbinToGpxBatch
{
    private static final String APP_NAME="TomTomWatch-batch";

    private boolean force;
    private boolean smooth;
    private boolean gpxOnly;
    private boolean tcxOnly;
    private String  propertiesFile;
    private Path    inputDir;
    private Path    outputDir;

    public static void main(String[] args)
    {
        Locale.setDefault(new Locale("en", "US"));
        TtbinToGpxBatch batch=new TtbinToGpxBatch();
        if (!batch.parseArgs(args))
        {
            System.exit(1);
        }
        System.exit(batch.run());
    }

    private boolean parseArgs(String[] args)
    {
        List<String> positional=new ArrayList<>();
        for (int i=0; i<args.length; i++)
        {
            String arg=args[i];
            if ("--help".equals(arg) || "-h".equals(arg))
            {
                printHelp();
                return false;
            }
            if ("--force".equals(arg) || "-f".equals(arg))
            {
                force=true;
            }
            else if ("--smooth".equals(arg))
            {
                smooth=true;
            }
            else if ("--gpx-only".equals(arg))
            {
                gpxOnly=true;
            }
            else if ("--tcx-only".equals(arg))
            {
                tcxOnly=true;
            }
            else if ("--properties".equals(arg))
            {
                if (i+1>=args.length)
                {
                    System.err.println("Missing value for --properties");
                    return false;
                }
                propertiesFile=args[++i];
            }
            else if (arg.startsWith("-"))
            {
                System.err.println("Unknown option: "+arg);
                printHelp();
                return false;
            }
            else
            {
                positional.add(arg);
            }
        }

        if (positional.isEmpty())
        {
            printHelp();
            return false;
        }
        if (gpxOnly && tcxOnly)
        {
            System.err.println("Cannot combine --gpx-only and --tcx-only");
            return false;
        }

        inputDir=new File(positional.get(0)).toPath().toAbsolutePath().normalize();
        outputDir=(positional.size()>=2)
            ? new File(positional.get(1)).toPath().toAbsolutePath().normalize()
            : null;

        if (!Files.isDirectory(inputDir))
        {
            System.err.println("Input directory does not exist: "+inputDir);
            return false;
        }
        return true;
    }

    private void printHelp()
    {
        System.out.println("Usage: TtbinToGpxBatch [options] <inputDir> [outputDir]");
        System.out.println("  GPS activities -> .gpx ; Gym/Indoor (HR+time) -> .tcx");
        System.out.println("Options: --force --smooth --gpx-only --tcx-only --properties <file> --help");
    }

    private int run()
    {
        if (propertiesFile!=null)
        {
            ConfigSettings.setPropertiesFile(propertiesFile);
        }
        else
        {
            File localProps=new File("tomtomwatch.properties");
            if (localProps.isFile())
            {
                ConfigSettings.setPropertiesFile(localProps.getAbsolutePath());
            }
        }

        ConfigSettings settings=ConfigSettings.getInstance();
        DebugLogger.setDebugLevel("error");

        TomTomReader reader=TomTomReader.getInstance();
        if (smooth || settings.getBooleanValue("trackSmoothingEnabled"))
        {
            float q=(float)settings.getDoubleValue("trackSmoothingQFactor");
            reader.setTrackSmoothing(true, q);
            System.out.println("Track smoothing enabled (Q="+q+")");
        }
        if (settings.getBooleanValue("trackCompressionEnabled"))
        {
            reader.setTrackCompression(true, settings.getDoubleValue("trackCompressionMaxError"));
        }

        List<Path> ttbinFiles;
        try
        {
            ttbinFiles=collectTtbinFiles(inputDir);
        }
        catch (IOException e)
        {
            System.err.println("Failed to scan input directory: "+e.getMessage());
            return 1;
        }

        if (ttbinFiles.isEmpty())
        {
            System.out.println("No .ttbin files found under "+inputDir);
            return 0;
        }

        System.out.println("Found "+ttbinFiles.size()+" .ttbin file(s)");
        int okGpx=0;
        int okTcx=0;
        int skipped=0;
        int failed=0;

        GpxWriter gpxWriter=GpxWriter.getInstance();
        TcxWriter tcxWriter=TcxWriter.getInstance();

        for (Path ttbin : ttbinFiles)
        {
            try
            {
                Activity activity=reader.readTtbinFile(ttbin.toString());
                if (activity==null)
                {
                    System.err.println("FAIL  "+ttbin+" (parse error)");
                    failed++;
                    continue;
                }

                boolean hasGps=activity.hasGpsTrack();
                boolean hasTimedHr=activity.hasTimedHeartRateSamples();

                if (hasGps && !tcxOnly)
                {
                    Path out=resolveOutputPath(ttbin, "gpx");
                    if (Files.exists(out) && !force)
                    {
                        skipped++;
                        continue;
                    }
                    ensureParent(out);
                    try (FileWriter fw=new FileWriter(out.toFile()))
                    {
                        gpxWriter.writeTrackToFile(fw, activity, APP_NAME);
                    }
                    System.out.println("GPX   "+activity.getActivityDescription()+"  "+relativize(ttbin)+" -> "+relativize(out));
                    okGpx++;
                }
                else if (!hasGps && hasTimedHr && !gpxOnly)
                {
                    Path out=resolveOutputPath(ttbin, "tcx");
                    if (Files.exists(out) && !force)
                    {
                        skipped++;
                        continue;
                    }
                    ensureParent(out);
                    try (FileWriter fw=new FileWriter(out.toFile()))
                    {
                        tcxWriter.writeTrackToFile(fw, activity, APP_NAME);
                    }
                    System.out.println("TCX   "+activity.getActivityDescription()+"  "+relativize(ttbin)+" -> "+relativize(out));
                    okTcx++;
                }
                else
                {
                    System.out.println("SKIP  "+activity.getActivityDescription()+"  "+relativize(ttbin));
                    skipped++;
                }
            }
            catch (Exception e)
            {
                System.err.println("FAIL  "+ttbin+" ("+e.getMessage()+")");
                failed++;
            }
        }

        System.out.println();
        System.out.println("Done: "+okGpx+" GPX, "+okTcx+" TCX, "+skipped+" skipped, "+failed+" failed");
        return failed>0 ? 2 : 0;
    }

    private static void ensureParent(Path out) throws IOException
    {
        if (out.getParent()!=null)
        {
            Files.createDirectories(out.getParent());
        }
    }

    private Path resolveOutputPath(Path ttbin, String extension)
    {
        String fileName=ttbin.getFileName().toString();
        int dot=fileName.lastIndexOf('.');
        String base=(dot>0) ? fileName.substring(0, dot) : fileName;
        String outName=base+"."+extension;
        if (outputDir==null)
        {
            return ttbin.resolveSibling(outName);
        }
        return outputDir.resolve(inputDir.relativize(ttbin.getParent())).resolve(outName);
    }

    private String relativize(Path path)
    {
        try
        {
            Path cwd=new File(".").toPath().toAbsolutePath().normalize();
            if (path.startsWith(cwd))
            {
                return cwd.relativize(path).toString();
            }
        }
        catch (Exception ignored)
        {
        }
        return path.toString();
    }

    private static List<Path> collectTtbinFiles(Path root) throws IOException
    {
        final List<Path> files=new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            {
                if (file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ttbin"))
                {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        files.sort(null);
        return files;
    }
}
