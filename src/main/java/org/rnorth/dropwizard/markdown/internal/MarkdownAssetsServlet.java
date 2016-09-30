package org.rnorth.dropwizard.markdown.internal;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.DataHolder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import io.dropwizard.servlets.assets.AssetServlet;
import io.dropwizard.servlets.assets.ResourceURL;
import org.jetbrains.annotations.NotNull;
import org.rnorth.dropwizard.markdown.MarkdownAssetsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static javax.ws.rs.core.HttpHeaders.IF_MODIFIED_SINCE;
import static javax.ws.rs.core.HttpHeaders.IF_NONE_MATCH;

/**
 * Assets servlet that will render markdown assets rather than just passing the raw markdown to the client.
 * <p>
 * Non-markdown asset requests are passed through to a wrapped {@link AssetServlet} instance. This is so that the more
 * advanced HTTP features of {@link AssetServlet} (such as range support and fuller MIME support) remain available.
 */
public class MarkdownAssetsServlet extends HttpServlet {

    /*
     * flexmark-java markdown processors.
     */
    private final Parser parser;
    private final HtmlRenderer renderer;

    /*
     * Wrapped AssetServlet
     */
    private final AssetServlet assetServlet;

    /*
     * Cache for rendered markdown content
     */
    private final LoadingCache<URL, CachedPage> pageCache;

    private final String resourcePath;
    private final String uriPath;
    private final String indexFile;
    private final Charset defaultCharset;
    @NotNull
    private final MarkdownAssetsConfiguration configuration;
    private final URI resourceRootURL;

    private static final Logger logger = LoggerFactory.getLogger(MarkdownAssetsServlet.class);

    /**
     * Construct a {@link MarkdownAssetsServlet} configured with provided parameters, having a wrapped default
     * {@link AssetServlet} for fulfilling non-markdown asset requests.
     * <p>
     * {@code MarkdownAssetsServlet} and {@link AssetServlet} serve static assets loaded from {@code resourceURL}
     * (typically a file: or jar: URL). The assets are served at URIs rooted at {@code uriPath}. For
     * example, given a {@code resourceURL} of {@code "file:/data/assets"} and a {@code uriPath} of
     * {@code "/js"}, an {@code AssetServlet} would serve the contents of {@code
     * /data/assets/example.js} in response to a request for {@code /js/example.js}. If a directory
     * is requested and {@code indexFile} is defined, then {@code AssetServlet} will attempt to
     * serve a file with that name in that directory.
     *
     * @param resourcePath   the base URL from which assets are loaded
     * @param uriPath        the URI path fragment in which all requests are rooted
     * @param indexFile      the filename to use when directories are requested
     * @param defaultCharset the default character set
     * @param configuration  environment-specific configuration properties
     * @param extensions     Flexmark-Java markdown rendering extensions to use
     * @param options        Flexmark-Java markdown rendering options
     */
    public MarkdownAssetsServlet(@NotNull String resourcePath,
                                 @NotNull String uriPath,
                                 @NotNull String indexFile,
                                 @NotNull Charset defaultCharset,
                                 @NotNull MarkdownAssetsConfiguration configuration,
                                 @NotNull List<Extension> extensions,
                                 @NotNull DataHolder options) {

        this.resourcePath = resourcePath;
        this.uriPath = uriPath;
        this.indexFile = indexFile;
        this.defaultCharset = defaultCharset;
        this.configuration = configuration;

        parser = Parser.builder(options).extensions(extensions).build();
        renderer = HtmlRenderer.builder(options).extensions(extensions).build();

        assetServlet = new AssetServlet(resourcePath, uriPath, indexFile, defaultCharset);
        pageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<URL, CachedPage>() {
                    @Override
                    public CachedPage load(@NotNull URL key) throws Exception {
                        return renderPage(key);
                    }
                });
        try {
            URL resource = this.getClass().getResource(resourcePath);
            Preconditions.checkNotNull(resource, "Resource root URL (" + resourcePath + ") was not found");

            resourceRootURL = resource.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Resource root URL (" + resourcePath + ") was ind", e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (uriPath.equals(path)) {
            path = "/" + indexFile;
        }

        CachedPage renderedPage;
        URL localSourceUrl;
        // If it's not markdown, delegate to AssetServlet
        if (path.endsWith(".md")) {
            localSourceUrl = this.getClass().getResource(resourcePath + path);

            // No such resource
            if (localSourceUrl == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Content path was outside of the resource root path - path traversal attempt?
            if (!localSourceUrl.toString().startsWith(resourceRootURL.toString())) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                logger.warn("Resolved asset URL ({}) was outside of resource root ({}) - possible path traversal attempt?", localSourceUrl, resourceRootURL);
                return;
            }

        } else if (path.endsWith("dropwizard-markdown.css")) {
            localSourceUrl = this.getClass().getResource(resourcePath + path);

            if (localSourceUrl == null) {
                // no override provided - use default
                localSourceUrl = this.getClass().getResource("/default-dropwizard-markdown.css");
            }

        } else {
            assetServlet.service(req, resp);
            return;
        }

        // Go ahead and fetch the rendered page (cached if available)
        try {
            renderedPage = pageCache.get(localSourceUrl);
        } catch (ExecutionException e) {
            // No rendered page for some reason
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            logger.error("Error when fetching cached/fresh rendered content", e);
            return;
        }

        // Don't need to send the full page content back, as the client already has latest version
        if (isCachedClientSide(req, renderedPage)) {
            resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        // If reached here, we're sending the full rendered page to the client
        resp.setDateHeader(HttpHeaders.LAST_MODIFIED, renderedPage.lastModifiedTime);
        resp.setHeader(HttpHeaders.ETAG, renderedPage.eTag);
        resp.setContentType(renderedPage.mimeType);

        try (OutputStream outputStream = resp.getOutputStream()) {
            ByteStreams.copy(new ByteArrayInputStream(renderedPage.renderedBytes), outputStream);
        }
    }

    private boolean isCachedClientSide(HttpServletRequest req, CachedPage renderedPage) {
        return Objects.equals(renderedPage.eTag, req.getHeader(IF_NONE_MATCH)) ||
                req.getDateHeader(IF_MODIFIED_SINCE) >= renderedPage.lastModifiedTime;

    }

    @NotNull
    private CachedPage renderPage(URL localSourceUrl) throws IOException, URISyntaxException, TemplateException {

        if (localSourceUrl.toString().endsWith(".md")) {
            return renderMarkdown(localSourceUrl);
        } else {
            return renderLocalAsset(localSourceUrl);
        }
    }

    @NotNull
    private CachedPage renderMarkdown(URL localSourceUrl) throws IOException, URISyntaxException, TemplateException {
        String markdownSource;
        try {
            markdownSource = Resources.toString(localSourceUrl, defaultCharset);
        } catch (IOException e) {
            logger.error("Markdown source (at {}) could not be loaded", localSourceUrl);
            throw e;
        }

        Node parsedMarkdown = parser.parse(markdownSource);
        String html = renderer.render(parsedMarkdown);

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        Template template;
        try {
            cfg.setClassForTemplateLoading(this.getClass(), resourcePath);
            template = cfg.getTemplate("template.ftl");
        } catch (TemplateNotFoundException e) {
            cfg.setClassForTemplateLoading(this.getClass(), "/");
            template = cfg.getTemplate("default-dropwizard-markdown-template.ftl");
        }

        String title = resourceRootURL.relativize(localSourceUrl.toURI()).toString();
        PageModel pageModel = new PageModel(html, title, configuration, uriPath);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            template.process(pageModel, new OutputStreamWriter(baos));

            long lastModified = ResourceURL.getLastModified(localSourceUrl);
            return new CachedPage(baos.toByteArray(), lastModified, MediaType.TEXT_HTML);
        }
    }

    private CachedPage renderLocalAsset(URL localSourceUrl) throws IOException {
        return new CachedPage(Resources.toByteArray(localSourceUrl),
                ResourceURL.getLastModified(localSourceUrl),
                com.google.common.net.MediaType.CSS_UTF_8.toString());
    }
}