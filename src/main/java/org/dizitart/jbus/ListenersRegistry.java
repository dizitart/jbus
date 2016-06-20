/*
 * Copyright (c) 2016 JBus author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.jbus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.dizitart.jbus.ReflectionUtil.findSubscribedMethods;

/**
 * A class to hold the records of listeners registered to the event bus runtime.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
class ListenersRegistry {
    private Log logger = LogFactory.getLog(getClass());

    // keep track of event and its registered subscribed methods
    private Map<Class<?>, List<ListenerMethod>> registry =
            new ConcurrentHashMap<Class<?>, List<ListenerMethod>>();
    // cache to keep track of all strong referenced listener object
    private final List<Object> subscriberCache = new CopyOnWriteArrayList<Object>();
    // cache to keep track of all weak referenced listener object
    private final List<WeakReference<Object>> weakSubscriberCache =
            new CopyOnWriteArrayList<WeakReference<Object>>();
    private final Object lock = new Object();

    /**
     * Registers an object in the listener registry. If weak is set,
     * it will create a weak reference of the listener object and register.
     * Otherwise it will store a strong reference of the listener object.
     *
     * */
    void register(Object listener, boolean weak) {
        WeakReference<Object> weakListener = null;

        // synchronize the search in the cache, to check if the listener
        // has already been registered.
        synchronized (lock) {
            if (weak) {
                // if the weak is set, check if a weak reference of the object is
                // kept in the cache or not.
                if (containsWeak(listener)) {
                    logger.error(listener + " has already been registered.");
                    throw new JBusException(listener + " has already been registered.");
                }
                // create a weak reference of the object and add it to the cache
                weakListener = new WeakReference<Object>(listener);
                weakSubscriberCache.add(weakListener);
                logger.debug(listener + " added to the weak subscriber cache.");
            } else {
                if (subscriberCache.contains(listener)) {
                    // if listener is found in the strong referenced cache, throw
                    logger.error(listener + " has already been registered.");
                    throw new JBusException(listener + " has already been registered.");
                }
                // add the object to the strong referenced cache
                subscriberCache.add(listener);
                logger.debug(listener + " added to the subscriber cache.");
            }
        }

        // extract all subscribed methods from the listener and its super class and interfaces.
        // up to this point, we hold a strong reference of the object, beyond this point,
        // if the weak is set, we will not hold any strong reference of the object.
        List<ListenerMethod> subscribedMethods = findSubscribedMethods(listener.getClass());
        if (subscribedMethods == null || subscribedMethods.isEmpty()) {
            logger.error(listener + " does not have any method marked with @Subscribe.");
            throw new JBusException(listener + " does not have any method marked with @Subscribe.");
        }

        for (ListenerMethod listenerMethod : subscribedMethods) {
            if (weak) {
                listenerMethod.weakListener = weakListener;
                listenerMethod.holdWeakReference = true;
            } else {
                listenerMethod.target = listener;
                listenerMethod.holdWeakReference = false;
            }

            Class<?> eventType = listenerMethod.eventType;
            if (registry.containsKey(eventType)) {
                List<ListenerMethod> listenerMethods = registry.get(eventType);

                // check ListenerMethod's equals method
                if (!listenerMethods.contains(listenerMethod)) {
                    listenerMethods.add(listenerMethod);
                    logger.debug(listenerMethod + " has been registered.");
                } else {
                    logger.debug(listenerMethod + " has already been registered.");
                }
            } else {
                List<ListenerMethod> listenerMethods = new CopyOnWriteArrayList<ListenerMethod>();
                listenerMethods.add(listenerMethod);
                registry.put(listenerMethod.eventType, listenerMethods);
                logger.debug(listenerMethod + " has been registered.");
            }
        }
    }

    /**
     * De-registers a listener object.
     * */
    void deregister(Object listener) {
        // synchronize the search. search and remove are put in one place
        // to avoid unnecessary looping.
        synchronized (lock) {
            // we need to check in both the caches, as we don't know how
            // the listener object was registered. If it was a weak reference,
            // there are chances, that underlying object has already been
            // collected by GC. In that case while iterating we will cleanup
            // the cache as we go, if we find such weak reference.
            //
            // But one catch here is that we will never know if we are trying
            // to deregister an object which was never registered before, hence
            // we can not throw such exception.

            for (WeakReference<Object> weakRef : weakSubscriberCache) {
                Object element = weakRef.get();
                if (element == null) {
                    // underlying object is no more. remove it from the cache already.
                    if (weakSubscriberCache.remove(weakRef)) {
                        logger.debug(weakRef + " removed from cache as underlying object does not exist anymore.");
                    }
                } else {
                    if (element.equals(listener)) {
                        // found in weak cache, remove it and break
                        if (weakSubscriberCache.remove(weakRef)) {
                            logger.debug(listener + " removed from the weak subscriber reference cache.");
                        }
                        break;
                    }
                }
            }

            for (Object object : subscriberCache) {
                if (object.equals(listener)) {
                    // found in strong cache, remove it break
                    if (subscriberCache.remove(listener)) {
                        logger.debug(listener + " removed from the subscriber cache.");
                    }
                    break;
                }
            }
        }

        // remove the listener and its subscribed methods from the registry,
        // we are not 100% sure if we are dealing with weak referenced object
        // or not, hence we are passing false.
        removeFromRegistry(listener, false);
    }

    /**
     * Get all registered subscriber information for an event.
     * */
    List<ListenerMethod> getSubscribers(Object event) {
        if (event != null) {
            Class<?> eventType = event.getClass();
            // loop through the registry to get all subscribed method
            if (registry.containsKey(eventType)) {
                return registry.get(eventType);
            }
        }
        return null;
    }

    /**
     * Checks if an object's weak reference is kept in the cache or not.
     * */
    private boolean containsWeak(Object listener) {
        for (WeakReference<Object> weakRef : weakSubscriberCache) {
            Object element = weakRef.get();
            if (element == null) {
                // if the object has already been claimed by the GC,
                // remove it from the cache.
                if (weakSubscriberCache.remove(weakRef)) {
                    logger.debug(weakRef + " removed from cache as underlying object does not exist anymore.");
                }
            } else {
                if (element.equals(listener)) {
                    // if the listener is found equal to any weak referenced object,
                    // return true immediately.
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes a weak referenced listener from the bus runtime.
     * */
    void removeWeakListener(WeakReference<?> weakRef) {
        // first remove it from cache
        if (weakSubscriberCache.remove(weakRef)) {
            logger.debug("Weak reference " + weakRef + " removed from cache.");
        }

        // clean up the registry. Most of the time removeWeakListener is called when
        // underlying object is garbage collected and we want to remove the weak
        // reference from the runtime. So weakRef.get() will always return null
        // in that case.
        //
        // But fortunately, removeFromRegistry method will cleanup the cache while
        // iterating if it finds a weak reference which does not hold any object
        // reference any more.
        removeFromRegistry(weakRef.get(), true);
    }

    /**
     * Removes the listener from the registry. It also cleans up the registry,
     * while iterating, if it finds any weak reference which does not hold any
     * object reference any more, i.e. the underlying object has been garbage collected.
     *
     * @param confirmedWeak if we know listener was registered as a weak reference
     * */
    private void removeFromRegistry(Object listener, boolean confirmedWeak) {
        // iterate the whole registry map
        for (Map.Entry<Class<?>, List<ListenerMethod>> entry : registry.entrySet()) {
            List<ListenerMethod> subscribedMethods = entry.getValue();
            for (ListenerMethod listenerMethod : subscribedMethods) {
                if (confirmedWeak || listenerMethod.holdWeakReference) {
                    // if confirmedWeak is true or listener method holds weak reference,
                    // check if underlying object is still valid.

                    // if not valid clean up. remove the entry from cache and
                    // from the event's subscriber list.
                    Object reference = listenerMethod.weakListener.get();
                    if (reference == null) {
                        // remove from event subscribers list
                        if (subscribedMethods.remove(listenerMethod)) {
                            logger.debug(listenerMethod + " has been un-registered as the " +
                                    "target has been garbage collected.");
                        }
                        // remove that invalid weak reference from cache
                        if (weakSubscriberCache.remove(listenerMethod.weakListener)) {
                            logger.debug(listenerMethod.weakListener +
                                    " removed from cache as underlying object does not exist anymore.");
                        }
                    } else if (reference.equals(listener)) {
                        if (subscribedMethods.remove(listenerMethod)) {
                            logger.debug(listenerMethod + " has been un-registered.");
                        }
                    }
                } else {
                    if (listenerMethod.target.equals(listener)) {
                        if (subscribedMethods.remove(listenerMethod)) {
                            logger.debug(listenerMethod + " has been un-registered.");
                        }
                    }
                }
            }
        }
    }
}
