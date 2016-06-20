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

/**
 * Interface to be implemented by event that wish to be aware of
 * the {@link HandlerChain}.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public interface HandlerChainAware {
    /**
     * Callback that supplies the instance of the {@link HandlerChain}
     * to the event.
     *
     * @param handlerChain instance of the event handler chain.
     * */
    void setHandlerChain(HandlerChain handlerChain);

    /**
     * Returns the {@link HandlerChain} instance associated with the event.
     *
     * @return {@link HandlerChain} instance.
     * */
    HandlerChain getHandlerChain();
}
