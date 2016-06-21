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

/**
 * Represents the event triggered when an exception is thrown from
 * a subscriber code.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see ExceptionContext
 */
public class ExceptionEvent implements HandlerChainAware {
    private Throwable exception;
    private ExceptionContext exceptionContext;
    private HandlerChain handlerChain;

    /**
     * Instantiate the exception event.
     *
     * @param exception the exception that was thrown from the subscriber code.
     * @param exceptionContext contextual information about the {@code exception}.
     * */
    public ExceptionEvent(Throwable exception, ExceptionContext exceptionContext) {
        this.exception = exception;
        this.exceptionContext = exceptionContext;
    }

    /**
     * Gets the contextual information about the exception triggering this event.
     * */
    public ExceptionContext getExceptionContext() {
        return exceptionContext;
    }

    /**
     * Gets the exception triggering this event.
     * */
    public Throwable getException() {
        return exception;
    }

    @Override
    public void setHandlerChain(HandlerChain handlerChain) {
        this.handlerChain = handlerChain;
    }

    @Override
    public HandlerChain getHandlerChain() {
        return handlerChain;
    }

    @Override
    public String toString() {
        return "[" +
                "exception = " +
                exception.toString() +
                ", context = " +
                exceptionContext +
                "]";
    }
}
