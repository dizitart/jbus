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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class HandlerChainAwareTest {
    private JBus jBus;

    @Before
    public void setUp() {
        jBus = new JBus();
    }

    @Test
    public void testHandlerChainAwareInvocation() {
        // simulate 10 event handler for HandlerChainAwareEvent
        for (int i = 0; i < 10; i++) {
            jBus.register(new Listener());
        }

        HandlerChainAwareEvent event = new HandlerChainAwareEvent();
        jBus.post(event);
        assertEquals(event.invocationCount, 6);

        event = new HandlerChainAwareEvent();
        jBus.post(event);
        // this time it will not interrupt as flag set to false in listeners
        assertEquals(event.invocationCount, 10);
    }

    @Test
    public void testWeakHandlerChainAwareInvocation() {
        // simulate 10 event handler for HandlerChainAwareEvent
        for (int i = 0; i < 10; i++) {
            jBus.registerWeak(new Listener());
        }

        HandlerChainAwareEvent event = new HandlerChainAwareEvent();
        jBus.post(event);
        assertEquals(event.invocationCount, 6);

        event = new HandlerChainAwareEvent();
        jBus.post(event);
        // this time it will not interrupt as flag set to false in listeners
        assertEquals(event.invocationCount, 10);
    }

    private class HandlerChainAwareEvent implements HandlerChainAware {
        private HandlerChain handlerChain;
        int invocationCount;

        @Override
        public void setHandlerChain(HandlerChain handlerChain) {
            this.handlerChain = handlerChain;
        }

        @Override
        public HandlerChain getHandlerChain() {
            return handlerChain;
        }
    }

    private class Listener {
        private boolean flag = true;

        @Subscribe
        public void handle(HandlerChainAwareEvent event) {
            if (event.invocationCount == 5 && flag) {
                event.getHandlerChain().interrupt();
            }
            event.invocationCount++;
            // so that in next invocation, it will  not interrupt
            flag = false;
        }
    }
}
