package org.rnorth.dropwizard.markdown;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.ImmutableList;
import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.options.DataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.rnorth.dropwizard.markdown.internal.MarkdownAssetsServlet;

import java.nio.charset.Charset;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>An assets bundle (like {@link io.dropwizard.assets.AssetsBundle}) that allows a dropwizard app to serve
 * rendered Markdown documents from the classpath. The goal is to provide a way to quickly and easily serve up
 * static documentation for a service, taking advantage of Markdown's ease of editing and reusability.</p>
 *
 * <p>Additionally, it is heavily inspired by {@link com.bazaarvoice.dropwizard.assets.ConfiguredAssetsBundle}, which:
 * </p>
 *
 * <blockquote>utilizes configuration to provide the
 * ability to override how assets are loaded and cached.  Specifying an override is useful during the development phase
 * to allow assets to be loaded directly out of source directories instead of the classpath and to force them to not be
 * cached by the browser or the server.  This allows developers to edit an asset, save and then immediately refresh the
 * web browser and see the updated assets.  No compilation or copy steps are necessary.</blockquote>
 */
public class MarkdownAssetsBundle implements ConfiguredBundle<MarkdownBundleConfiguration> {


    public static final String DEFAULT_PATH = "/assets";
    public static final String DEFAULT_INDEX_FILE = "index.md";
    public static final String DEFAULT_SERVLET_MAPPING_NAME = "assets";
    public static final CacheBuilderSpec DEFAULT_CACHE_SPEC = CacheBuilderSpec.parse("expireAfterWrite=5s");

    public static final List<Extension> DEFAULT_FLEXMARK_EXTENSIONS = ImmutableList.of(
            AnchorLinkExtension.create(),
            AutolinkExtension.create(),
            FootnoteExtension.create(),
            StrikethroughExtension.create(),
            TaskListExtension.create(),
            TablesExtension.create(),
            SimTocExtension.create(),
            TypographicExtension.create());

    public static final DataHolder DEFAULT_FLEXMARK_OPTIONS = new MutableDataSet()
            .set(AnchorLinkExtension.ANCHORLINKS_SET_ID, true)
            .set(HtmlRenderer.RENDER_HEADER_ID, true)
            .set(HtmlRenderer.GENERATE_HEADER_ID, true)
            .toImmutable();

    private String resourcePath = DEFAULT_PATH;
    private String uriPath = DEFAULT_PATH;
    private String indexFile = DEFAULT_INDEX_FILE;
    private String assetsName = DEFAULT_SERVLET_MAPPING_NAME;
    private CacheBuilderSpec cacheBuilderSpec = DEFAULT_CACHE_SPEC;
    private List<Extension> extensions = DEFAULT_FLEXMARK_EXTENSIONS;
    private DataHolder options = DEFAULT_FLEXMARK_OPTIONS;

    /**
     * Creates a new {@link MarkdownAssetsBundle} which serves up static assets from
     * {@code src/main/resources/assets/*} as {@code /assets/*}.
     */
    public MarkdownAssetsBundle() {
    }

    /**
     * Fluent setter for {@code resourcePath}
     * @param resourcePath the resource path (in the classpath) of the markdown and static asset files
     * @return
     */
    public MarkdownAssetsBundle withResourcePath(String resourcePath) {
        checkArgument(resourcePath.startsWith("/"), "%s is not an absolute path", resourcePath);
        resourcePath = resourcePath.endsWith("/") ? resourcePath : (resourcePath + '/');

        this.resourcePath = resourcePath;
        return this;
    }

    /**
     * Fluent setter for {@code uriPath}
     * @param uriPath      the uri path for the markdown and static asset files
     * @return
     */
    public MarkdownAssetsBundle withUriPath(String uriPath) {
        checkArgument(!"/".equals(resourcePath), "%s is the classpath root", resourcePath);
        uriPath = uriPath.endsWith("/") ? uriPath : (uriPath + '/');

        this.uriPath = uriPath;
        return this;
    }

    /**
     * Fluent setter for {@code indexFile}
     * @param indexFile    the name of the index file to use
     * @return
     */
    public MarkdownAssetsBundle withIndexFile(String indexFile) {
        this.indexFile = indexFile;
        return this;
    }

    /**
     * Fluent setter for {@code assetsName}
     * @param assetsName   the name of servlet mapping used for this assets bundle
     * @return
     */
    public MarkdownAssetsBundle withAssetsName(String assetsName) {
        this.assetsName = assetsName;
        return this;
    }

    /**
     * Fluent setter for {@code cacheBuilderSpec}
     * @param cacheBuilderSpec the spec for the cache builder
     * @return
     */
    public MarkdownAssetsBundle withCacheBuilderSpec(CacheBuilderSpec cacheBuilderSpec) {
        this.cacheBuilderSpec = cacheBuilderSpec;
        return this;
    }

    /**
     * Fluent setter for {@code extensions}
     * @param extensions a list of flexmark-java extensions that should be used for markdown parsing/rendering
     * @return
     */
    public MarkdownAssetsBundle withFlexMarkExtensions(List<Extension> extensions) {
        this.extensions = extensions;
        return this;
    }

    /**
     * Fluent setter for {@code options}
     * @param options collection of flexmark-java options that should be used for markdown parsing/rendering
     * @return
     */
    public MarkdownAssetsBundle withFlexMarkOptions(DataHolder options) {
        this.options = options;
        return this;
    }


    @Override
    public void run(MarkdownBundleConfiguration configuration, Environment environment) throws Exception {

        MarkdownAssetsServlet servlet =
                new MarkdownAssetsServlet(
                        resourcePath,
                        uriPath,
                        indexFile,
                        Charset.defaultCharset(),
                        configuration.getMarkdownAssetsConfiguration(),
                        extensions,
                        options,
                        cacheBuilderSpec);

        environment.servlets()
                .addServlet(assetsName, servlet)
                .addMapping(uriPath + "*");
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
