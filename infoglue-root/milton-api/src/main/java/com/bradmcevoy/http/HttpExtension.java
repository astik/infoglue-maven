package com.bradmcevoy.http;

import java.util.Set;

/**
 *
 * @author brad
 */
public interface HttpExtension {

    /**
     *
     * @return - all method handlers that this extension supports.
     */
    Set<Handler> getHandlers();


//    public void setResponseHeaders( Request request, Response response, Resource resource, Status status );
}
