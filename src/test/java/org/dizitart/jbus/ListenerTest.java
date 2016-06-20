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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class ListenerTest {
    private JBus jBus;

    @Before
    public void setUp() {
        jBus = new JBus();
    }

    @Test
    public void testSubscription() {
        Listener listener = new Listener();
        jBus.register(listener);
        jBus.post(new Event());

        assertEquals(listener.listen1Counter.intValue(), 1);

        jBus.deregister(listener);
        jBus.post(new Event());

        assertEquals(listener.listen1Counter.intValue(), 1);
    }

    @Test(expected = NullPointerException.class)
    public void testNullPosting() {
        Listener listener = new Listener();
        jBus.register(listener);
        jBus.post(null);
    }

    @Test(expected = JBusException.class)
    public void testVarargsRegistration() {
        VarargsListener listener = new VarargsListener();
        jBus.register(listener);
    }

    @Test(expected = JBusException.class)
    public void testArrayRegistration() {
        ArrayListener listener = new ArrayListener();
        jBus.register(listener);
    }

    @Test(expected = JBusException.class)
    public void testMultiParameterRegistration() {
        MultiParameterListener listener = new MultiParameterListener();
        jBus.register(listener);
    }

    @Test(expected = JBusException.class)
    public void testMultipleRegister() {
        Listener listener = new Listener();
        jBus.register(listener);
        jBus.register(listener);
    }

    @Test
    public void testMultiplePost() {
        Listener listener = new Listener();
        jBus.register(listener);
        jBus.post(new Event());

        assertEquals(listener.listen1Counter.intValue(), 1);

        jBus.post(new Event());
        jBus.post(new Event());

        assertEquals(listener.listen1Counter.intValue(), 3);

        jBus.deregister(listener);
        jBus.post(new Event());

        assertEquals(listener.listen1Counter.intValue(), 3);
    }

    @Test
    public void testConcurrentPost() throws InterruptedException {
        Listener listener = new Listener();
        jBus.register(listener);
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < 10; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    jBus.post(new Event());
                }
            });
        }

        // wait for all threads to complete
        Thread.sleep(1000);

        assertEquals(listener.listen1Counter.intValue(), 10);

        jBus.deregister(listener);

        for (int i = 0; i < 10; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    jBus.post(new Event());
                }
            });
        }
        // wait for all threads to complete
        Thread.sleep(1000);

        assertEquals(listener.listen1Counter.intValue(), 10);
    }

    @Test
    public void testMultiEvent() {
        MultiListener listener = new MultiListener();
        jBus.register(listener);

        jBus.post(new Event());

        assertEquals(listener.eventListenerCalled, 1);
        assertEquals(listener.intListenerCalled, 0);

        jBus.post(3);

        assertEquals(listener.eventListenerCalled, 1);
        assertEquals(listener.intListenerCalled, 1);

        jBus.deregister(listener);

        jBus.post(new Event());
        jBus.post(4);

        assertEquals(listener.eventListenerCalled, 1);
        assertEquals(listener.intListenerCalled, 1);

        jBus.register(listener);

        jBus.post(new Event());
        jBus.post(4);

        assertEquals(listener.eventListenerCalled, 2);
        assertEquals(listener.intListenerCalled, 2);
    }

    @Test(expected = JBusException.class)
    public void testNoArgListener() {
        NoArgListener listener = new NoArgListener();
        jBus.register(listener);
    }

    @Test(expected = JBusException.class)
    public void testNoSubscriberListener() {
        NoSubscriberListener listener = new NoSubscriberListener();
        jBus.register(listener);
    }

    private class Event {}

    private class Listener {
        AtomicInteger listen1Counter = new AtomicInteger(0);

        @Subscribe
        public void listen1(Event event){
            listen1Counter.getAndIncrement();
        }

        public void listen2(Event event) {
            throw new RuntimeException("should not be called.");
        }

        @Subscribe
        public void listen3(Integer event) {
            throw new RuntimeException("should not be called.");
        }
    }

    private class VarargsListener {
        @Subscribe
        public void listen(Event... events) {

        }
    }

    private class ArrayListener {
        @Subscribe
        public void listen(Event[] events) {

        }
    }

    private class MultiParameterListener {
        @Subscribe
        public void listen(Event event, Integer integer) {

        }
    }

    private class MultiListener {
        int eventListenerCalled;
        int intListenerCalled;

        @Subscribe
        void listen(Event event) {
            eventListenerCalled++;
        }

        @Subscribe
        void listen(Integer integer) {
            intListenerCalled++;
        }
    }

    private class NoArgListener {
        @Subscribe
        private void listen(){
        }
    }

    private class NoSubscriberListener {
        private void listen(Event event) {
        }
    }
}
