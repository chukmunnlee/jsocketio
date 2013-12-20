/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core.websocket;

import at.jsocketio.api.*;
import at.jsocketio.core.*;
import at.jsocketio.core.utils.*;
import com.glines.socketio.server.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.annotation.*;
import javax.enterprise.concurrent.*;
import javax.inject.*;
import javax.json.*;
import javax.websocket.*;
import javax.websocket.server.*;

/**
 *
 * @author projects
 */
@ServerEndpoint(value = "/socket.io/{version}/websocket/{sessionId}"
        , encoders = {SocketIOFrameEncoder.class}, decoders = {SocketIOFrameDecoder.class}
)
public class WebSocketHandler {

    private static final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    @Inject private HandlerMap handlerMap;
    @Inject private SocketIOConfiguration socketIOConfig;
    @Inject private SessionMap sessionMap;
    
    private TimeoutTask timeoutTask;
    private ScheduledFuture<?> timeoutTaskHandle = null;
    private Session session;
    private String sessionId;    

    public WebSocketHandler() {
        logger.log(Level.INFO, "WebSocketHandler for jsocketio: {0}"
                , at.jsocketio.core.SocketIOServlet.VERSION);
    }

    @OnOpen
    public void open(Session session, EndpointConfig config
            , @PathParam("version") String version, @PathParam("sessionId") String sessionId) {

        session.getUserProperties().put("sessionId", sessionId);                

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Connected: version: {0}, session: {1}"
                    , new Object[]{version, sessionId});        

        try {
            InvokeMethod.invokeOnOpen(session, config, handlerMap);
            session.getBasicRemote().sendObject(Frame.connect());
        } catch (final IOException | EncodeException | IllegalArgumentException ex) {
            logger.log(Level.SEVERE, "Cannot send SocketIO CONNECT", ex);
            try {
                session.close();
            } catch (final IOException e) { /* Ignore */

            }
        }
        
        long heartBeatInterval = socketIOConfig.getHeartbeatDelay(
                SocketIOServlet.DEFAULT_HEARTBEAT_TIMEOUT_VALUE);
        WebSocketTimeoutHandler timeoutHandler = 
                new WebSocketTimeoutHandler(sessionId, session, handlerMap);
        timeoutHandler.setDuration(heartBeatInterval);
                
        timeoutTask = new TimeoutTask(sessionId, timeoutHandler);        
        timeoutTaskHandle = (ScheduledFuture<?>)socketIOConfig.getExecutorService()
                .scheduleAtFixedRate(timeoutTask, 0, heartBeatInterval, TimeUnit.MILLISECONDS);     
        timeoutHandler.setScheduledFuture(timeoutTaskHandle);
        
        this.session = session;
        this.sessionId = sessionId;        

        sessionMap.add(new WebSocketCloseHandler(sessionId
                , session, timeoutTaskHandle));
    }

    @OnClose
    public void close(Session session, CloseReason reason
            , @PathParam("version") String version, @PathParam("sessionId") String sessionId) {                

        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Closed: version {0}, session: {1}, code: {2}, reason: {3}"
                    , new Object[]{version, sessionId, reason.getCloseCode().getCode()
                            , reason.getReasonPhrase()});
        }

        try {
            InvokeMethod.invokeOnClose(session, reason, handlerMap);
            session.close();
        } catch (final IOException ex) { /* Ignore */ }
        
        try {
            cleanup(sessionId, session, reason);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Problems when closing session: " + sessionId, ex);
        }
    }

    @OnMessage
    public void message(Session session, List<SocketIOFrame> frames
            , @PathParam("version") String version, @PathParam("sessionId") String sessionId) {

        if (logger.isLoggable(Level.FINE))
            logger.log(Level.FINE, "@OnMessage: {0}", frames);        

        for (SocketIOFrame f : frames) {
            switch (f.getFrameType()) {
                case CONNECT:
                    //We are connected so can ignore
                    if (logger.isLoggable(Level.FINE))
                        logger.log(Level.FINE, "SocketIO Frame: CONNECT: {0}", sessionId);                
                    break;

                case HEARTBEAT:                    
                    if (logger.isLoggable(Level.FINE))
                        logger.log(Level.FINE, "SocketIO Frame: HEARTBEAT: {0}", sessionId);                                       
                    break;

                case CLOSE:                    
                    if (logger.isLoggable(Level.FINE))
                        logger.log(Level.FINE, "SocketIO Frame: CLOSE {0}", sessionId);
                    try {
                        cleanup(sessionId, session
                                , new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE
                                , "Closed by client"));
                    } catch (IOException ex) {
                        logger.log(Level.WARNING, "Error when closing client initiated close()" + sessionId
                                , ex);
                    }
                
                    break;

                case EVENT:
                    at.jsocketio.api.EventObject evt = 
                            at.jsocketio.api.EventObject.valueOf(f.getData());
                    if (logger.isLoggable(Level.FINE))
                        logger.log(Level.FINE, "EVENT: {0}, {1}"
                                , new Object[]{evt.getName(), evt.getPayload().toString()});
                    try {
                        InvokeMethod.invoke(evt, handlerMap, session);
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, "Error when invoking EVENT", t);
                    }
                    break;

                case JSON_MESSAGE:
                    JsonObject obj = Frame.parseJson(f.getData());
                    if (logger.isLoggable(Level.FINE))
                        logger.log(Level.FINE, "JSON_MESSAGE: {0}", obj);          
                    
                    try {
                        InvokeMethod.invoke(obj, handlerMap, session);
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, "Error when invoking JSON_MESSAGE", t);
                    }
                    break;

                case MESSAGE:
                    if (logger.isLoggable(Level.FINE))
                        logger.log(Level.FINE, "MESSAGE: {0}", f.getData());                
                    
                    try {
                        InvokeMethod.invoke(f.getData(), handlerMap, session);
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, "Error when invoking MESSAGE", t);
                    }
                    break;
                    
                case ACK:
                    if (logger.isLoggable(Level.FINE))
                        logger.log(Level.FINE, "ACK: {0}", f.getData());
                    break;

                default:
                    logger.log(Level.INFO, "Unknown frame type");
            }
        }
        //Reset the resetTimeout on every message from client. Is alive
        timeoutTask.resetTimeout();
    }
    
    private void cleanup(String sessionId, Session s, CloseReason cr) throws IOException {
		sessionMap.remove(sessionId);
        if (!timeoutTaskHandle.isCancelled())
            timeoutTaskHandle.cancel(true);
        s.close(cr);
    }      
}
