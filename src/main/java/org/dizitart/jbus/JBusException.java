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
 * Thrown if there are any exception in event bus runtime.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class JBusException extends RuntimeException {
    public JBusException(String message) {
        super(message);
    }

    public JBusException(String message, Throwable e) {
        super(message, e);
    }
}
