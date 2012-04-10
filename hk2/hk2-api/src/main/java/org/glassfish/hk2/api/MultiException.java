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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This exception can contain multiple other exceptions.
 * However, it will also have the causal chain of the
 * first exception added to the list of exceptions
 * 
 * @author jwells
 *
 */
public class MultiException extends RuntimeException {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 2112432697858621044L;
    
    private final List<Throwable> throwables = new LinkedList<Throwable>();
    
    /**
     * Creates an empty MultiException
     */
    public MultiException() {
        super();
    }
    
    /**
     * This list must have at least one element in it.
     * The first element of the list will become the
     * cause of this exception, and its message will become
     * the message of this exception
     * 
     * @param th A non-null, non-empty list of exceptions
     */
    public MultiException(List<Throwable> th) {
        super(th.get(0).getMessage(), th.get(0));
        
        throwables.addAll(th);
    }
    
    /**
     * This allows for construction of a MultiException
     * with one element in its list
     * 
     * @param th May not be null
     */
    public MultiException(Throwable th) {
        super(th.getMessage(), th);
        
        throwables.add(th);
    }
    
    /**
     * Gets all the errors associated with this MultiException
     * 
     * @return All the errors associated with this MultiException. Will
     * not return null, but may return an empty object
     */
    public List<Throwable> getErrors() {
        return Collections.unmodifiableList(throwables);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("MultiException(");
        
        int lcv = 1;
        for (Throwable th : throwables) {
            sb.append("\n" + lcv++ + ". " + th.getMessage());
        }
        
        if (throwables.isEmpty()) {
            sb.append(System.identityHashCode(this) + ")");
        }
        else {
            sb.append("\n" + System.identityHashCode(this) + ")");
        }
        
        return sb.toString();
    }

}