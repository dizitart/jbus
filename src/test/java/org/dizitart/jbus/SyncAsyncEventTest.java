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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class SyncAsyncEventTest {

    private JBus jBus;
    private Object event = new Object();

    @Before
    public void setUp() {
        jBus = new JBus();
    }

    @Test
    public void testSynchronousListener() {
        SynchronousListener listener = new SynchronousListener();
        long threadId = Thread.currentThread().getId();

        jBus.register(listener);
        jBus.post(event);

        assertTrue(listener.publicListenerCalled);
        assertTrue(listener.privateListenerCalled);
        assertEquals(threadId, listener.privateListenerThreadId);
        assertEquals(threadId, listener.publicListenerThreadId);
        assertEquals(listener.privateListenerThreadId, listener.publicListenerThreadId);
    }

    @Test
    public void testAsynchronousListener() throws InterruptedException {
        AsynchronousListener listener = new AsynchronousListener();
        long threadId = Thread.currentThread().getId();

        jBus.register(listener);
        jBus.post(event);

        // wait for all events to be dispatched and executed
        Thread.sleep(1000);

        assertTrue(listener.publicListenerCalled);
        assertTrue(listener.privateListenerCalled);
        assertNotEquals(threadId, listener.privateListenerThreadId);
        assertNotEquals(threadId, listener.publicListenerThreadId);
    }

    @Test
    public void testAsynchronousListenerWithError() throws Exception {
        AsynchronousListenerWithError listener = new AsynchronousListenerWithError();

        jBus.registerWeak(listener);

        for (int i = 0; i < 10; i++) {
            jBus.post(new Object());
        }

        // wait for all events to be dispatched and executed
        Thread.sleep(1000);

        assertTrue(listener.privateListenerCalled);
        assertEquals(listener.invocationCount, 10);
    }


    private class SynchronousListener {
        boolean publicListenerCalled;
        boolean privateListenerCalled;
        long publicListenerThreadId;
        long privateListenerThreadId;

        @Subscribe
        public void publicListener(Object event) {
            publicListenerCalled = true;
            publicListenerThreadId = Thread.currentThread().getId();
        }

        @Subscribe
        private void privateListener(Object event) {
            privateListenerCalled = true;
            privateListenerThreadId = Thread.currentThread().getId();
        }

        private void shouldNotListen(Object event) {
            throw new RuntimeException("should not execute");
        }
    }

    private class AsynchronousListener {
        boolean publicListenerCalled;
        boolean privateListenerCalled;
        long publicListenerThreadId;
        long privateListenerThreadId;

        @Subscribe(async = true)
        public void publicListener(Object event) {
            publicListenerCalled = true;
            publicListenerThreadId = Thread.currentThread().getId();
        }

        @Subscribe(async = true)
        private void privateListener(Object event) {
            privateListenerCalled = true;
            privateListenerThreadId = Thread.currentThread().getId();
        }
    }

    private class AsynchronousListenerWithError {
        boolean privateListenerCalled;
        int invocationCount;

        @Subscribe(async = true)
        private void privateListener(Object event) {
            privateListenerCalled = true;
            invocationCount++;

            // simulate failure of one of the dispatch
            if (invocationCount == 2) {
                throw new RuntimeException("generated error");
            }
        }
    }
}
