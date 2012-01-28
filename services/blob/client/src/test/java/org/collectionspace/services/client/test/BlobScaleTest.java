package org.collectionspace.services.client.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.common.profile.Profiler;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class BlobScaleTest extends BaseServiceTest<AbstractCommonList> {

    private final Logger logger = LoggerFactory.getLogger(BlobScaleTest.class);
	
	private static final int IMAGE_SIZE = 1000;
	private static final int IMAGE_EDGE = -15;
	private static final int MIN_FONTSIZE = 15;
	private static final int MAX_FONTSIZE = 60;
	private static final String IMAGES_TO_CREATE_PROP = "imagesToCreate";
	private static final int DEFAULT_IMAGES_TO_CREATE = 1;
    private static final String GENERATED_IMAGES = "target/generated_images";

	private static Random generator = new Random(System.currentTimeMillis());
	
	@Override
	protected CollectionSpaceClient getClientInstance() {
        return new BlobClient();
	}

	@Override
	protected String getServicePathComponent() {
		return BlobClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return BlobClient.SERVICE_NAME;
	}

	private int getImagesToCreate() {
		int result = DEFAULT_IMAGES_TO_CREATE;
		
        String imagesToCreate = System.getProperty(IMAGES_TO_CREATE_PROP);
        try {
        	result = Integer.parseInt(imagesToCreate);
        } catch (NumberFormatException np) {
        	logger.info("No value (or a bad value) for system property '"
        			+ IMAGES_TO_CREATE_PROP
        			+ "' was defined, so we'll use the default instead.");
        } finally {
        	logger.info("Testing blob scaling by creating "
        			+ result
        			+ " images.");
        }
        
        return result;
	}
	
	@Test(dataProvider = "testName")
	public void scaleTest(String testName) throws MalformedURLException {
		this.createDirectory(GENERATED_IMAGES);
		setupCreate();
		int imagesToCreate = getImagesToCreate();
        BlobClient client = new BlobClient();
		Profiler profiler = new Profiler(this, 1);
        
        for (int i = 0; i < imagesToCreate; i++, profiler.reset()) {
			File jpegFile = createJpeg(GENERATED_IMAGES);	
			URL url = jpegFile.toURI().toURL();
			
	    	profiler.start();
			ClientResponse<Response> res = client.createBlobFromURI(url.toString());
			try {
				profiler.stop();
		        assertStatusCode(res, testName);
				logger.debug(
						i + ": Uploaded image to Nuxeo in "
						+ profiler.getCumulativeTime()
						+ " milleseconds "
						+ " - "
						+ " : "
						+ jpegFile.getAbsolutePath());
				
		        String csid = extractId(res);
		        allResourceIdsCreated.add(csid);
			} finally {
				if (res != null) {
                    res.releaseConnection();
                }
			}
        }
	}
	
	private void createDirectory(String dirName) {
		boolean success = (
				new File(dirName)).mkdir();
		if (success) {
			logger.debug("Directory: " 
					+ dirName + " created");
		} 
	}
	
	public File createJpeg(String destDir) {
		File result = null;

		BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);   
		Graphics g = image.getGraphics();
		for (int i = 0; i < IMAGE_SIZE; i = i + 10) {
			int x = random(IMAGE_EDGE, IMAGE_SIZE);
			int y = random(IMAGE_EDGE, IMAGE_SIZE);
			g.drawString(Integer.toString(random(-123456789, 123456789)), x, y);
			Color c = new Color(random(0, 255), random(0, 255), random(0, 255));
			g.setColor(c);

			Font currentFont = g.getFont();
			Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(),
					random(MIN_FONTSIZE, MAX_FONTSIZE));
			g.setFont(newFont);
		}
		try {    
			ImageIO.write(image, "jpg", result = new File(destDir
					+ File.separator
					+ System.currentTimeMillis()
					+ ".jpg"));
		} catch (IOException e) {    
			e.printStackTrace();   
		} 

		return result;
	}

	public int random(int min, int max) {
		return min + (int)(generator.nextFloat() * ((max - min) + 1));
	}
	

}
