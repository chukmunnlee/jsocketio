/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.test;

import at.jsocketio.core.HandlerMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author project
 */
public class MatchingEvents {

    static HandlerMap handlerMap = null;

    @BeforeClass
    public static void setup() {
        handlerMap = new HandlerMap();
        Set<Class<?>> set = new HashSet<>();
        set.add(String.class);
        set.add(Integer.class);
        set.add(Float.class);
        handlerMap.register("mess.*", set);
    }

    @Test
    public void shouldFindEvent() {
//        assertNotSame(Collections.EMPTY_SET, handlerMap.get("message"));
//        assertNotSame(Collections.EMPTY_SET, handlerMap.get("mess"));
//        assertEquals(Collections.EMPTY_SET, handlerMap.get("abc"));      
    }

}
