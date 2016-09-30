package org.rnorth.dropwizard.markdown.internal;

import com.google.common.hash.Hashing;

/**
 * Created by rnorth on 29/09/2016.
 */
class CachedPage {

    public final byte[] renderedBytes;
    public final long lastModifiedTime;
    public final String eTag;
    public final String mimeType;

    public CachedPage(byte[] renderedBytes, long lastModifiedTime, String mimeType) {
        this.renderedBytes = renderedBytes;
        this.lastModifiedTime = lastModifiedTime;
        this.eTag = "\"" + Hashing.murmur3_128().hashBytes(renderedBytes).toString() + "\"";
        this.mimeType = mimeType;
    }
}
