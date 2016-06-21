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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A reflection utility class to extract information about subscriber methods
 * from a registering listener object.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
class ReflectionUtil {
    private static Log logger = LogFactory.getLog(ReflectionUtil.class);

    /**
     * Finds all subscriber methods in the whole class hierarchy of {@code subscribedClass}.
     *
     * */
    static List<ListenerMethod> findSubscribedMethods(Class<?> subscribedClass) {
        List<ListenerMethod> listenerMethodList = new ArrayList<ListenerMethod>();
        if (subscribedClass != null) {
            Method[] declaredMethods = subscribedClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(Subscribe.class) && !method.isBridge() && !method.isSynthetic()) {
                    if (method.getParameterTypes().length != 1) {
                        logger.error(method.getName() + " has @Subscribe annotation, " +
                                "but it should have exactly 1 parameter.");
                        throw new JBusException(method.getName() + " has @Subscribe annotation, " +
                                "but it should have exactly 1 parameter.");
                    }

                    Class<?> parameterType = method.getParameterTypes()[0];
                    if (parameterType.isArray() || method.isVarArgs()) {
                        logger.error(method.getName() + " has @Subscribe annotation, " +
                                "but its parameter should not be an array or varargs.");
                        throw new JBusException(method.getName() + " has @Subscribe annotation, " +
                                "but its parameter should not be an array or varargs.");
                    }

                    method.setAccessible(true);
                    Subscribe subscribe = method.getAnnotation(Subscribe.class);
                    boolean async = subscribe.async();

                    ListenerMethod listenerMethod = new ListenerMethod(method, method.getParameterTypes()[0]);
                    listenerMethod.async = async;
                    listenerMethodList.add(listenerMethod);
                }
            }

            if (subscribedClass.getSuperclass() != null && !subscribedClass.getSuperclass().equals(Object.class)) {
                if (logger.isDebugEnabled() && !subscribedClass.getSuperclass().equals(Object.class)) {
                    logger.debug("Super class found. searching for listener methods in super class "
                            + subscribedClass.getSuperclass().getName());
                }
                List<ListenerMethod> subscribedMethods = findSubscribedMethods(subscribedClass.getSuperclass());
                listenerMethodList.addAll(subscribedMethods);
            }

            if (subscribedClass.getInterfaces() != null && subscribedClass.getInterfaces().length > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Interface found. searching for listener methods in interfaces.");
                }
                for (Class<?> interfaceClass : subscribedClass.getInterfaces()) {
                    List<ListenerMethod> subscribedMethods = findSubscribedMethods(interfaceClass);
                    listenerMethodList.addAll(subscribedMethods);
                }
            }
        }
        return listenerMethodList;
    }
}
