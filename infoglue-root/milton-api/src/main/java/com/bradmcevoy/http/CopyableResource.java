package com.bradmcevoy.http;

/**
 * webDAV COPY
 */
public interface CopyableResource extends Resource{
    void copyTo(CollectionResource toCollection, String name);
}
