/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core.utils;

import java.lang.annotation.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.spi.*;
import javax.enterprise.inject.spi.*;
import javax.inject.*;

/**
 *
 * @author projects
 */
public class CDIUtils {
    
    private static final Logger logger = Logger.getLogger(CDIUtils.class.getName());
    
    public static Object locateBean(Class<?> c) {
        BeanManager bm = CDI.current().getBeanManager();
        Set<Bean<?>> beans = null;
        Named named = c.getAnnotation(Named.class);
        if (null != named)
            beans = bm.getBeans(named.value());
        else {
            List<Annotation> list = new LinkedList<>();
            for (Annotation a: c.getDeclaredAnnotations())
                if (a.annotationType().isAnnotationPresent(Qualifier.class))
                    list.add(a);
            Annotation[] qualifiers = new Annotation[list.size()];
            qualifiers = list.toArray(qualifiers);
            beans = bm.getBeans(c, qualifiers);            
        }
        
        Bean<?> bean = bm.resolve(beans);
        if (null == bean) {
            if (logger.isLoggable(Level.INFO))
                logger.log(Level.INFO, "Cannot resolve {0}. Not Scope annotation?"
                        , c.getName());
            Object obj = null;
            try {
                obj = c.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                logger.log(Level.INFO, "Cannot instantiate {0} with newInsance()", c.getName());
                return (null);
            }
            return (obj);
        }        
        CreationalContext<?> ctx = bm.createCreationalContext(bean);        
        return (bm.getReference(bean, c, ctx));
    }
    
}
