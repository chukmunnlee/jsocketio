/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core.websocket;

import at.jsocketio.core.*;
import java.io.*;
import java.util.concurrent.*;
import javax.websocket.*;

/**
 *
 * @author project
 */
public class WebSocketCloseHandler extends CloseHandler<Session> {

    public WebSocketCloseHandler(String i, Session s, ScheduledFuture<?> timeScheduledFuture) {
        id = i;
        object = s;
        future = timeScheduledFuture;
    }

    @Override
    public void close() throws IOException {
        if (!future.isCancelled())
            future.cancel(true);
        object.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Forced closed"));
    }

}
