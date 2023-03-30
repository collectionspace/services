package org.collectionspace.services.report.jasperreports;

import java.util.Collections;
import java.util.List;

import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.extensions.ExtensionsRegistryFactory;
import net.sf.jasperreports.repo.RepositoryService;

public class CSpaceRepositoryExtensionsRegistryFactory implements ExtensionsRegistryFactory {

	private static final RepositoryService repositoryService = new CSpaceRepositoryService();

	private static final ExtensionsRegistry extensionsRegistry = new ExtensionsRegistry() {
		@Override
		public <T> List<T> getExtensions(Class<T> extensionType) {
			if (RepositoryService.class.equals(extensionType)) {
				return (List<T>) Collections.singletonList(repositoryService);
			}

			return null;
		}
	};

	@Override
	public ExtensionsRegistry createRegistry(String registryId, JRPropertiesMap properties) {
		return extensionsRegistry;
	}
}
