/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.jvnet.hk2;

import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public final class RequiredBundle extends Named implements Comparable<RequiredBundle> {
    /**
     * Version constraint.
     */
    public final String version;

    /**
     * Resolution.
     *
     * TODO: what are possible values?
     */
    public final String resolution;

    public final String visibility;

    RequiredBundle(Lexer sc) {
        super(sc);

        String version=null,resolution=null,visibility=null;

        while(true) {
            if(sc.at(BUNDLE_VERSION)) {
                sc.read(BUNDLE_VERSION);
                version = sc.readUntil('\"');
                sc.consume("\"");
                continue;
            }
            if(sc.at(RESOLUTION)) {
                sc.read(RESOLUTION);
                resolution = sc.read(TOKEN);
                continue;
            }
            if(sc.at(VISIBILITY)) {
                sc.read(VISIBILITY);
                visibility = sc.read(TOKEN);
                continue;
            }
            break;
        }

        this.version = version;
        this.resolution = resolution;
        this.visibility = visibility;

        if(sc.at(','))
            sc.consume(",");
    }

    public int compareTo(RequiredBundle that) {
        return this.name.compareTo(that.name);
    }

    static final Pattern PACKAGE = Pattern.compile("[^,;]+");
    static final Pattern BUNDLE_VERSION = Pattern.compile(";bundle-version=\"");
    static final Pattern RESOLUTION = Pattern.compile(";resolution:=");
    static final Pattern VISIBILITY = Pattern.compile(";visibility:=");
    static final Pattern TOKEN = Pattern.compile("[A-Za-z]+");
}
