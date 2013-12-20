/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import java.util.*;
import java.util.logging.*;
import javax.servlet.*;

import javax.servlet.annotation.HandlesTypes;

import at.jsocketio.api.annotation.SocketIO;
import java.io.*;

/**
 *
 * @author projects
 */
@HandlesTypes({SocketIO.class})
public class SocketIOServletInitializer implements ServletContainerInitializer {

    private static final Logger logger
            = Logger.getLogger(SocketIOServletInitializer.class.getName());
        
    //@Inject private HandlerMap handlers;
    
    @Override
    public void onStartup(Set<Class<?>> handlerSet, ServletContext ctx) throws ServletException {
        
        if ((handlerSet.size() <= 0) && (logger.isLoggable(Level.INFO))) {
            logger.log(Level.INFO, "No @SocketIO annotation found.");                    
            return;
        }

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Initializing SocketIOServlet");                

        ctx.addServlet("SocketIOServlet", SocketIOServlet.class);

        //Modify resource 
        byte[] buffer = new byte[8192];
        int sz = 0;
        String socketJS;

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(
                SocketIOServlet.RESOURCE_BASE + "/socket.io.js");
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while ((sz = is.read(buffer)) != -1)
                baos.write(buffer, 0, sz);
            socketJS = baos.toString();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Modifying socket.io.js", ex);
            throw new ServletException("Modifying socket.io.js", ex);
        }

        String ctxPath = ctx.getContextPath().substring(1);
        socketJS = socketJS.replaceAll("resource: 'socket.io'", "resource: '" + ctxPath + "/socket.io'");

        File tmpDir = (File) ctx.getAttribute(ServletContext.TEMPDIR);
        File sockJS = new File(tmpDir, ctxPath + "_socket.io.js");
        try (FileWriter fw = new FileWriter(sockJS)) {
            fw.write(socketJS);
            fw.flush();
            ctx.setAttribute(SocketIOServlet.MODIFIED_SOCKET_IO_JS, sockJS);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Writing " + ctxPath + "_socket.io.js", ex);
        }

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Modified socket.io.js file at: {0}", sockJS.getAbsolutePath());
        
        //Register handlers
        Map<String, Set<Class<?>>> handlers = new HashMap<>();
        //HandlerMap handlers = new HandlerMap();
        for (Class<?> c: handlerSet) {      
            SocketIO sio = c.getAnnotation(SocketIO.class);
            Set<Class<?>> clazz = handlers.get(sio.value());
            if (null == clazz) {
                clazz = new HashSet<>();
                handlers.put(sio.value(), clazz);
            }
            clazz.add(c);
            if (logger.isLoggable(Level.FINE))
                logger.log(Level.FINE, "Registering: {0}", c.getName());
        }
        ctx.setAttribute("handlerMap", handlers);
        
        
    }

}
