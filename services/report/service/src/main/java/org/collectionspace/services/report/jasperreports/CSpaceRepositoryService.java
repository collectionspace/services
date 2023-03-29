package org.collectionspace.services.report.jasperreports;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.MediaClient;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.media.MediaResource;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.repo.InputStreamResource;
import net.sf.jasperreports.repo.RepositoryService;
import net.sf.jasperreports.repo.Resource;

public class CSpaceRepositoryService implements RepositoryService {
	final static Logger logger = LoggerFactory.getLogger(CSpaceRepositoryService.class);

	public static final String CSPACE_PROTOCOL = "cspace://";
	public static final Pattern MEDIA_CONTENT_PATH_PATTERN = Pattern.compile("^/?media/(.*?)/blob/derivatives/(.*?)/content$");

	@Override
	public Resource getResource(String uri) {
		return getResource(uri, InputStreamResource.class);
	}

	@Override
	public <K extends Resource> K getResource(String uri, Class<K> resourceType) {
		if (
			InputStreamResource.class.equals(resourceType)
			&& uri.startsWith(CSPACE_PROTOCOL))
		{
			return ((K) getCSpaceResource(uri.substring(CSPACE_PROTOCOL.length())));
		}

		return null;
	}

	@Override
	public void saveResource(String uri, Resource resource) {
		// Not implemented
	}

	private InputStreamResource getCSpaceResource(String resourcePath) {
		Matcher matcher = MEDIA_CONTENT_PATH_PATTERN.matcher(resourcePath);

		if (matcher.matches()) {
			String mediaCsid = matcher.group(1);
			String derivative = matcher.group(2);

			return getMediaContentResource(mediaCsid, derivative);
		}

		return null;
	}

	private InputStreamResource getMediaContentResource(String mediaCsid, String derivative) {
		if (StringUtils.isNotEmpty(mediaCsid) && StringUtils.isNotEmpty(derivative)) {
			ResourceMap resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
			MediaResource mediaResource = (MediaResource) resourceMap.get(MediaClient.SERVICE_NAME);

			InputStream contentStream = null;

			try {
				contentStream = mediaResource.getDerivativeContent(mediaCsid, derivative);
			} catch (Exception e) {
				logger.warn("Error getting {} derivative for media csid {}: {}", derivative, mediaCsid, e.getMessage());
			}

			if (contentStream != null) {
				InputStreamResource inputStreamResource = new InputStreamResource();
				inputStreamResource.setInputStream(contentStream);

				return inputStreamResource;
			}
		}

		return null;
	}
}
