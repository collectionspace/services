package org.collectionspace.services.client.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlobServer {

    private static final Logger logger = LoggerFactory.getLogger(BlobServer.class);

    private Server server;
    private int port;

    public void start() throws Exception {
        port = Integer.parseInt(System.getProperty("blob.test.port"));
        logger.info("Starting server on port {}", port);
        server = new Server(port);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(ImageServlet.class, "/images");
        server.start();
    }

    public String getBirdUrl() {
        return "http://localhost:" + port + "/images?image=bird";
    }

    public String getDeckUrl() {
        return "http://localhost:" + port + "/images?image=deck";
    }

    public void stop() throws Exception {
        server.stop();
    }

    public enum Image {
        BIRD("blobs/birb.jpg"),
        DECK("blobs/deck.jpg");

        private final String path;

        Image(String path) {
            this.path = path;
        }

        public File getFile() {
            ClassLoader cl = getClass().getClassLoader();
            return new File(cl.getResource(path).getFile());
        }

    }

    public static class ImageServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            final String image = req.getParameter("image");
            BlobServer.logger.info("Requesting image {}", image);

            final Image theImage = Image.valueOf(image.toUpperCase());
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(ContentType.IMAGE_JPEG.toString());
            resp.setContentLengthLong(theImage.getFile().length());
            resp.getOutputStream().write(Files.readAllBytes(theImage.getFile().toPath()));
        }
    }

}
