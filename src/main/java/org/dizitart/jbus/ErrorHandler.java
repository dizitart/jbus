/*
 * Copyright 2016 JBus author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.jbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
class ErrorHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ListenersRegistry listenersRegistry;
    private EventDispatcher eventDispatcher;

    ErrorHandler(ListenersRegistry listenersRegistry, EventDispatcher eventDispatcher) {
        this.listenersRegistry = listenersRegistry;
        this.eventDispatcher = eventDispatcher;
    }

    void handle(Object event, ListenerMethod listenerMethod, Throwable error) {
        // create exception context
        ExceptionContext exceptionContext
                = new ExceptionContext(
                listenerMethod.holdWeakReference ? listenerMethod.weakListener.get() : listenerMethod.target,
                event, listenerMethod.method);
        // create exception event
        ExceptionEvent exceptionEvent
                = new ExceptionEvent(error, exceptionContext);

        // first check if any custom error handler is registered. If found,
        // handle it gracefully, otherwise log it and move on.
        List<ListenerMethod> errorSubscribers = listenersRegistry.getSubscribers(exceptionEvent);
        if (errorSubscribers != null && !errorSubscribers.isEmpty()) {
            logger.debug("Total error handler found for error " + error
                    + " is = " + errorSubscribers.size());
            logger.info("Dispatching error event " + exceptionEvent);
            DefaultHandlerChain errorHandlerChain = new DefaultHandlerChain(errorSubscribers);
            eventDispatcher.dispatch(exceptionEvent, errorHandlerChain);
        } else {
            logger.error("No error handler found for " + error);
        }
    }
}
