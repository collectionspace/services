/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2019 LYRASIS

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
package org.collectionspace.services.nuxeo.client.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.management.JMException;

import org.nuxeo.osgi.application.FrameworkBootstrap;
import org.nuxeo.osgi.application.MutableClassLoader;

public class NuxeoFrameworkBootstrap extends FrameworkBootstrap {
    public NuxeoFrameworkBootstrap(ClassLoader cl, File home) throws IOException {
        super(cl, home);
    }

    public NuxeoFrameworkBootstrap(MutableClassLoader loader, File home) throws IOException {
        super(loader, home);
    }

    @Override
    public void start(MutableClassLoader cl) throws ReflectiveOperationException, IOException, JMException {
        if (frameworkLoaderClass == null) {
            throw new IllegalStateException("Framework Loader was not initialized. Call initialize() method first");
        }
        Method start = frameworkLoaderClass.getMethod("start");
        start.invoke(null);
        printStartedMessage();
    }

    @Override
    public void stop(MutableClassLoader cl) throws ReflectiveOperationException, JMException {
        if (frameworkLoaderClass == null) {
            throw new IllegalStateException("Framework Loader was not initialized. Call initialize() method first");
        }
        Method stop = frameworkLoaderClass.getMethod("stop");
        stop.invoke(null);
    }
}
