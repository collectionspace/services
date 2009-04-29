/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.collectionspace.services.nuxeo;

/**
 *
 * @author sanjaydalal
 */

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class NuxeoLoginContextFactory {


    private static boolean initDone=false;

    private static void initLoginConfig()
    {
        if (initDone)
            return;

        Configuration parentConfig = null;
        try {
            parentConfig = Configuration.getConfiguration();
        } catch (Exception e) {
            // do nothing - this can happen if default configuration provider is not correctly configured
            // for examnple FileConfig fails if no config file was defined
        }
        Configuration config = new NuxeoLoginConfiguration(parentConfig);
        Configuration.setConfiguration(config);

        initDone=true;

    }


    public static LoginContext getLoginContext(CallbackHandler handler) throws LoginException
    {
        initLoginConfig();
        return  new LoginContext(NuxeoLoginConfiguration.LOGIN_DOMAIN, handler);
    }
}
