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
package org.jvnet.hk2.generator.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.glassfish.hk2.utilities.DescriptorImpl;
import org.objectweb.asm.ClassReader;

/**
 * @author jwells
 *
 */
public class GeneratorRunner {
    private final static String DOT_CLASS = ".class";
    private final static String META_INF = "META-INF";
    private final static String INHABITANTS = "inhabitants";
    
    private final String fileOrDirectory;
    private final String locatorName;
    private final boolean verbose;

    /**
     * This initializes the GeneratorRunner with the values needed to run
     * 
     * @param fileOrDirectory The fileOrDirectory to inspect for services
     * @param locatorName The name of the locator these files should be put into
     * @param verbose true if this should print information about progress
     */
    public GeneratorRunner(String fileOrDirectory, String locatorName, boolean verbose) {
        this.fileOrDirectory = fileOrDirectory;
        this.locatorName = locatorName;
        this.verbose = verbose;
    }
    
    /**
     * Does the work of writing out the inhabitants file to the proper location
     * 
     * @throws AssertionError On an error such as not being able to find the
     * proper file
     * @throws IOException On IO error
     */
    public void go() throws AssertionError, IOException {
        File toInspect = new File(fileOrDirectory);
        
        if (!toInspect.exists()) {
            throw new AssertionError("Could not find file: " + toInspect.getAbsolutePath());
        }
        
        List<DescriptorImpl> allDescriptors;
        if (toInspect.isDirectory()) {
            allDescriptors = findAllServicesFromDirectory(toInspect);
            if (allDescriptors.isEmpty()) return;
            writeToDirectory(toInspect, allDescriptors);
        }
        else {
            allDescriptors = findAllServicesFromJar(toInspect);
            writeToJar(toInspect, allDescriptors);
        }
        
    }
    
    private List<DescriptorImpl> findAllServicesFromDirectory(File directory) throws IOException {
        List<DescriptorImpl> retVal = new LinkedList<DescriptorImpl>();
        
        File subDirectories[] = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
            
        });
        
        for (File subDirectory : subDirectories) {
            retVal.addAll(findAllServicesFromDirectory(subDirectory));
        }
        
        // Now get all the class files from this directory itself
        File candidates[] = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(DOT_CLASS);
            }
        });
        
        for (File candidate : candidates) {
            FileInputStream fis = new FileInputStream(candidate);
                
            DescriptorImpl di = createDescriptorIfService(fis);
            if (di != null) {
                retVal.add(di);
            }
        }
        
        return retVal;
    }
    
    
    
    private void writeToDirectory(File parent, List<DescriptorImpl> descriptors) throws IOException {
        File META_INF_dir = new File(parent, META_INF);
        File inhabitantsDir = new File(META_INF_dir, INHABITANTS);
        
        if (!inhabitantsDir.exists()) {
            inhabitantsDir.mkdirs();
        }
        
        File writeMeFile = writeInhabitantsFile(descriptors);
        
        // OK, now swap it
        File outputFile = new File(inhabitantsDir, locatorName);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        
        writeMeFile.renameTo(outputFile);
    }
    
    private void writeToJar(File jarFile, List<DescriptorImpl> descriptors) throws IOException {
        File writeMeFile = writeInhabitantsFile(descriptors);
        writeMeFile.deleteOnExit();
        
        byte buffer[] = new byte[1024];
        
        File tmpJarFile = File.createTempFile(jarFile.getName(), ".tmp");
        
        FileInputStream fis = new FileInputStream(jarFile);
        ZipInputStream zis = new ZipInputStream(fis);
        
        FileOutputStream fos = new FileOutputStream(tmpJarFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        
        ZipEntry zentry = zis.getNextEntry();
        while (zentry != null) {
            String entryName = zentry.getName();
            
            if (entryName.equals(META_INF + "/" + INHABITANTS + "/" + locatorName)) {
                // Don't write out the old one
                continue;
            }
            
            zos.putNextEntry(new ZipEntry(entryName));
            
            int len;
            while ((len = zis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
        
        zis.close();
        
        if (!descriptors.isEmpty()) {
            zos.putNextEntry(new ZipEntry(META_INF + "/" + INHABITANTS + "/" + locatorName));
        
            FileInputStream desc_os = new FileInputStream(writeMeFile);
            int len;
            while ((len = desc_os.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            desc_os.close();
        }
        
        zos.close();
        
        // All went well, replace the JAR file with the new and improved jar file
        tmpJarFile.renameTo(jarFile);
    }
    
    private File writeInhabitantsFile(List<DescriptorImpl> descriptors) throws IOException {
        File outFile = File.createTempFile(locatorName, ".tmp");
        
        FileOutputStream fos = new FileOutputStream(outFile);
        
        PrintWriter pw = new PrintWriter(fos);
        
        for (DescriptorImpl di : descriptors) {
            di.writeObject(pw);
        }
        
        pw.close();
        fos.close();
        
        return outFile;
    }
    
    private List<DescriptorImpl> findAllServicesFromJar(File jar) throws IOException {
        List<DescriptorImpl> retVal = new LinkedList<DescriptorImpl>();
        
        JarFile jarFile = new JarFile(jar);
        
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            
            String entryName = entry.getName();
            if (!entryName.endsWith(DOT_CLASS)) continue;
            
            DescriptorImpl di = createDescriptorIfService(jarFile.getInputStream(entry));
            if (di != null) {
                retVal.add(di);
            }
            
            
        }
        
        return retVal;
    }
    
    private DescriptorImpl createDescriptorIfService(InputStream is) throws IOException {
        ClassReader reader = new ClassReader(is);
        
        ClassVisitorImpl cvi = new ClassVisitorImpl(verbose);
        
        reader.accept(cvi, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        
        return cvi.getGeneratedDescriptor();
    }
}