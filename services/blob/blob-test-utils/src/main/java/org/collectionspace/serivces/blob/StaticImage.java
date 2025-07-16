package org.collectionspace.serivces.blob;

/**
 * Static images which are served by an embedded jetty server. These are the images found under src/main/resources and
 * it's expected that they're served on /static in jetty, e.g. /static/bird.jpg
 *
 * @since 8.3.0
 */
public enum StaticImage {
    BIRD("bird.jpg"),
    DECK("bird.jpg");

    private final String path;

    StaticImage(final String path) {
        this.path = path;
    }

    public String getUrl() {
        final String port = System.getProperty("jetty.port");
        if (port == null) {
            throw new RuntimeException("jetty.port property is not set; check your maven plugin configuration");
        }

        return "http://localhost:" + port + "/static/" + path;
    }

}
