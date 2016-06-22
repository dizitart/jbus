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

import java.lang.reflect.Method;

/**
 * A class to provide relevant information about
 * an exception thrown from subscriber code during event
 * handling.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see ExceptionEvent
 */
public class ExceptionContext {
    private Object listener;
    private Object event;
    private Method subscribedMethod;

    /**
     * Instantiate a new exception context.
     *
     * @param listener listener object.
     * @param event the event which was being handled
     * @param subscribedMethod the subscribed method which has thrown the exception
     * */
    public ExceptionContext(Object listener, Object event, Method subscribedMethod) {
        this.listener = listener;
        this.event = event;
        this.subscribedMethod = subscribedMethod;
    }

    /**
     * Gets the subscribed method which has thrown the exception.
     *
     * @return the subscribed method.
     * */
    public Method getSubscribedMethod() {
        return subscribedMethod;
    }

    /**
     * Gets the event which was being handled when the exception was thrown.
     *
     * @return the event which was being handled.
     * */
    public Object getEvent() {
        return event;
    }

    /**
     * Gets the listener object.
     *
     * @return the listener object.
     * */
    public Object getListener() {
        return listener;
    }

    @Override
    public String toString() {
        return "[" +
                "method = " +
                subscribedMethod.getName() +
                ", listener = " +
                listener +
                ", event = " +
                event +
                "]";
    }
}
