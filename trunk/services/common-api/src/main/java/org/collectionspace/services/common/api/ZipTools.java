/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common.api;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class ZipTools {

    public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    public static void forceParentDirectories(String filename) throws Exception {
        File theFile = new File(filename);
        String parent = theFile.getParent();
        if (parent != null) {
            File p = new File(parent);
            p.mkdirs();
            System.out.println("Making directory: " + p.getCanonicalPath());
        }
    }

    /**
     * It is HIGHLY recommended to use a baseOutputDir, such as "./", or
     * a local directory you know, such as "/tmp/foo", to prevent
     * files from being unzipped in your root directory.
     */
    public static final void unzip(String zipfileName, String baseOutputDir) {
        Enumeration entries;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(zipfileName);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String entryName = entry.getName();
                String theName = baseOutputDir + '/' + entryName;
                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    System.out.println("Extracting directory: " + entry.getName());
                    // This is not robust, just for demonstration purposes.
                    (new File(theName)).mkdirs();
                    continue;
                }
                //(new File(theName)).mkdirs();
                forceParentDirectories(theName);
                System.out.println("Extracting file: " + theName);
                copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(theName)));
            }
            zipFile.close();
        } catch (Exception ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        }
    }

    public static void zipDiveDirectory(int stripLeadingPathChars, String directory, String zipFilename) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilename));
        zipDir(stripLeadingPathChars, directory, zos);
        zos.close();
    }

    public static void zipDir(int stripLeadingPathChars, String dir2zip, ZipOutputStream zos) throws Exception {
        File fzipDir = new File(dir2zip);
        if (!fzipDir.exists()) {
            System.out.println("dir doesn't exist: " + dir2zip);
            return;
        }
        String[] dirList = fzipDir.list(); //get a listing of the directory content
        byte[] readBuffer = new byte[2156];
        int bytesIn = 0;
        //loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; i++) {
            File f = new File(fzipDir, dirList[i]);
            if (f.isDirectory()) {
                //if the File object is a directory, call this function again to add its content recursively
                zipDir(stripLeadingPathChars, f.getPath(), zos); //DIVE!
                continue;
            }
            //if we reached here, the File object f was not a directory
            String fpath = f.getPath();
            String nameInArchive = fpath.substring(stripLeadingPathChars, fpath.length());
            addToZip(zos, fpath, nameInArchive);
        }
    }

    public static void addToZip(ZipOutputStream zos, String filename, String nameInArchive) throws Exception {
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("File does not exist, skipping: " + filename);
            return;
        }
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int bytesRead;
        byte[] buffer = new byte[1024];
        CRC32 crc = new CRC32();
        crc.reset();
        while ((bytesRead = bis.read(buffer)) != -1) {
            crc.update(buffer, 0, bytesRead);
        }
        bis.close();
        // Reset to beginning of input stream
        bis = new BufferedInputStream(new FileInputStream(file));
        String nameInArchiveFixed = nameInArchive.replace("\\", "/");
        ZipEntry entry = new ZipEntry(nameInArchiveFixed);
        entry.setMethod(ZipEntry.STORED);
        entry.setCompressedSize(file.length());
        entry.setSize(file.length());
        entry.setCrc(crc.getValue());
        zos.putNextEntry(entry);
        while ((bytesRead = bis.read(buffer)) != -1) {
            zos.write(buffer, 0, bytesRead);
        }
        bis.close();
    }

}

