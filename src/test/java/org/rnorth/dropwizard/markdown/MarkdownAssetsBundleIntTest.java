package org.rnorth.dropwizard.markdown;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URISyntaxException;

import static org.rnorth.visibleassertions.VisibleAssertions.assertFalse;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;
import static uk.co.deloittedigital.dropwizard.testsupport.Targets.localTarget;

/**
 * Created by rnorth on 29/09/2016.
 */
public class MarkdownAssetsBundleIntTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> APP = new DropwizardAppRule<>(
            TestApp.class,
            ResourceHelpers.resourceFilePath("int-test-config.yml")
    );

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> APP_NO_EXTRAS = new DropwizardAppRule<>(
            TestApp.class,
            ResourceHelpers.resourceFilePath("int-test-config-no-extras.yml")
    );

    private static Client client;

    @BeforeClass
    public static void initializeClient() {
        client = ClientBuilder.newClient();
    }

    @Test
    public void testSimpleUsage() throws URISyntaxException {
        String response = localTarget(client, APP, "/docs/index.md").get(String.class);
        assertTrue("The page is rendered from markdown to HTML", response.contains("<h1 id=\"test\">"));
    }

    @Test
    public void testMermaidInclusion() throws URISyntaxException {
        String response = localTarget(client, APP, "/docs/index.md").get(String.class);
        assertTrue("Mermaid JS is included if the app is configured to do so", response.contains("mermaid.init"));
    }

    @Test
    public void testMermaidNonInclusion() throws URISyntaxException {
        String response = localTarget(client, APP_NO_EXTRAS, "/docs/index.md").get(String.class);
        assertFalse("Mermaid JS is not included if the app is configured as such", response.contains("mermaid.init"));
    }

    @Test
    public void testHlJsInclusion() throws URISyntaxException {
        String response = localTarget(client, APP, "/docs/index.md").get(String.class);
        assertTrue("HlJs is included if the app is configured to do so", response.contains("hljs.initHighlightingOnLoad"));
    }

    @Test
    public void testHlJsNonInclusion() throws URISyntaxException {
        String response = localTarget(client, APP_NO_EXTRAS, "/docs/index.md").get(String.class);
        assertFalse("HlJs is not included if the app is configured as such", response.contains("hljs.initHighlightingOnLoad"));
    }

    @Test
    public void testGoogleAnalyticsInclusion() throws URISyntaxException {
        String response = localTarget(client, APP, "/docs/index.md").get(String.class);
        assertTrue("Google Analytics is included if the app is configured to do so", response.contains("GoogleAnalyticsObject"));
    }

    @Test
    public void testGoogleAnalyticsNonInclusion() throws URISyntaxException {
        String response = localTarget(client, APP_NO_EXTRAS, "/docs/index.md").get(String.class);
        assertFalse("Google Analytics is not included if the app is configured as such", response.contains("GoogleAnalyticsObject"));
    }

    public static class TestApp extends Application<TestConfiguration> {

        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(new MarkdownAssetsBundle());
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {

        }
    }

    public static class TestConfiguration extends Configuration implements MarkdownBundleConfiguration {

        public MarkdownAssetsConfiguration assets = new MarkdownAssetsConfiguration();

        @Override
        public MarkdownAssetsConfiguration getMarkdownAssetsConfiguration() {
            return assets;
        }
    }
}

