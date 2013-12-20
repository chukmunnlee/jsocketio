/**
 * The MIT License Copyright (c) 2010 Tad Glines
 *
 * Contributors: Ovea.com, Mycila.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package at.jsocketio.core;

import javax.servlet.ServletConfig;
import java.util.logging.*;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.*;

/**
 * @author Mathieu Carbou
 */
@ApplicationScoped
public class SocketIOConfiguration {

    private static final Logger LOGGER = Logger.getLogger(SocketIOConfiguration.class.getName());

    public static final String PARAM_HEARTBEAT_DELAY = "heartbeat-delay";
    public static final String PARAM_HEARTBEAT_TIMEOUT = "heartbeat-timeout";
    public static final String PARAM_TIMEOUT = "timeout";

    public static final String PARAM_BUFFER_SIZE = "bufferSize";
    public static final String PARAM_MAX_IDLE = "maxIdleTime";

    int DEFAULT_BUFFER_SIZE = 8192;
    int DEFAULT_MAX_IDLE = 300 * 1000;

    private ServletConfig config;
    private String namespace;   
    
    private ManagedScheduledExecutorService executorService;
    
    public void init(ServletConfig config, String namespace) {
        this.namespace = namespace;
        this.config = config;
    }

    public long getHeartbeatDelay(long def) {
        return getLong(PARAM_HEARTBEAT_DELAY, def);
    }

    public long getHeartbeatTimeout(long def) {
        return getLong(PARAM_HEARTBEAT_TIMEOUT, def);
    }

    public long getTimeout(long def) {
        return getLong(PARAM_TIMEOUT, def);
    }

    public int getBufferSize() {
        return getInt(PARAM_BUFFER_SIZE, DEFAULT_BUFFER_SIZE);
    }

    public int getMaxIdle() {
        return getInt(PARAM_MAX_IDLE, DEFAULT_MAX_IDLE);
    }

    private int getInt(String param, int def) {
        String v = getString(param);
        return v == null ? def : Integer.parseInt(v);
    }

    private long getLong(String param, long def) {
        String v = getString(param);
        return v == null ? def : Long.parseLong(v);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getString(String param) {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Getting InitParameter: " + namespace + "." + param);
        String v = config.getInitParameter(namespace + "." + param);
        if (v == null) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Fallback to InitParameter: " + param);
            v = config.getInitParameter(param);
        }
        return v;
    }

    public String getString(String param, String def) {
        String v = getString(param);
        return v == null ? def : v;
    }
    
    public void setExecutorService(ManagedScheduledExecutorService s) {
        executorService = s;
    }
    public ManagedScheduledExecutorService getExecutorService() {
        return (executorService);
    }

}
