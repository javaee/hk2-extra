/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Extends the {@link Provider} contract, offering the ability to
 * 
 * 	<li> obtain the runtime {@link Descriptor} describing the
 * 		attributes of the registered component/service, and
 * 
 * 	<li> provides a means to access the clazz type instance.  Note
 * 		that the class instance might be managed in a context
 * 		other than the current thread classloader context.
 * 
 * @author Jerome Dochez
 * @author Jeff Trent
 * @author Mason Taube
 *  
 * @see ManagedComponentProvider
 */
public interface ComponentProvider<T> extends Provider<T> {

    /**
     * The {@link Descriptor} fully characterizes the attributes
     * of this {@link Provider}.
     * 
     * @return 
     * 	a non-null Descriptor describing the complete set of
     * 	attributes of the provider.
     */
    Descriptor getDescriptor();
  
    /**
     * The class type of the implementation. it is responsible also
     * for determining how (i.e., which loader) to use.
     * 
     * <p/>
     * The class type for what the {@link Provider} actually
     * produces.
     * 
     * <p/>
     * Note that there is some cost to this call during the first
     * invocation since it needs to perform classloading.  Care
     * should therefore be exercised accordingly.
     * 
     * @return
     * 	the class type for what this Provider produces, or null
     * 	only in the case where the Provider is a facade to a user
     * 	defined factory that
     * 
     */
    Class<? extends T> type();

    /**
     * The collection of annotations for this type.  Note that this
     * may not be the same as the annotations on the {@link #type()}.
     * 
     * @return
     * 	a non-null collection of annotation classes for
     *  this provider type.
     */
    Collection<Annotation> getAnnotations();

}