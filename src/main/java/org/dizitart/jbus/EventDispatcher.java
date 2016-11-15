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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Internal class for dispatching event to its registered subscribers.
 * It registers itself to a JVM shutdown hook to shutdown gracefully.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
class EventDispatcher {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ListenersRegistry listenersRegistry;
    private boolean isShutdownInitiated = false;
    private ErrorHandler errorHandler;

    EventDispatcher(ListenersRegistry listenersRegistry) {
        this.listenersRegistry = listenersRegistry;
        errorHandler = new ErrorHandler(listenersRegistry, this);
    }

    /**
     * Dispatches the event to a handler chain.
     *
     * If the submitted event is of type {@link HandlerChainAware}, before each invocation,
     * it will check if an interruption is signalled from the the client code.
     * If signalled then no more invocation will happen for that event.
     *
     * */
    void dispatch(Object event, DefaultHandlerChain handlerChain) {
        // iterate over the subscriber list. If shutdown is initiated already,
        // do not dispatch anything. Otherwise, check the execution mode
        // of the subscriber. If set to async, dispatch it again to the
        // executor service, otherwise invoke the subscriber synchronously.

        HandlerChainAware handlerChainAware;
        if (event instanceof HandlerChainAware) {
            handlerChainAware = (HandlerChainAware) event;
            handlerChainAware.setHandlerChain(handlerChain);
        }

        for (ListenerMethod listenerMethod : handlerChain) {
            if (isShutdownInitiated) {
                logger.debug("Shutdown initiated. No more dispatching.");
                return;
            }

            // check for interruption signal before each invocation. if interrupted,
            // no more invocation will happen from the handler chain.
            if (!handlerChain.interrupt) {
                if (listenerMethod.async) {
                    logger.debug("Executing listener asynchronously " + listenerMethod);
                    dispatchSingleAsync(event, listenerMethod);
                } else {
                    logger.debug("Executing listener " + listenerMethod);
                    dispatchSingle(event, listenerMethod);
                }
            }
        }
    }

    /**
     * Dispatches the subscriber and the event to the executor service for
     * asynchronous execution.
     * */
    private void dispatchSingleAsync(final Object event, final ListenerMethod listenerMethod) {
        executorService.submit(new Runnable() {
            public void run() {
                dispatchSingle(event, listenerMethod);
            }
        });
    }

    /**
     * Executes the subscriber synchronously.
     * */
    private void dispatchSingle(Object event, ListenerMethod listenerMethod) {
        try {
            if (listenerMethod.holdWeakReference) {
                Object listener = listenerMethod.weakListener.get();
                if (listener == null) {
                    // if underlying object is no more, remove it from the runtime and
                    // all of its associations.
                    listenersRegistry.removeWeakListener(listenerMethod.weakListener);
                } else {
                    // invoke synchronously.
                    listenerMethod.method.invoke(listener, event);
                }
            } else {
                listenerMethod.method.invoke(listenerMethod.target, event);
            }
        } catch (Exception e) {
            if (e.getCause() != null) {
                logger.error("Error occurred while invoking " + listenerMethod, e.getCause());
                errorHandler.handle(event, listenerMethod, e.getCause());
            } else {
                logger.error("Error occurred while invoking " + listenerMethod, e);
                errorHandler.handle(event, listenerMethod, e);
            }
        }
    }

    /**
     * Creates a shutdown hook.
     * */
    private Runnable getShutdownHook() {
        return new Runnable() {
            @Override
            public void run() {
                // set a flag to indicate the shutdown has been initiated, so that
                // no more dispatch happens. Then gracefully shutdown the executor.
                isShutdownInitiated = true;
                if (!executorService.isShutdown()) {
                    logger.debug("Shutting down executor, no more event will be dispatched.");
                    executorService.shutdown();
                    logger.debug("Executor has been shutdown gracefully.");
                }
            }
        };
    }

    /**
     * Registers a shutdown hook to the JVM for graceful shutdown of the event bus.
     * */
    void addShutdownHook() {
        // set a shutdown hook to gracefully shutdown executors
        Runtime.getRuntime().addShutdownHook(new Thread(getShutdownHook()));
    }
}
