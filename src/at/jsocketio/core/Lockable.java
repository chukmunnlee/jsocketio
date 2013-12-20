/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.locks.Lock;

/**
 *
 * @author project
 */
public class Lockable implements Closeable {
    
    private final Lock lock;

    public Lockable(Lock l) {
        lock = l;
        lock.lock();
    }        

    @Override
    public void close() throws IOException {        
        lock.unlock();
    }
    
    public static Lockable lock(Lock l) {        
        return (new Lockable(l));
    }
    
}
