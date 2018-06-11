package org.collectionspace.authentication;

import static org.apache.catalina.Lifecycle.AFTER_INIT_EVENT;
import static org.apache.catalina.Lifecycle.AFTER_START_EVENT;
import static org.apache.catalina.LifecycleState.INITIALIZED;
import static org.apache.catalina.LifecycleState.NEW;
import static org.apache.catalina.LifecycleState.STARTED;

import java.util.logging.Level;

import org.apache.catalina.Container;
import org.apache.catalina.Executor;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;

/**
 * Checks to see if any of the webapps in the Tomcat container failed during startup.  If so,
 * we ask Tomcat to shutdown.
 * 
 * @author remillet
 *
 */
public class TomcatShutdownListener implements LifecycleListener {
	
	java.util.logging.Logger logger = java.util.logging.Logger.getAnonymousLogger();

	@Override
	public void lifecycleEvent(final LifecycleEvent event) {
		boolean isInit = AFTER_INIT_EVENT.equals(event.getType());
		if (isInit || AFTER_START_EVENT.equals(event.getType())) {
			Server server = (Server) event.getLifecycle();
			boolean failed = checkServer(isInit, server);
			if (failed) {
				triggerShutdown(isInit, server);
			} else if (!isInit) {
				logger.log(Level.INFO, "CollectionSpace Startup successful." + "\007" + "\007" + "\007");
			}
		}
	}

	private boolean checkServer(final boolean isInit, final Server server) {
		boolean failed = checkState(isInit, server);
		for (Service service : server.findServices()) {
			failed |= checkService(isInit, service);
		}
		return failed;
	}

	private boolean checkService(final boolean isInit, final Service service) {
		boolean failed = checkState(isInit, service);
		failed |= checkContainer(isInit, service.getContainer());
		for (Connector connector : service.findConnectors()) {
			failed |= checkState(isInit, connector);
		}
		for (Executor executor : service.findExecutors()) {
			failed |= checkState(isInit, executor);
		}
		return failed;
	}

	private boolean checkContainer(final boolean isInit, final Container container) {
		boolean failed = checkState(isInit, container);
		for (Container child : container.findChildren()) {
			failed |= checkContainer(isInit, child);
		}
		return failed;
	}

	private boolean checkState(final boolean isInit, final Lifecycle lifecycle) {
		LifecycleState state = lifecycle.getState();
		if (isInit ? state != NEW && state != INITIALIZED : state != STARTED) {
			logger.log(Level.SEVERE, "CollectionSpace Startup Failure\n###\n### - CollectionSpace Startup failure, initiating Tomcat shutdown...\n###");
			logger.log(Level.SEVERE, String.format("Required CollectionSpace Component {%s} failed to {%s}: state {%s}.", lifecycle.toString(),
					isInit ? "initialize" : "start", state.toString()));
			logger.log(Level.SEVERE, "See other Tomcat log files for possible failure reasons and details.\n");
			return true;
		}
		return false;
	}

	private void triggerShutdown(final boolean isInit, final Server server) {
		if (isInit) {
			throw new RuntimeException("One or more required Tomcat components failed to initialize.");
		}
		try {
			server.stop();
			server.destroy();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
	}
}
