package org.rnorth.dropwizard.markdown.internal;

import com.google.common.base.Strings;
import org.rnorth.dropwizard.markdown.MarkdownAssetsConfiguration;

/**
 * Created by rnorth on 29/09/2016.
 */
public class PageModel {
    private final String html;
    private final String title;
    private final MarkdownAssetsConfiguration configuration;

    public PageModel(String html, String title, MarkdownAssetsConfiguration configuration) {
        this.html = html;
        this.title = title;
        this.configuration = configuration;
    }

    public String getHtml() {
        return html;
    }

    public String getTitle() {
        return title;
    }

    public boolean isUseMermaid() {
        return configuration.isEnableMermaid();
    }

    public boolean isUseGoogleAnalytics() {
        return !Strings.isNullOrEmpty(configuration.getGoogleTrackingId());
    }

    public String getGoogleAnalyticsTrackingId() {
        return configuration.getGoogleTrackingId();
    }

    public boolean isUseHlJs() {
        return configuration.isEnableHlJs();
    }

    public String getCopyrightFooter() {
        return configuration.getCopyrightFooter();
    }
}
