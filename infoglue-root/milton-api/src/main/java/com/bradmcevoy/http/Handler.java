package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 *
 * @author brad
 */
public interface Handler {

    /**
     * 
     * @return - the http methods supported by this handler. Must be all upper case.
     */
    String[] getMethods();

    void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException;

    /**
     *
     * @param res
     * @return - true if the given resource is compatible with this method
     */
    boolean isCompatible( Resource res );

}
