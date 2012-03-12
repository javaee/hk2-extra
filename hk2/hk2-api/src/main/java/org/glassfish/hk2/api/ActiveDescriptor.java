/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author jwells
 *
 */
public interface ActiveDescriptor<T> extends Descriptor, SingleCache<T> {
    /**
     * The implementation class that should be used
     * to generate new instances of this descriptor.
     * <p>
     * If the class returned is a Factory, then the
     * factory is used to create instances.  In this case
     * the system will get an instance of the factory and
     * use it to create the instances
     *  
     * @return The class that directly implements the contract
     * types, or the class that is the factory for an object
     * that implements the contract types
     */
    public Class<?> getImplementationClass();
    
    /**
     * The set of types that this ActiveDescriptor must produce.
     * These types may be Classes or ParameterizedTypes, and
     * may be no other subclass of Type
     * 
     * @return the set of types this ActiveDescriptor must implement
     * or extend
     */
    public Set<Type> getContractTypes();
    
    /**
     * Returns the scope that this ActiveDescriptor belongs to
     * 
     * @return The scope of this ActiveDescriptor
     */
    public Class<? extends Annotation> getScopeAnnotation();
    
    /**
     * The full set of qualifiers that this ActiveDescriptor
     * provides
     * 
     * @return The set of annotations that this ActiveDescriptor
     * provides
     */
    public Set<Annotation> getQualifierAnnotations();
    
    /**
     * Returns the full set of Injectees this class has.  These
     * references will be resolved prior to the class being constructed,
     * even if these injectees are field or method injectees.
     * <p>
     * If this descriptor is describing a factory created type then
     * this set must have zero length
     * 
     * @return Will not return null, but may return an empty set.  The set
     * of Injectees that must be resolved before this ActiveDescriptor can
     * be constructed
     */
    public Set<Injectee> getInjectees();
    
    /**
     * Creates an instance of the ActiveDescriptor.  All of the
     * Injectee's must be created prior to instantiation, and
     * associated with the ExtendedProvider so that they can be
     * destroyed properly
     * 
     * @param root The root service handle, which can be used
     * to associated all the PerLookup objects with this creation
     * 
     * @return An instance of this ActiveDescriptor
     */
    public T create(ServiceHandle<T> root);
    
    /**
     * Disposes this instance.  All the PerLookup objects that
     * were created for this instance will be destroyed after this
     * object has been destroyed
     * 
     * @param instance The instance to destroy
     * @param root the extended provider created when this was found
     */
    public void dispose(T instance, ServiceHandle<T> root);
    
    
}