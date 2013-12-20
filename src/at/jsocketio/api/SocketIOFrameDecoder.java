/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.api;

import com.glines.socketio.server.*;
import java.util.*;
import java.util.logging.*;
import javax.websocket.*;

/**
 *
 * @author projects
 */
public class SocketIOFrameDecoder implements Decoder.Text<List<SocketIOFrame>> {        
    
    private static final Logger logger = Logger.getLogger(SocketIOFrameDecoder.class.getName());

    @Override
    public List<SocketIOFrame> decode(String arg0) throws DecodeException {
        if (logger.isLoggable(Level.FINE))
            logger.log(Level.FINE, "ws-in: {0}", arg0);
        return (SocketIOFrame.parse(arg0));
    }

    @Override
    public boolean willDecode(String arg0) {
        List<SocketIOFrame> frames = SocketIOFrame.parse(arg0);
        return ((null != frames) || (frames.size() > 0));
    }

    @Override
    public void init(EndpointConfig config) { }

    @Override
    public void destroy() { }
    
}
