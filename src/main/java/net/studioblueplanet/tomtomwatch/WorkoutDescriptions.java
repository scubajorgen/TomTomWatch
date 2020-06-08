/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This class represents a list of descriptions, each description identified by 
 * an index value. The index has values of 0, 1, 2, 3,...
 * The class is implemented around a HashMap and contains some additional 
 * functionality for searching and populating the list.
 * @author jorgen
 */
public class WorkoutDescriptions
{
    private HashMap<Integer, String> descriptions;
    /**
     * Constructor
     */
    public WorkoutDescriptions()
    {
        descriptions=new HashMap<>();
    }
    
    /**
     * Returns the description at given index position
     * @param index The index position
     * @return The description as String or null if not existing
     */
    public String findDescription(int index)
    {
        return descriptions.get(index);
    }
    
    /** 
     * Find the index for a given description
     * @param description Description to find
     * @return The index, or -1 if not found
     */
    public int findDescriptionIndex(String description)
    {
        for (Integer key   : descriptions.keySet()) 
        {
             String value = descriptions.get(key);  //get() is less efficient 
             if (value.equals(description))
             {
                 return key;
             }
        }        
        return -1;
    } 
    
    /**
     * Adds the description if it is not already present in the descriptions list
     * @param description Description to add
     * @return The index representing the location in the hash map. It is an 
     *         existing value (if the description already exists) or the next
     *         value (if the description has been added)
     */
    public int addDescription(String description)
    {
        int     index;
        int     maxIndex;
        Integer i;

        index=-1;
        maxIndex=-1;
        Iterator<Integer> it=descriptions.keySet().iterator();
        while (it.hasNext() && index<0)
        {
            i=it.next();
            if (descriptions.get(i).equals(description))
            {
                index=i;
            }
            if (i>maxIndex)
            {
                maxIndex=i;
            }
        }
        if (index<0)
        {
            index=maxIndex+1;
            descriptions.put(index, description);
        }
        return index;
    }
    
    
    /**
     * Adds description to the description list at existing index positions. 
     * The user is responsible for the consistency of the list
     * @param index Index position
     * @param description Description to adde
     */
    public void addDescription(int index, String description)
    {
        descriptions.put(index, description);
    }
    
    /**
     * Empties the description list
     */
    public void clear()
    {
        descriptions.clear();
    }
    
    /**
     * Returns the size of the description list
     * @return The size
     */
    public int size()
    {
        return descriptions.size();
    }
    
    /**
     * Returns the index iterator
     * @return The iterator
     */
    public Iterator<Integer> iterator()
    {
        return descriptions.keySet().iterator();
    }
}
