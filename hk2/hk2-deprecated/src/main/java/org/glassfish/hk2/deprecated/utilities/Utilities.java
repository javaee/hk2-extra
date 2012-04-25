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

package org.glassfish.hk2.deprecated.utilities;


import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;


/**
 * Core utilities.
 *
 * @author tbeerbower
 */
public class Utilities {

    /**
     * Add an alternate index to look up the given descriptor.
     *
     * @param locator     the service locator to associate this index with
     * @param descriptor  the descriptor that we are adding the index for
     * @param contract    the contract for the index
     * @param name        the name for the index
     * @param <T>         the descriptor type
     */
    public static <T> void addIndex(ServiceLocator locator,
                                    ActiveDescriptor<T> descriptor,
                                    String contract,
                                    String name) {
        DynamicConfigurationService dcs    = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration        config = dcs.createDynamicConfiguration();

        config.addActiveDescriptor(new AliasDescriptor<T>(locator, descriptor, contract, name));
        config.commit();
    }

    /**
     * Returns the type closure for the given contract.
     *
     * @param ofType   the type to check
     * @param contract the contract this type is allowed to handle
     *
     * @return the type closure restricted to the contract; null if the
     *         given type does not implement the given contract
     */
    protected static Type getTypeClosure(Type ofType, String contract){
        Set<Type> contractTypes = ReflectionHelper.getTypeClosure(
                ofType, Collections.singleton(contract));

        Iterator<Type> iterator = contractTypes.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
}