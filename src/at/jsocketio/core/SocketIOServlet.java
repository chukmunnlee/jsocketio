package at.jsocketio.core;

import java.io.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import static com.glines.socketio.util.IO.*;
import static at.jsocketio.core.SocketIOServlet.*;
import java.util.Map;
import java.util.Set;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author projects
 */
@WebServlet(urlPatterns = {"/socket.io/*"}, asyncSupported = true)
@MultipartConfig
public class SocketIOServlet extends HttpServlet {

    public static final String RESOURCE_BASE = "at/jsocketio/resources";
    public static final String MODIFIED_SOCKET_IO_JS = "_socket.io.js";
    
    public static final String PARAMETER_DISCONNECT = "disconnect";
    
    public static final String VERSION = "ver 0.3 alpha";

    private static final Logger logger
            = Logger.getLogger(SocketIOServlet.class.getName());

    public final static String DEFAULT_HEARTBEAT_TIMEOUT = "defaultHeartbeatTimeout";
    public final static String DEFAULT_TIMEOUT = "defaultTimeout";
    public final static String MAX_TEXT_MESSAGE_SIZE = "maxTextMessageSize";
    public final static String EXECUTOR_POOL_NAME = "executorPoolName";
    
    public static final long DEFAULT_HEARTBEAT_TIMEOUT_VALUE = 15000;
    
    private String config;
    
    @Inject private HandlerMap handlerMap;
    @Inject private SocketIOConfiguration socketIOConfig;
    @Inject private SessionMap sessionMap;

	@SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Initializing SocketIOServlet: {0}", VERSION);

        socketIOConfig.init(getServletConfig(), "socketio");

        config = ":" + (socketIOConfig.getString(DEFAULT_HEARTBEAT_TIMEOUT) == null
                ? DEFAULT_HEARTBEAT_TIMEOUT_VALUE : socketIOConfig.getString(DEFAULT_HEARTBEAT_TIMEOUT))
                + ":" + (socketIOConfig.getString(DEFAULT_TIMEOUT) == null
                ? "10000" : socketIOConfig.getString(DEFAULT_TIMEOUT)) + ":";

        config += "websocket";        
        
        Map<String, Set<Class<?>>> handlers = 
                (Map<String, Set<Class<?>>>)getServletContext().getAttribute("handlerMap");
        for (String s: handlers.keySet())
            handlerMap.register(s, handlers.get(s));
        
        String executorName = socketIOConfig.getString(EXECUTOR_POOL_NAME
                , "java:comp/DefaultManagedScheduledExecutorService");
        
        if (logger.isLoggable(Level.FINE))
            logger.log(Level.FINE, "Executor pool: {0}", executorName);

        try {
            socketIOConfig.setExecutorService((ManagedScheduledExecutorService)
                    InitialContext.doLookup(executorName));
        } catch (NamingException ex) {
            logger.log(Level.SEVERE, "Cannot lookup executor pool: {0}", executorName);
            throw new ServletException("Cannot lookup executor pool: " + executorName, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "GET {0}", req.getRequestURI());

        serve(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "POST {0}", req.getRequestURI());

        serve(req, resp);
    }

    private void serve(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if ((null == path) || (path.trim().length() <= 0) || "/".equals(path.trim())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing SocketIO transport");
            return;
        }

        path = path.startsWith("/") ? path.substring(1) : path;
        String[] parts = path.split("/");                
        
        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "request: {0}", path);
        
        if ("GET".equals(req.getMethod()) && "socket.io.js".equals(parts[0])) {
            resp.setContentType("text/javascript");
            try (InputStream is = socketIOFile(req.getParameter("raw"));
                    OutputStream os = resp.getOutputStream()) {
                copy(is, os);
            }

        } else if ("GET".equals(req.getMethod()) && "/WebSocketMain.swf".equals(parts[0])) {
            resp.setContentType("application/x-shockwave-flash");
            try (InputStream is = this.getClass().getClassLoader()
                    .getResourceAsStream(RESOURCE_BASE + "WebSocketMain.swf");
                    OutputStream os = resp.getOutputStream()) {
                copy(is, os);
            }

        } else if (parts.length <= 2) {            
            
            if (logger.isLoggable(Level.INFO))
                logger.log(Level.INFO, "HANDSHAKE: {0}", path);
            
            try (PrintWriter pw = resp.getWriter()) {
                // Format: sessionId : heartbeat : timeout : transport,transport,...
                pw.print(req.getSession().getId() + config);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            
        } else if (parts.length == 3) {

            int status = HttpServletResponse.SC_OK;            
            if (req.getParameterMap().containsKey(PARAMETER_DISCONNECT)) {
                if (logger.isLoggable(Level.INFO))
                    logger.log(Level.INFO, "Force disconnect: {0}", path);                
                try {
                    sessionMap.close(req.getSession().getId());
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Force closing socket: " + req.getSession().getId(), ex);
                    status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}                
                resp.setStatus(status);
            }
                
        } else if (logger.isLoggable(Level.INFO)) 
            logger.log(Level.INFO, "PATH: {0}", path);                        
        
    }


    private InputStream socketIOFile(String raw) throws FileNotFoundException {
        if ((null != raw) && (raw.trim().length() > 0) && Boolean.parseBoolean(raw.trim())) {
            return (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_BASE + "/socket.io.js"));
        }

        File f = (File) getServletContext().getAttribute(MODIFIED_SOCKET_IO_JS);
        if (null == f) {
            return (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_BASE + "/socket.io.js"));
        }

        return (new FileInputStream(f));
    }
}
