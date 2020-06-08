/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jorgen
 */
public class WorkoutDescriptionsTest
{
    
    public WorkoutDescriptionsTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }


    
    /**
     * Test of findDescription method, of class WorkoutDescriptions.
     */
    @Test
    public void testFindDescription()
    {
        String result;
        
        System.out.println("findDescription");
        int index = 0;
        WorkoutDescriptions instance = new WorkoutDescriptions();
        String expResult = "";
        result = instance.findDescription(index);
        assertNull(result);
        
        instance.addDescription("hello");
        result = instance.findDescription(index);
        assertEquals("hello", result);
    }

    /**
     * Test of findDescriptionIndex method, of class WorkoutDescriptions.
     */
    @Test
    public void testFindDescriptionIndex()
    {
        int result;
        
        System.out.println("findDescriptionIndex");
        String description1 = "testing 123";
        String description2 = "testing 234";
        WorkoutDescriptions instance = new WorkoutDescriptions();
        instance.addDescription(description1);
        instance.addDescription(description2);
        int expResult = 0;
        result = instance.findDescriptionIndex(description1);
        assertEquals(expResult, result);

        expResult=1;
        result = instance.findDescriptionIndex(description2);
        assertEquals(expResult, result);
        
        expResult=-1;
        result = instance.findDescriptionIndex("non existing description");
        assertEquals(expResult, result);        

    }

    /**
     * Test of addDescription method, of class WorkoutDescriptions.
     */
    @Test
    public void testAddDescription_String()
    {
        int result; 
        String description;
        
        System.out.println("addDescription");
        String description1 = "some description";
        String description2 = "some other description";
        WorkoutDescriptions instance = new WorkoutDescriptions();

        result=instance.addDescription(description1);
        assertEquals(0, result);
        assertEquals(description1, instance.findDescription(0));

        result=instance.addDescription(description1);
        assertEquals(0, result);
        assertEquals(description1, instance.findDescription(0));
        
        result=instance.addDescription(description2);
        assertEquals(1, result);
        assertEquals(description1, instance.findDescription(0));
        assertEquals(description2, instance.findDescription(1));
    }

    /**
     * Test of addDescription method, of class WorkoutDescriptions.
     */
    @Test
    public void testAddDescription_int_String()
    {
        System.out.println("addDescription");
        int index = 0;
        String description = "test description";
        WorkoutDescriptions instance = new WorkoutDescriptions();
        instance.addDescription(index, description);
        
        String result=instance.findDescription(0);
        assertEquals(description,  result);
    }
    
    /**
     * Test of size method, of class WorkoutDescriptions.
     */
    @Test
    public void testSize()
    {
        System.out.println("size");
        int index = 0;
        String description = "test description";
        WorkoutDescriptions instance = new WorkoutDescriptions();
        assertEquals(0, instance.size());
        
        instance.addDescription(index, description);
        assertEquals(1, instance.size());
        instance.addDescription(index, description);
        assertEquals(1, instance.size());
    }

    /**
     * Test of clear() method, of class WorkoutDescriptions.
     */
    @Test
    public void testClear()
    {
        System.out.println("clear");
        int index = 0;
        String description = "test description";
        WorkoutDescriptions instance = new WorkoutDescriptions();
        
        instance.addDescription(index, description);
        assertEquals(1, instance.size());
        assertEquals(description, instance.findDescription(0));
        instance.clear();
        assertEquals(0, instance.size());
        assertNull(instance.findDescription(0));
    }

    
    
}
