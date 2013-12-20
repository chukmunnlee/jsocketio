/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author projects
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketIO {
    
    public static final String DEFAULT_EVENT_NAME = "message";    
    
    public String value() default DEFAULT_EVENT_NAME;
}
