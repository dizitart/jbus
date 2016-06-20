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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class ListenerInheritanceTest {

    private JBus jBus;

    @Before
    public void setUp() {
        jBus = new JBus();
    }

    @Test
    public void testInheritance() {
        ChildListener listener = new ChildListener();
        jBus.register(listener);
        jBus.post(new UserEvent());

        assertTrue(listener.privateChildListenCalled == 1);
        assertTrue(listener.publicChildListenCalled == 1);
        assertTrue(listener.publicSuperListenCalled == 1);
        assertTrue(listener.privateSuperListenCalled == 1);
        assertFalse(listener.superCommonCalled == 1);
        assertTrue(listener.childCommonCalled == 1);

        jBus.deregister(listener);
        jBus.post(new UserEvent());

        assertTrue(listener.privateChildListenCalled == 1);
        assertTrue(listener.publicChildListenCalled == 1);
        assertTrue(listener.publicSuperListenCalled == 1);
        assertTrue(listener.privateSuperListenCalled == 1);
        assertFalse(listener.superCommonCalled == 1);
        assertTrue(listener.childCommonCalled == 1);
    }

    @Test
    public void testAbstractMethod() {
        AnotherChildListener listener = new AnotherChildListener();
        jBus.register(listener);
        jBus.post(new UserEvent());

        assertTrue(listener.abstractListenCalled == 1);
        assertTrue(listener.superListenCalled == 1);

        jBus.deregister(listener);
        jBus.post(new UserEvent());

        assertTrue(listener.abstractListenCalled == 1);
        assertTrue(listener.superListenCalled == 1);
    }

    @Test
    public void testInterfaceAbstractClassMethod() {
        SomeListener listener = new SomeListener();
        SomeOtherListener someOtherListener = new SomeOtherListener();
        jBus.register(someOtherListener);
        jBus.register(listener);
        jBus.post(new UserEvent());

        assertEquals(listener.listen2Called, 1);
        assertEquals(listener.listenCalled, 1);
        assertEquals(listener.listen3Called, 1);
        assertEquals(someOtherListener.listenCalled, 1);

        jBus.deregister(someOtherListener);
        jBus.deregister(listener);
        jBus.post(new UserEvent());

        assertEquals(listener.listen2Called, 1);
        assertEquals(listener.listenCalled, 1);
        assertEquals(listener.listen3Called, 1);
        assertEquals(someOtherListener.listenCalled, 1);
    }

    private class UserEvent {}

    private class SuperListener {
        int publicSuperListenCalled;
        int privateSuperListenCalled;
        int superCommonCalled;

        @Subscribe
        public void publicListen(UserEvent event) {
            publicSuperListenCalled++;
        }

        @Subscribe
        private void privateListen(UserEvent event) {
            privateSuperListenCalled++;
        }

        @Subscribe
        public void common(UserEvent event) {
            superCommonCalled++;
        }

        public void anotherMethod(Object object) {
            throw new RuntimeException("This method should not be called");
        }
    }

    private class ChildListener extends SuperListener {
        int publicChildListenCalled;
        int privateChildListenCalled;
        int childCommonCalled;

        @Subscribe
        public void publicListen2(UserEvent event) {
            publicChildListenCalled++;
        }

        @Subscribe
        private void privateListen(UserEvent event) {
            privateChildListenCalled++;
        }

        @Subscribe
        public void common(UserEvent event) {
            childCommonCalled++;
        }

        private void anotherMethod2(Object object) {
            throw new RuntimeException("This method should not be called");
        }
    }

    private abstract class AbstractListener {
        int superListenCalled;

        @Subscribe
        abstract void abstractListen(UserEvent event);

        @Subscribe
        void superListen(UserEvent event) {
            superListenCalled++;
        }
    }

    private class AnotherChildListener extends AbstractListener {
        int abstractListenCalled;

        void abstractListen(UserEvent event) {
            abstractListenCalled++;
        }
    }

    private interface SomeInterface {
        @Subscribe
        void listen(UserEvent event);
    }

    private abstract class SomeAbstractClass {
        int listen2Called;

        @Subscribe
        void listen2(UserEvent event) {
            listen2Called++;
        }

        abstract void listen3(UserEvent event);
    }

    private class SomeListener extends SomeAbstractClass implements SomeInterface {
        int listen3Called;
        int listenCalled;

        @Subscribe
        @Override
        void listen3(UserEvent event) {
            listen3Called++;
        }

        @Override
        public void listen(UserEvent event) {
            listenCalled++;
        }
    }

    private class SomeOtherListener implements SomeInterface {
        int listenCalled;

        @Subscribe
        @Override
        public void listen(UserEvent event) {
            listenCalled++;
        }
    }
}
