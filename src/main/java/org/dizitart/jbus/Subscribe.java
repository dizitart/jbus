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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes a method eligible to respond to an event.
 *
 * <p>It should be applied to a method (with any access modifier) which has
 * only one argument with the type of the event. If it is applied on a method
 * having no parameter or more than one parameters, during registration, event bus
 * runtime will throw a {@link JBusException}.
 * </p>
 *
 * If a method in an interface or an abstract class is marked with this annotation,
 * all the overridden methods will be eligible for subscription.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see JBus#register(Object)
 * @see JBus#registerWeak(Object)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Subscribe {

    /**
     * Declares whether the subscribed method will be invoked asynchronously.
     *
     * <p>Defaults to {@code false}</p>.
     *
     * @return async flag.
     * */
    boolean async() default false;
}
