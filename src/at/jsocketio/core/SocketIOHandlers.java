/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import at.jsocketio.api.annotation.*;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author projects
 */
public class SocketIOHandlers {
    
    private static final Logger logger = Logger.getLogger(SocketIOHandlers.class.getName());
    
    private static final Map<String, List<Class<?>>> classes = new HashMap<>();    
    
    public static void add(Class<?> c) {
        if (!c.isAnnotationPresent(SocketIO.class))
            return;
        SocketIO sio = c.getAnnotation(SocketIO.class);                
        
        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Adding @SocketIO {0}, {1}"
                    , new Object[]{sio.value(), c.getName()});
        List<Class<?>> handlers = classes.get(sio.value());
        if ((null == handlers)) {
            handlers = new LinkedList<>();
            classes.put(sio.value(), handlers);
        }
        handlers.add(c);        
    }
    
    public List<Class<?>> eventHandlers(String event) {
        List<Class<?>> handlers = classes.get(event);
        if (null == handlers)
            handlers = new LinkedList<>();
        return (Collections.unmodifiableList(handlers));
    }
    
    public List<Class<?>> defaultEventHandlers() {
        return (eventHandlers(SocketIO.DEFAULT_EVENT_NAME));
    }        
    
}
