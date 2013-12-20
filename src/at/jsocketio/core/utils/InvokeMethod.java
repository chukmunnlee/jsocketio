/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core.utils;

import at.jsocketio.api.EventObject;
import at.jsocketio.api.annotation.*;
import at.jsocketio.core.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.*;
import javax.websocket.*;
import javax.websocket.CloseReason.CloseCode;

/**
 *
 * @author projects
 */
public class InvokeMethod {
    
    private static final Logger logger = Logger.getLogger(InvokeMethod.class.getName());
    
    public static void invokeOnOpen(Session session, EndpointConfig config
            , HandlerMap handlerMap) {
        invoke(handlerMap, WhenConnect.class, session, config);
    }
    
    public static void invokeOnClose(Session session, CloseReason reason
            , HandlerMap handlerMap) {
        invoke(handlerMap, WhenDisconnect.class, session, reason);
    }
    
    public static void invokeOnTimeout(Session session, HandlerMap handlerMap) {
        invoke(handlerMap, WhenTimeout.class, session
                , new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Timeout"));
    }
    
    //Used by @WhenConnect, @WhenDisconnect and @WhenTimeout
    public static void invoke(HandlerMap handlerMap, Class<? extends Annotation> annot
            , Object... predefined) {
        for (Class<?> h: handlerMap.get()) {
            Method m = find(h, annot);
            if (null == m)
                continue;
            
            Object obj = CDIUtils.locateBean(h);
            if (null == obj)
                return;
            Object[] params = createParameters(m, predefined);
            
            if (logger.isLoggable(Level.FINE))
                    logger.log(Level.FINE, "@{0}: {1}.{2}", new Object[]{
                        annot.getSimpleName(), m.getDeclaringClass().getName(), m.getName()});
            
            try {
                m.invoke(obj, params);
            } catch (    IllegalAccessException | IllegalArgumentException 
                    | InvocationTargetException ex) {
                logger.log(Level.WARNING, "Error invoking " + m.getName(), ex);                
            }
        }
    }   
    
    public static List<String> createRegExGroup(Matcher m) {
        List<String> result = new LinkedList<>();
        int count = m.groupCount();
        if (m.matches())
            for (int i = 0; i < count; i++)
                result.add(m.group(i));        
        return (result);
    }
    
    public static void invoke(EventObject value, HandlerMap handlerMap, Session session) {
        Map<Pattern, Set<Class<?>>> result = handlerMap.get(value.getName());
        for (Pattern p: result.keySet()) {            
            List<String> regexGroup = createRegExGroup(p.matcher(value.getName()));
            for (Class<?> h: result.get(p)) {      
                Method m = find(h, WhenEvent.class);
                if (null == m)
                    continue;                
                invokeMethod(value.getName(), m, value, session, regexGroup);                
            }
        }
    }
    
    public static void invoke(JsonObject value, HandlerMap handlerMap, Session session) {
        Map<Pattern, Set<Class<?>>> result = handlerMap.get("message");
        for (Pattern p: result.keySet()) {
            Matcher matcher = p.matcher("message");
            for (Class<?> h: result.get(p)) {
                Method m = find(h, WhenJsonObject.class);
                if (null == m)
                    continue;
                invokeMethod("message", m, value, session, matcher);
            }
        }
    }
    
    public static void invoke(String value, HandlerMap handlerMap, Session session) {
        Map<Pattern, Set<Class<?>>> result = handlerMap.get("message");
        for (Pattern p: result.keySet()) {
            Matcher matcher = p.matcher("message");
            for (Class<?> h: result.get(p)) {
                Method m = find(h, WhenMessage.class);
                if (null == m)
                    continue;
                invokeMethod("message", m, value, session, matcher);
            }
        }               
    }    
    
    public static Object instantiate(Method m, String event) {
        Class<?> clazz = m.getDeclaringClass();
        Object obj = CDIUtils.locateBean(clazz);
        if (null == obj) {
            logger.log(Level.WARNING, "Cannot locate bean {0} for event {1}"
                , new Object[]{clazz.getName(), event});
            return (null);            
        }
        return (obj);
    }
    
    public static void execute(Method m, Object obj, Object[] params) {
        try {                
            m.invoke(obj, params);                                
        } catch (IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException ex) {
            logger.log(Level.WARNING, "Error invoking " + m.getName(), ex);                
        }  
    }
    
    public static void invokeMethod(String event, Method m, EventObject value
            , Session session, List<String> regexGroup) {        
        Object obj = instantiate(m, event);                        
        Object[] params = createParameters(m, value, session, regexGroup);                
        execute(m, obj, params);
    }
    
    public static void invokeMethod(String event, Method m, String value
            , Session session, Matcher matcher) {
        Object obj = instantiate(m, event);
        Object[] params = createParameters(m, value, session, matcher);
        execute(m, obj, params);          
    }
    
    public static void invokeMethod(String event, Method m, JsonObject value
            , Session session, Matcher matcher) {
        Object obj = instantiate(m, event);
        Object[] params = createParameters(m, value, session, matcher);
        execute(m, obj, params);     
    }
      
    //For @WhenConnect, @WhenDisconnect
    public static Object[] createParameters(Method m, Object... predefined) { 
        Class<?>[] paramType = m.getParameterTypes();
        Object[] params = new Object[paramType.length]; 
        for (int i = 0; i < params.length; i++) {
            for (Object o: predefined)
                if (TypeConversionUtils.isType(o, paramType[i])) {
                    params[i] = o;
                    break;
                }
        }
        return (params);
    }
            
    public static Object[] createParameters(Method m, EventObject message
            , Session session, List<String> regexGroup) {
        Class<?>[] paramType = m.getParameterTypes();        
        Annotation[][] paramAnnot = m.getParameterAnnotations();
        Object[] params = new Object[paramType.length];  
                
        for (int i = 0; i < params.length; i++) {
            if (Session.class == paramType[i]) {
                params[i] = session;
                continue;
            }
            
            if (EventObject.class == paramType[i]) {
                params[i] = message;
                continue;
            }
            
            if (JsonArray.class == paramType[i]) {
                params[i] = message.getPayload();
                continue;
            }
            
            Group group = find(paramAnnot[i]);
            if (group != null) {
                int id = group.value();
                if (id > regexGroup.size())
                    logger.log(Level.INFO, "Invalid @Group position: {0} for method {1}", 
                            new Object[]{ group.toString()
                                    , m.getDeclaringClass().getName() + "." + m.getName()});                    
                else {
                    Object value = TypeConversionUtils.convertPrimitive(regexGroup.get(id), paramType[i]);
                    if (null != value)
                        params[i] = value;
                    else
                        //Try converting to object            
                        params[i] = TypeConversionUtils.convert(regexGroup.get(id), paramType[i]);
                }                                                                       
                continue;
            }
            
            if (String.class == paramType[i])
                params[i] = message.getName();            
        }
        
        return (params);
    }
    
    public static Object[] createParameters(Method m, JsonObject message
            , Session session, Matcher matcher) {
        Class<?>[] paramType = m.getParameterTypes();        
        Object[] params = new Object[paramType.length];  
        
        for (int i = 0; i < params.length; i++) {
            //Check for Session.class
            if (Session.class == paramType[i]) {
                params[i] = session;
                continue;
            }
            
            //Check for message
            if (JsonObject.class == paramType[i]) {
                params[i] = message;
                continue;
            }      
            
            if (String.class == paramType[i])
                params[i] = message.toString();
        }
        
        return (params);
    }
    
    //public static Object[] createParameters(Method m, String message
    public static Object[] createParameters(Method m, String message
            , Session session, Matcher matcher) {
        Class<?>[] paramType = m.getParameterTypes();        
        Object[] params = new Object[paramType.length];               
        
        for (int i = 0; i < params.length; i++) {
            
            //Check for Session.class
            if (Session.class == paramType[i]) {
                params[i] = session;
                continue;
            }
            
            //Check for message
            if (String.class == paramType[i]) {
                params[i] = message;
                continue;
            }
            
            //Try converting to primitive type
            Object value = TypeConversionUtils.convertPrimitive(message, paramType[i]);
            if (null != value) {
                params[i] = value;
                continue;
            }            
            
            //Try converting to object            
            params[i] = TypeConversionUtils.convert(message, paramType[i]);
        }
        
        return (params);
    }   
    
    public static Method find(Class<?> clazz, Class<? extends Annotation> annot) {
        for (Method m: clazz.getDeclaredMethods())
            if (m.isAnnotationPresent(annot))
                return (m);
        return (null);
    }
    
    public static Group find(Annotation[] paramAnnot) {
        for (int i = 0; i < paramAnnot.length; i++)
            if (paramAnnot[i].annotationType() == Group.class)
                return (Group.class.cast(paramAnnot[i]));
        return (null);
    }
}
