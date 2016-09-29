package org.rnorth.dropwizard.markdown;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilderSpec;
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
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.rnorth.dropwizard.markdown.internal.MarkdownAssetsServlet;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by rnorth on 29/09/2016.
 */
public class MarkdownAssetsBundle implements ConfiguredBundle<MarkdownBundleConfiguration> {

    public static final CacheBuilderSpec DEFAULT_CACHE_SPEC = CacheBuilderSpec.parse("expireAfterWrite=5s");

    @Override
    public void run(MarkdownBundleConfiguration configuration, Environment environment) throws Exception {
        List<Extension> extensions = asList(
                AnchorLinkExtension.create(),
                AutolinkExtension.create(),
                FootnoteExtension.create(),
                StrikethroughExtension.create(),
                TaskListExtension.create(),
                TablesExtension.create(),
                SimTocExtension.create(),
                TypographicExtension.create());


        MutableDataHolder options = new MutableDataSet()
                .set(AnchorLinkExtension.ANCHORLINKS_SET_ID, true)
                .set(HtmlRenderer.RENDER_HEADER_ID, true)
                .set(HtmlRenderer.GENERATE_HEADER_ID, true);

        MarkdownAssetsServlet servlet =
                new MarkdownAssetsServlet(
                        "/docs",
                        "/docs",
                        "index.md",
                        Charsets.UTF_8,
                        configuration.getMarkdownAssetsConfiguration(),
                        extensions,
                        options);

        environment.servlets()
                .addServlet("markdownServlet", servlet)
                .addMapping("/docs/*");
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
