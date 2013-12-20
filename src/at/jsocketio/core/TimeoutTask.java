/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import java.io.IOException;
import java.util.concurrent.atomic.*;
import java.util.logging.*;

/**
 *
 * @author projects
 */
public class TimeoutTask implements Runnable {
    
    private static final Logger logger = Logger.getLogger(TimeoutTask.class.getName());
    
    private final String id;            
    private final AtomicLong value = new AtomicLong();
    private final Timeout timeout;
    private final long timeoutValue;
    private final long heartbeatDuration;

    public TimeoutTask(String i, Timeout t) {
        id = i;        
        timeout = t;       
        heartbeatDuration = timeout.getDuration();
        timeoutValue = heartbeatDuration * 4;
    }
    
    public void resetTimeout() {
        value.set(0);
    }

    @Override
    public void run() {
        final long currValue = value.addAndGet(timeout.getDuration());
        if (currValue >= timeoutValue)        
            timeout.timeout();         
        else if (currValue > heartbeatDuration) //Have missed atleast one heartbeat
            try {
                timeout.sendHeartbeat();
                if (logger.isLoggable(Level.FINER))
                    logger.log(Level.INFO, "Tick tock for {0}", id);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Cannot send heartbeat", ex);
            }
        //else currValue == heartbeatDuration - don't send heartbeat cause start of new cycle
    }
    
}
