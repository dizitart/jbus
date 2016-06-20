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
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.ref.WeakReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * @author Anindya Chatterjee.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({
        WeakReference.class,
        ListenersRegistry.class
})
public class WeakListenerTest {

    private JBus jBus;

    private WeakReference<Listener> weakReference;

    @Before
    public void setUp() {
        jBus = new JBus();
    }

    @Test
    public void testWeakListener() throws Exception {
        Listener listener = new Listener();
        WeakReference<Listener> listenerWeakReference =
                new WeakReference<Listener>(listener);

        weakReference = spy(listenerWeakReference);
        whenNew(WeakReference.class).withAnyArguments().thenReturn(weakReference);

        jBus.registerWeak(listener);
        UserEvent userEvent = new UserEvent();
        jBus.post(userEvent);
        assertTrue(userEvent.executed);

        Mockito.verify(weakReference, atLeast(1)).get();

        weakReference.clear();
        System.gc();

        userEvent = new UserEvent();
        assertFalse(userEvent.executed);
        jBus.post(userEvent);
        assertFalse(userEvent.executed);
    }

    private class UserEvent {
        boolean executed = false;
    }

    private class Listener {
        @Subscribe
        private void listen(UserEvent event) {
            if (event != null) {
                event.executed = true;
            }
        }
    }
}
