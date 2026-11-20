package org.collectionspace.services.common;

import java.net.MalformedURLException;
import java.net.URI;

import org.testng.annotations.DataProvider;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

public class HttpDownloadUtilityTest {

    @DataProvider(name = "invalidURLs")
    public Object[][] createLocalURLs() {
        return new Object[][] {
            {"http://localhost:8080"},
            {"http://0.0.0.0:8080"},
            {"http://10.0.0.2/static/index.html"},
            {"http://169.254.169.254/index.html"},
            {"http://224.0.0.4/"},
        };
    }

    @DataProvider(name = "invalidProtocols")
    public Object[][] createInvalidProtocols() {
        return new Object[][] {
            {"file:///afile.txt"},
            {"ftp://localhost/afile.txt"},
        };
    }

    @Test(dataProvider = "invalidURLs")
    @ExpectedExceptions(IllegalArgumentException.class)
    public void testInvalidAddresses(String url) {
        HttpDownloadUtility.addressFor(URI.create(url));
    }

    @Test(dataProvider = "invalidProtocols")
    @ExpectedExceptions(MalformedURLException.class)
    public void testRejectNonHttpURL(String url) throws MalformedURLException {
        HttpDownloadUtility.downloadFile(url);
    }

}