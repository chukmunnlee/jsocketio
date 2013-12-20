/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author chukmunn
 */
@ApplicationScoped
public class SessionMap {
    
    private static final Logger logger = Logger.getLogger(SessionMap.class.getName());

    private final Lock mapLock = new ReentrantLock();
    private final Map<String, CloseHandler<?>> sessions = new HashMap<>();

    public void add(final CloseHandler<?> closable) {
        mapLock.lock();        
        try {
            sessions.put(closable.getSessionId(), closable);
        } finally {
            mapLock.unlock();
        }
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "Adding {0} to SessionMap", closable.getSessionId());
    }
    
    public boolean hasSession(final String id) {
        return (sessions.containsKey(id));
    }

    public void close(final String id) throws IOException {        
        mapLock.lock();        
        try {
            CloseHandler<?> handler = sessions.remove(id);
            if (null != handler)
                handler.close();            
        } finally {
            mapLock.unlock();
        }
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "Closing {0} to SessionMap", id);
    }
    
    public void remove(final String id) {
        mapLock.lock();
        try {
            sessions.remove(id);
        } finally {
            mapLock.unlock();
        }
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "Removing {0} to SessionMap", id);
    }

}
