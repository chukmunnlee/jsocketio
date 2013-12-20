/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.*;

/**
 *
 * @author projects
 */
//@Qualifier
//@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WhenConnect { }
