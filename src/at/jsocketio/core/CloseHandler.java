/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import java.io.Closeable;
import java.util.concurrent.ScheduledFuture;

/**
 *
 * @author project
 * @param <T>
 */
public abstract class CloseHandler<T> implements Closeable {

    protected T object;    
    protected String id;
	protected ScheduledFuture<?> future;
    
    public void setSessionId(String i) {
        id = i;
    }
    public String getSessionId() {
        return (id);
    }
}
