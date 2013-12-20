/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core.utils;

import java.lang.reflect.*;
import java.util.logging.*;

/**
 *
 * @author projects
 */
public class TypeConversionUtils {
    
    private static final Logger logger = Logger.getLogger(TypeConversionUtils.class.getName());
    
    public static Object convertPrimitive(String value, Class<?> c) throws NumberFormatException {        
        if (String.class == c)
            return (value);
        if (byte.class == c)
            return (Byte.parseByte(value));
        if (short.class == c)
            return (Short.parseShort(value));
        if (int.class == c)
            return (Integer.parseInt(value));
        if (long.class == c)
            return (Long.parseLong(value));
        if (float.class == c)
            return (Float.parseFloat(value));
        if (double.class == c)
            return (Double.parseDouble(value));
        return (null);
    }        
    
	@SuppressWarnings("unchecked")
    public static <T> T convert(String value, Class<T> c) {
        T result = null;
        Constructor<?> cons = null;
        try {
            cons = c.getDeclaredConstructor(String.class);
        } catch (NoSuchMethodException ex) { }
        if (null != cons) 
            try {
                result = (T)cons.newInstance(value);
            } catch (IllegalAccessException | IllegalArgumentException 
                    | InstantiationException | InvocationTargetException ex) {
                //Fall through to see if we can use valueOf()
                logger.log(Level.WARNING, "Cannot instantiate type: {0}", c.getName());
            }
        
        Method method = null;
        try {            
            method = c.getDeclaredMethod("valueOf", String.class);
            int mods = method.getModifiers();
            if (Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
                result = (T)method.invoke(null, value);
            }
        } catch (NoSuchMethodException  ex) {  
        } catch (IllegalAccessException | 
                IllegalArgumentException | InvocationTargetException | SecurityException ex) {  
            logger.log(Level.WARNING, "Trouble invoking conversion method: " 
                    + method.getDeclaringClass().getName() + "." + method.getName(), ex);
        }
        
        
        if (null == result)
            logger.log(Level.WARNING, "Cannot convert type: {0}", c.getName());
            
        return (result);
    }
    
    public static boolean isType(Object obj, Class<?> c) {
        return ((obj.getClass() == c) || c.isAssignableFrom(obj.getClass()));
    }
    
}
