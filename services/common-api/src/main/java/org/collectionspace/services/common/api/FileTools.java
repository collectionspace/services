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

//  This class is designed to avoid dependencies, so it does not include logging, or apache commons.
//  There is another cspace utility class, called
//       org.collectionspace.services.common.FileUtils
//  albeit with different functions, which does have dependencies.

import java.io.*;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Laramie Crocker
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class FileTools {

	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * Reader.read(char[] buffer) method. We iterate until the
		 * Reader return -1 which means there's no more data to
		 * read. We use the StringWriter class to produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return writer.toString();
		} else {       
			return "";
		}
	}
	
    public static void forceParentDirectories(String filename) throws IOException {
        File theFile = new File(filename);
        String parent = theFile.getParent();
        if (parent != null){
            File p = new File(parent);
            p.mkdirs();
            System.out.println("Making directory: "+p.getCanonicalPath());
        }
    }

    public static boolean copyFile(String sourceFileName, String destFileName, boolean forceParentDirs) throws IOException {
        if (sourceFileName == null || destFileName == null)
            return false;
        if (sourceFileName.equals(destFileName))
            return false;
        if (forceParentDirs)
            forceParentDirectories(destFileName);
        try{
            java.io.FileInputStream in = new java.io.FileInputStream(sourceFileName);
            java.io.FileOutputStream out = new java.io.FileOutputStream(destFileName);
            try {
                byte[] buf = new byte[31000];
                int read = in.read(buf);
                while (read > -1){
                    out.write(buf, 0, read);
                    read = in.read(buf);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    public static String readFile(String dir, String relPath){
        try {
            File theFile = new File(dir, relPath);
            FileInputStream fis = new FileInputStream(theFile);
            byte[] theData = new byte[(int) theFile.length()];
            // need to check the number of bytes read here
            int howmany = fis.read(theData);
            if (howmany != theData.length){
                System.out.println("ERROR: Couldn't read all of stream!  filesize: "+theData.length+"  read: "+howmany);
            }
            fis.close();
            return new String(theData);
        } catch (Exception e) {  // can't find the file
            System.out.println("ERROR: "+e);
            return null;
        }
    }

    public static File saveFile(String dir, String relativeName, String content, boolean forceParentDirs)
    throws IOException {
        File result = null;
        PrintWriter writer;
        try{
            if (forceParentDirs) forceParentDirectories(dir+'/'+relativeName);
            result = new File(dir,relativeName);
            writer = new PrintWriter(new FileOutputStream(result));
        }catch (Exception e){
            System.out.println("Can't write to file in FileTools.saveFile: " + relativeName + " :: " + e);
            return null;
        }
        writer.write(content);
        writer.close();
        return result;
    }

    public static File createTmpDir(String filePrefix){
        String tmpDir = System.getProperty("java.io.tmpdir");
		File result = new File(tmpDir, filePrefix + UUID.randomUUID().toString());
		return result;
    }
}
