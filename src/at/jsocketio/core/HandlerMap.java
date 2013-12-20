/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.regex.Pattern;
import javax.enterprise.context.*;

/**
 *
 * @author projects
 */
@ApplicationScoped
public class HandlerMap {
    
    private final Map<Pattern, Set<Class<?>>> handlerMap = new HashMap<>();
    private final Set<Class<?>> allClasses = new HashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();   
    
    public void register(String event, Set<Class<?>> handlers) {
        try (Lockable l = Lockable.lock(lock.writeLock())) {
            handlerMap.put(Pattern.compile(event), handlers);
            allClasses.addAll(handlers);
        }  catch (IOException ex) { }
    }
    
	@SuppressWarnings("unchecked")
    public Set<Class<?>> get() {
        try (Lockable l = Lockable.lock(lock.readLock())) {
            return (Collections.unmodifiableSet(allClasses));
        } catch (IOException ex) {}
        return (Collections.EMPTY_SET);
    }
    
    public Set<Method> get(Class<? extends Annotation> annot) {
        Set<Method> set = new HashSet<>();
        try (Lockable l = Lockable.lock(lock.readLock())) {
            for (Class<?> c: allClasses)
                for (Method m: c.getDeclaredMethods()) {
                    if (!m.isAnnotationPresent(annot))
                        continue;
                    set.add(m);
                    break;
                }
        } catch (IOException ex) { }
        return (set);
    }   
    
    public Map<Pattern, Set<Class<?>>> get(String event) {
        Map<Pattern, Set<Class<?>>> result = new HashMap<>();
        try (Lockable l = Lockable.lock(lock.readLock())) {
            for (Pattern p: handlerMap.keySet()) {
                if (p.matcher(event).matches())
                    result.put(p, handlerMap.get(p));
            }
        } catch (IOException ex) { }
        return (result);
    }
    
//    public Set<Class<?>> get(String event) {                
//        try (Lockable l = Lockable.lock(lock.readLock())) {
//            Pattern p = getPattern(event);
//            if (null != p)                
//                return (Collections.unmodifiableSet(handlerMap.get(p)));          
//        } catch (IOException ex) { }
//        return (Collections.EMPTY_SET);
//    }          
}
