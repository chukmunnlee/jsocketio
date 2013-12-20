/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import java.io.IOException;

/**
 *
 * @author projects
 */
public interface Timeout {
    public void timeout();
    public long getDuration();
    public void sendHeartbeat() throws IOException;
}
