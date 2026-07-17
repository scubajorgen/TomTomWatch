/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.generics;

import net.studioblueplanet.logger.DebugLogger;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author jorgen
 */
public class GitBuildInfo
{
    private Properties buildProperties;

    private static GitBuildInfo theInstance;
    
    /**
     * Constructor
     */
    private GitBuildInfo()
    {
        buildProperties=new Properties();
        try
        {
            InputStream inStream=getClass().getClassLoader().getResourceAsStream("build.properties");
            if (inStream!=null)
            {
                buildProperties.load(inStream);
            }
            else
            {
                DebugLogger.error("build.properties not found on classpath; run a Maven build to generate it");
            }
        }
        catch (IOException e)
        {
            DebugLogger.error("Error loading build properties: "+e.getMessage());
        }
    }
    
    /**
     * Get the one and only instance of this Singleton class
     * @return The instance
     */
    public static GitBuildInfo getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new GitBuildInfo();
        }
        return theInstance;
    }
    
    /**
     * Get the closest commit ID
     * @return Commit ID
     */
    public String getGitCommit()
    {
        return buildProperties.getProperty("git.commit.id");
    }
    
    /**
     * Get a description of the commit
     * @return Description of the commit
     */
    public String getGitCommitDescription()
    {
        return buildProperties.getProperty("git.commit.id.describe");
    }
    
    /**
     * Return the time of the build
     * @return Build time
     */
    public String getBuildTime()
    {
        return buildProperties.getProperty("git.build.time");
    }
    
    /**
     * Return the closest tag name
     * @return The tag name.
     */
    public String getClosestTagName()
    {
        return buildProperties.getProperty("git.closest.tag.name");
    }
    
}
