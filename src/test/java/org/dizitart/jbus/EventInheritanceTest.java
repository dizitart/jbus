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
public class EventInheritanceTest {
    private JBus jBus;

    @Before
    public void setUp() {
        jBus = new JBus();
    }

    @Test
    public void testEventInheritance() {
        Listener listener = new Listener();
        jBus.register(listener);

        jBus.post(new BaseEvent());
        assertEquals(listener.listenBaseEventCalled, 1);
        assertEquals(listener.listenEventCalled, 0);
        assertEquals(listener.listenAbstractEventCalled, 0);
        assertEquals(listener.listenUserEventCalled, 0);

        jBus.post(new UserEvent());
        assertEquals(listener.listenBaseEventCalled, 1);
        assertEquals(listener.listenEventCalled, 0);
        assertEquals(listener.listenAbstractEventCalled, 0);
        assertEquals(listener.listenUserEventCalled, 1);

        jBus.post(new Event());
        assertEquals(listener.listenBaseEventCalled, 1);
        assertEquals(listener.listenEventCalled, 1);
        assertEquals(listener.listenAbstractEventCalled, 0);
        assertEquals(listener.listenUserEventCalled, 1);

        jBus.deregister(listener);
        jBus.post(new BaseEvent());
        jBus.post(new UserEvent());
        jBus.post(new Event());
        assertEquals(listener.listenBaseEventCalled, 1);
        assertEquals(listener.listenEventCalled, 1);
        assertEquals(listener.listenAbstractEventCalled, 0);
        assertEquals(listener.listenUserEventCalled, 1);
    }

    private class BaseEvent {}
    private class Event extends BaseEvent {}
    private abstract class AbstractEvent {}
    private class UserEvent extends AbstractEvent {}

    private class Listener {
        int listenEventCalled;
        int listenBaseEventCalled;
        int listenAbstractEventCalled;
        int listenUserEventCalled;

        @Subscribe
        void listen(BaseEvent event) {
            listenBaseEventCalled++;
        }

        @Subscribe
        void listen(Event event) {
            listenEventCalled++;
        }

        @Subscribe
        void listen(AbstractEvent event) {
            listenAbstractEventCalled++;
        }

        @Subscribe
        void listen(UserEvent event) {
            listenUserEventCalled++;
        }
    }
}
