package org.collectionspace.services.servicegroup.nuxeo;

import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.Tags;
import org.testng.Assert;
import org.testng.annotations.Test;

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

        boolean accept = handler.acceptServiceBinding(binding, TAG);
        Assert.assertTrue(accept);
    }

    @Test
    public void acceptServiceBindingNoTagWhenNegation() {
        ServiceBindingType binding = new ServiceBindingType();
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        boolean accept = handler.acceptServiceBinding(binding, NEGATED_TAG);
        Assert.assertTrue(accept);
    }

    @Test
    public void rejectServiceBinding() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();
        Tags tags = new Tags();
        tags.getTag().add(TAG);
        binding.setTags(tags);

        boolean accept = handler.acceptServiceBinding(binding, ALT_TAG);
        Assert.assertFalse(accept);
    }

    @Test
    public void rejectServiceBindingNullTags() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();

        boolean accept = handler.acceptServiceBinding(binding, TAG);
        Assert.assertFalse(accept);
    }

    @Test
    public void rejectServiceBindingEmptyTags() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();
        binding.setTags(new Tags());

        boolean accept = handler.acceptServiceBinding(binding, TAG);
        Assert.assertFalse(accept);
    }

    @Test
    public void rejectServiceBindingNotMatch() {
        ServiceGroupDocumentModelHandler handler = new ServiceGroupDocumentModelHandler();

        ServiceBindingType binding = new ServiceBindingType();
        Tags tags = new Tags();
        tags.getTag().add(TAG);
        binding.setTags(tags);

        boolean accept = handler.acceptServiceBinding(binding, NEGATED_TAG);
        Assert.assertFalse(accept);
    }

}