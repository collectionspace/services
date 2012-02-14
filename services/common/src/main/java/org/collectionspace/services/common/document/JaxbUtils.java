/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions.

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.common.document;

import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for Jaxb classes
 * @author
 */
public class JaxbUtils {

    private final static Logger logger = LoggerFactory.getLogger(JaxbUtils.class);

    /**
     * toString marshals given Jaxb object to a string (useful for debug)
     * @param o jaxb object
     * @param clazz class of the jaxb object
     * @return
     */
    public static String toString(Object o, Class clazz) {
        StringWriter sw = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, sw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    /**
     * fromFile retrieves object of given class from given file (in classpath)
     * @param jaxbClass
     * @param fileName of the file to read to construct the object
     * @return
     * @throws Exception
     */
    public static Object fromFile(Class jaxbClass, String fileName)
            throws Exception {

        JAXBContext context = JAXBContext.newInstance(jaxbClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        //note: setting schema to null will turn validator off
        unmarshaller.setSchema(null);
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        InputStream is = tccl.getResourceAsStream(fileName);
        return fromStream(jaxbClass, is);
    }

    /**
     * fromStream retrieves object of given class from given inputstream
     * @param jaxbClass
     * @param is stream to read to construct the object
     * @return
     * @throws Exception
     */
    public static Object fromStream(Class jaxbClass, InputStream is) throws Exception {
        JAXBContext context = JAXBContext.newInstance(jaxbClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        //note: setting schema to null will turn validator off
        unmarshaller.setSchema(null);
        return jaxbClass.cast(unmarshaller.unmarshal(is));
    }

    /**
     * getValue gets invokes specified accessor method on given object. Assumption
     * is that this is used for JavaBean pattern getXXX methods only.
     * @param o object to return value from
     * @param methodName of method to invoke
     * @return value returned of invocation
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object getValue(Object o, String methodName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (methodName == null) {
            String msg = methodName + " cannot be null";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        Class c = o.getClass();
        Method m = c.getMethod(methodName);

        Object r = m.invoke(o);
//        if (logger.isDebugEnabled()) {
//            logger.debug("getValue returned value=" + r
//                    + " for " + c.getName());
//        }
        return r;
    }

    /**
     * setValue mutates the given object by invoking specified method. Assumption
     * is that this is used for JavaBean pattern setXXX methods only.
     * @param o object to mutate
     * @param methodName indicates method to invoke
     * @param argType type of the only argument (assumed) to method
     * @param argValue value of the only argument (assumed) to method
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object setValue(Object o, String methodName, Class argType, Object argValue)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (methodName == null) {
            String msg = methodName + " cannot be null";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (argType == null) {
            String msg = "argType cannot be null";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        Class c = o.getClass();
        Method m = c.getMethod(methodName, argType);
        Object r = m.invoke(o, argValue);
        if (logger.isTraceEnabled() == true) {
            logger.trace("Completed invocation of " + methodName
                    + " for " + c.getName() + "with value=" + argValue.toString());
        }
        return r;
    }
}
