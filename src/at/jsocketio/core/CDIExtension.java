/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import at.jsocketio.api.annotation.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;
import javax.enterprise.event.*;
import javax.enterprise.inject.spi.*;

/**
 *
 * @author projects
 */
public class CDIExtension implements Extension {
    
    private static final Logger logger = Logger.getLogger(CDIExtension.class.getName());
    
    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbdEvt, BeanManager bm) {
        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "BeforeBeanDiscovery phase");
    }
    
    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat
            , BeanManager bm) {                
        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "ProcessAnnotatedType phase");
    }   
    
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abdEvt
            , BeanManager bm) {
        
        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "AfterBeanDiscovery phase");    
    }
    
}
