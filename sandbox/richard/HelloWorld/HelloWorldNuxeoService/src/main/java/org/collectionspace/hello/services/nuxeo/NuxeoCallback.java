/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.collectionspace.hello.services.nuxeo;


import javax.security.auth.callback.Callback;

/**
 * Copied from jbossx
 *
 * An implementation of Callback that simply obtains an Object to be used
 * as the authentication credential. Interpretation of the Object is up to
 * the LoginModules that validate the credential.
 *
 * @author  Scott.Stark@jboss.org
 */
public class NuxeoCallback implements Callback {

    private final String prompt;

    private Object credential;

    public NuxeoCallback() {
        this("");
    }

    public NuxeoCallback(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public Object getCredential() {
        return credential;
    }

    public void setCredential(Object credential) {
        this.credential = credential;
    }

    public void clearCredential() {
        credential = null;
    }

}
