package org.collectionspace.services.servicegroup.nuxeo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.Tags;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for ServiceGroupDocumentModelHandler
 */
public class ServiceGroupDocumentModelHandlerTest {

    private static final String TAG = "tag";
    private static final String ALT_TAG = "altTag";
    private static final String NEGATED_TAG = "-tag";

    @Test
    public void acceptServiceBinding() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();
        Tags tags = new Tags();
        tags.getTag().add(TAG);
        binding.setTags(tags);

        List<String> queryTags = Collections.singletonList(TAG);
        boolean accept = handler.acceptServiceBinding(binding, queryTags);
        Assert.assertTrue(accept);
    }

    @Test
    public void rejectServiceBinding() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();
        Tags tags = new Tags();
        tags.getTag().add(TAG);
        binding.setTags(tags);

        List<String> queryTags = Collections.singletonList(ALT_TAG);
        boolean accept = handler.acceptServiceBinding(binding, queryTags);
        Assert.assertFalse(accept);
    }

    @Test
    public void rejectServiceBindingNullTags() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();

        List<String> queryTags = Collections.singletonList(TAG);
        boolean accept = handler.acceptServiceBinding(binding, queryTags);
        Assert.assertFalse(accept);
    }

    @Test
    public void rejectServiceBindingEmptyTags() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();
        binding.setTags(new Tags());

        List<String> queryTags = Collections.singletonList(TAG);
        boolean accept = handler.acceptServiceBinding(binding, queryTags);
        Assert.assertFalse(accept);
    }

    @Test
    public void acceptServiceBindingMultiple() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();
        Tags tags = new Tags();
        tags.getTag().add(TAG);
        tags.getTag().add(ALT_TAG);
        binding.setTags(tags);

        List<String> queryTags = Arrays.asList(TAG, ALT_TAG);
        boolean accept = handler.acceptServiceBinding(binding, queryTags);
        Assert.assertTrue(accept);
    }

    @Test
    public void rejectServiceBindingNegation() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();
        Tags tags = new Tags();
        tags.getTag().add(TAG);
        tags.getTag().add(ALT_TAG);
        binding.setTags(tags);

        List<String> queryTags = Arrays.asList(NEGATED_TAG, ALT_TAG);
        boolean accept = handler.acceptServiceBinding(binding, queryTags);
        Assert.assertFalse(accept);
    }
}