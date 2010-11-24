/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.module.bootstrap;

/**
 * Created by IntelliJ IDEA.
 * User: naman
 * Date: 3 Nov, 2010
 * Time: 12:10:21 PM
 */

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Level;

public class EarlyLogHandler extends Handler {

    private static int MAX_MESSAGES = 200;

    public static ArrayBlockingQueue<LogRecord> earlyMessages = new ArrayBlockingQueue<LogRecord>(MAX_MESSAGES);

	/*
    * collect the message that are logged before the log service is started
    * The log manager service will print them out when it is started.
	*/
    public void publish(LogRecord record) {

        //log manager service not started yet so we are queuing up the messages
        try {
            earlyMessages.add(record);
        } catch (IllegalStateException ie) {
            // can't add more messages; something terrible is happening.
            // Dump the queue to a file, need to stop queuing messages
        }
    }

	/*
	* Provide method for users to log their messages.
	*/
	public void logMessage(Level level,String message) {
        LogRecord lr = new LogRecord(level,message);
        publish(lr);
	}  

    /**
    * Called to close this log handler.
    */
    public void close() {
	    // not used
    }

    /**
    * Called to flush any cached data that
    * this log handler may contain.
    */
    public void flush() {
        // not used
    }
} 