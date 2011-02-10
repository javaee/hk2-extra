/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.hk2.component;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * Component for which constructors takes @Inject annotated parameters
 *
 * @author Jerome Dochez
 */
public class InjectableParametizedConstructorCreator<T> extends ConstructorCreator<T> {

    public InjectableParametizedConstructorCreator(Class<T> type, Habitat habitat, MultiMap<String, String> metadata) {
        super(type, habitat, metadata);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create(Inhabitant onBehalfOf) throws ComponentException {
        // find appropriate constructor.
        Constructor[] ctors = type.getDeclaredConstructors();
        if (ctors.length!=1) {
            // more than one constructor, we expect a null parameter ctor and fall back
            // into parent.
            return super.create(onBehalfOf);
        } else {
            Constructor<T> ctor=ctors[0];
            Annotation paramsAnnotations[][] = ctor.getParameterAnnotations();
            Class paramTypes[] = ctor.getParameterTypes();
            Type genericParamTypes[] = ctor.getGenericParameterTypes();
            Object paramValues[] = new Object[paramTypes.length];
            for (int i=0;i<paramTypes.length;i++) {
                Class paramType = paramTypes[i];
                final Annotation paramAnnotations[] = paramsAnnotations[i];
                for (Annotation a : paramAnnotations) {
                    if (a.annotationType().equals(Inject.class)) {
                        for (InjectionResolverQuery resolver : getInjectionResolvers(habitat)) {

                            AnnotatedElement annotatedElement = new AnnotatedElement() {
                                @Override
                                public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
                                    return getAnnotation(annotationClass)!=null;
                                }

                                @Override
                                public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
                                    for (Annotation a : paramAnnotations) {
                                        if (a.annotationType().equals(annotationClass)) {
                                            return (T) a;
                                        }
                                    }
                                    return null;
                                }

                                @Override
                                public Annotation[] getAnnotations() {
                                    return paramAnnotations;
                                }

                                @Override
                                public Annotation[] getDeclaredAnnotations() {
                                    return paramAnnotations;
                                }
                            };
                            Object value = resolver.getValue(this, onBehalfOf, annotatedElement, genericParamTypes[i], paramType);
                            if (value!=null) {
                                paramValues[i] = value;
                                break;
                            }
                        }
                        if ((paramValues[i] == null) && !((Inject) a).optional()) {
                            throw new UnsatisfiedDependencyException(ctor, a);
                        }
                    }
                }
            }
        try {
            return ctor.newInstance(paramValues);
        } catch(InvocationTargetException e) {
            throw new ComponentException("Failed to create "+type,e);
        } catch (InstantiationException e) {
            throw new ComponentException("Failed to create "+type,e);
        } catch (IllegalAccessException e) {
            try {
              ctor.setAccessible(true);
              return ctor.newInstance(paramValues);
            } catch (Exception e1) {
              // ignore
            }
            throw new ComponentException("Failed to create "+type,e);
        } catch (LinkageError e) {
            throw new ComponentException("Failed to create "+type,e);
        } catch (RuntimeException e) {
            throw new ComponentException("Failed to create "+type,e);
        }
        }
    }
}