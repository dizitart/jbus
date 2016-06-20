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

import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class ErrorHandlingTest {
    private JBus jBus;

    @Before
    public void setUp() {
        jBus = new JBus();
    }

    @Test
    public void testErrorHandling() {
        Listener listener = new Listener();
        jBus.register(listener);

        jBus.post(new Event());
        assertEquals(listener.listenerCalled, 1);

        RuntimeExceptionLister exceptionLister = new RuntimeExceptionLister();
        jBus.register(exceptionLister);

        assertFalse(exceptionLister.errorHandled);
        jBus.post(new Event());

        assertEquals(listener.listenerCalled, 2);
        assertTrue(exceptionLister.errorHandled);
    }


    private class RuntimeExceptionLister {
        boolean errorHandled;

        @Subscribe
        public void listen(ExceptionEvent exceptionEvent) {
            assertNotNull(exceptionEvent);
            assertNotNull(exceptionEvent.getExceptionContext());
            errorHandled = true;
        }
    }

    private class Listener {
        int listenerCalled;

        @Subscribe
        void listen(Event event) {
            listenerCalled++;
            throw new RuntimeException("generated error");
        }
    }

    private class Event {}
}
