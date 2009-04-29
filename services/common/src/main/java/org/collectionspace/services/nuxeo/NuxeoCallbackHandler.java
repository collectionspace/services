/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.nuxeo;

/**
 *
 * @author sanjaydalal
 */
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Copied from jbossx.
 *
 * @author Scott.Stark@jboss.org
 */
public class NuxeoCallbackHandler implements CallbackHandler {

    private final String username;
    private char[] password;
    private final Object credential;

    /**
     * Initializes the UsernamePasswordHandler with the username and password to
     * use.
     *
     * @param username the user name
     * @param password the password for this user
     */
    public NuxeoCallbackHandler(String username, char[] password) {
        this.username = username;
        this.password = password;
        credential = password;
    }

    public NuxeoCallbackHandler(String username, Object credential) {
        this.username = username;
        this.credential = credential;
        if (credential instanceof char[]) {
            password = (char[]) credential;
        } else if (credential instanceof CharSequence) {
            password = credential.toString().toCharArray();
        }
    }

    /**
     * Sets any NameCallback name property to the instance username, sets any
     * PasswordCallback password property to the instance, and any password.
     *
     * @exception UnsupportedCallbackException,
     *                thrown if any callback of type other than NameCallback or
     *                PasswordCallback are seen.
     */
    public void handle(Callback[] callbacks)
            throws UnsupportedCallbackException {
        for (Callback c : callbacks) {
            if (c instanceof NameCallback) {
                NameCallback nc = (NameCallback) c;
                nc.setName(username);
            } else if (c instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) c;
                if (password == null) {
                    // We were given an opaque Object credential but a char[] is
                    // requested?
                    if (credential != null) {
                        String tmp = credential.toString();
                        password = tmp.toCharArray();
                    }
                }
                pc.setPassword(password);
            } else if (c instanceof NuxeoCallback) {
                NuxeoCallback oc = (NuxeoCallback) c;
                oc.setCredential(credential);
            } else {
                throw new UnsupportedCallbackException(c,
                        "Unrecognized Callback");
            }
        }
    }
}
