package org.collectionspace.services.common.provider;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.stream.StreamSource;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.collectionspace.services.common.jaxb.JAXBContextCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MessageBodyWriter which can use the Jakarta XML bindings. This should only be necessary while we are on RESTEasy 5;
 * once we move to 6 and have other Jakarta specs available we can use the provided writers.
 * </p>
 * Additionally, this only supports marshalling {@link XmlRootElement}. We only have a few JAXB classes to support, and
 * while some were previously annotated with {@link XmlType} I'm not really sure why. Supporting XmlTypes requires some
 * reflection, instantiating the ObjectFactory, and wrapping the provided object T in a JAXBElement. If we do end up
 * needing additional support, it would be better to have a separate provider which is created.
 *
 * @param <T> The class type for the JAXB object
 * @since 9.0.0
 */
@Provider
@Produces({ "application/xml", "application/*+xml", "text/xml", "text/*+xml" })
public class JakartaJAXBProvider<T> implements MessageBodyWriter<T>, MessageBodyReader<T> {

    private static final Logger logger = LoggerFactory.getLogger(JakartaJAXBProvider.class);

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream)
        throws WebApplicationException {
        try {
            final var context = JAXBContextCache.getInstance().getCachedJAXBContext(type);
            final var marshaller = context.createMarshaller();
            marshaller.marshal(t, outputStream);
        } catch (JAXBException e) {
            logger.error("Unable to marshal JAXB Object {}", type.getSimpleName(), e);
            final var response = Response.status(INTERNAL_SERVER_ERROR).build();
            throw new WebApplicationException(response);
        }
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
        throws WebApplicationException {
        try {
            StreamSource source;
            final var context = JAXBContextCache.getInstance().getCachedJAXBContext(type);
            final var unmarshaller = context.createUnmarshaller();
            if (mediaType != null && mediaType.getParameters().get("charset") == null) {
                source = new StreamSource(new InputStreamReader(entityStream, StandardCharsets.UTF_8));
            } else {
                source = new StreamSource(entityStream);
            }
            return (T) unmarshaller.unmarshal(source);
        } catch (JAXBException e) {
            logger.error("Unable to unmarshal JAXB Object {}", type.getSimpleName(), e);
            final var response = Response.status(INTERNAL_SERVER_ERROR).build();
            throw new WebApplicationException(response);
        }
    }
}
