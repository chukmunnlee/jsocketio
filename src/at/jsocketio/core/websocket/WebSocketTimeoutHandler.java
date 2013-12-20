/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core.websocket;

import at.jsocketio.api.Frame;
import at.jsocketio.core.HandlerMap;
import at.jsocketio.core.Timeout;
import at.jsocketio.core.utils.InvokeMethod;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.Session;

/**
 *
 * @author project
 */
public class WebSocketTimeoutHandler implements Timeout {
    
    private static final Logger logger = Logger.getLogger(
            WebSocketTimeoutHandler.class.getName());
    
    private final Session session;
    private final HandlerMap handlerMap;
    private final String sessionId;
    
    private long duration = 60 * 1000; //1 minute
    private ScheduledFuture<?> future = null;

    public WebSocketTimeoutHandler(String id, Session sess, HandlerMap map) {
        session = sess;
        handlerMap = map;
        sessionId = id;
    }        
    
    public void setDuration(long d) {
        duration = d;
    }
    
    public void setScheduledFuture(ScheduledFuture<?> f) {
        future = f;
    }

    @Override
    public void sendHeartbeat() throws IOException {
        try {
            session.getBasicRemote().sendObject(Frame.heartbeat());
        } catch (EncodeException ex) {
            throw new IOException(sessionId + " cannot encode heartbeat frame", ex);
        }
    }        

    @Override
    public void timeout() {
        if (logger.isLoggable(Level.FINE))
            logger.log(Level.FINE, "Session timeout for: {0}", sessionId);
        try {
            InvokeMethod.invokeOnTimeout(session, handlerMap);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception when handling @WhenTimeout: " + sessionId
                    , t);
            //Try closing it
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY
                        , t.getMessage()));
            } catch (IOException ex) { /* ignore */ }
         }
        //Cancel the thread
        future.cancel(true);
    }     

    @Override
    public long getDuration() {
        return (duration);
    } 
    
}
