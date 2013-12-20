/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.api;

import com.glines.socketio.server.*;
import java.util.logging.*;
import javax.websocket.*;

/**
 *
 * @author projects
 */
public class SocketIOFrameEncoder implements Encoder.Text<SocketIOFrame> {
    
    private static final Logger logger = Logger.getLogger(SocketIOFrameEncoder.class.getName());

    @Override
    public String encode(SocketIOFrame sioFrame) throws EncodeException {
        final String result = sioFrame.encode();
        if (logger.isLoggable(Level.FINE))
            logger.log(Level.FINE, "ws-out: {0}", result);
        return (result);
    }        

    @Override
    public void init(EndpointConfig config) {  }

    @Override
    public void destroy() {  }
    
}
