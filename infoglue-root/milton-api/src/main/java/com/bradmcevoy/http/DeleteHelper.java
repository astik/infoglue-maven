package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * Supporting functions for the DeleteHandler
 *
 */
public interface DeleteHelper {
    /**
     * Check if the resource or any child resources are locked or otherwise not
     * deletable
     *
     * @param req
     * @param r
     * @return
     */
    boolean isLockedOut(Request req, Resource r);

    /**
     * Delete the resource and any child resources
     *
     * @param r
     */
    void delete(DeletableResource r) throws NotAuthorizedException, ConflictException, BadRequestException;
}
