/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.test;

import org.junit.*;

import static org.junit.Assert.*;
import static at.jsocketio.core.utils.TypeConversionUtils.*;
import javax.swing.*;

/**
 *
 * @author projects
 */
public class TypeConversion {
    
    public enum NumberThirtyTwo { thirtyTwo };
    
    @Test
    public void shouldConvertPrimitive() {
//        assertEquals((byte)32, convertPrimitive(byte.class, "32"));
//        assertEquals((short)32, convertPrimitive(short.class, "32"));
//        assertEquals(32, convertPrimitive(int.class, "32"));
//        assertEquals(32L, convertPrimitive(long.class, "32"));
//        assertEquals(32F, convertPrimitive(float.class, "32"));        
//        assertEquals(32D, convertPrimitive(double.class, "32"));
//        assertEquals("32", convertPrimitive(String.class, "32"));
//        assertNull(convertPrimitive(JButton.class, "32"));
    }
    
    @Test
    public void shouldConvert() {
//        assertEquals(new Integer(32), convert(Integer.class, "32"));
//        assertEquals("32", convert(String.class, "32"));
//        assertEquals(NumberThirtyTwo.thirtyTwo, convert(NumberThirtyTwo.class, "thirtyTwo"));
    }
}
