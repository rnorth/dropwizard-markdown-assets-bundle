package org.rnorth.dropwizard.markdown;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by rnorth on 29/09/2016.
 */
public class MarkdownAssetsConfiguration {

    @NotNull
    @JsonProperty
    private String cacheSpec = MarkdownAssetsBundle.DEFAULT_CACHE_SPEC.toParsableString();

    @NotNull
    @JsonProperty
    private boolean enableMermaid = true;

    @NotNull
    @JsonProperty
    private String googleTrackingId = "";

    @NotNull
    @JsonProperty
    private boolean enableHlJs = true;

    @NotNull
    @JsonProperty
    private String copyrightFooter = "";

    public String getCacheSpec() {
        return cacheSpec;
    }

    public boolean isEnableMermaid() {
        return enableMermaid;
    }

    public String getGoogleTrackingId() {
        return googleTrackingId;
    }

    public boolean isEnableHlJs() {
        return enableHlJs;
    }

    public void setEnableHlJs(boolean enableHlJs) {
        this.enableHlJs = enableHlJs;
    }

    public String getCopyrightFooter() {
        return copyrightFooter;
    }

    public void setCopyrightFooter(String copyrightFooter) {
        this.copyrightFooter = copyrightFooter;
    }
}
