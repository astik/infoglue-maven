package com.bradmcevoy.http.exceptions;

import com.bradmcevoy.http.*;

/**
 *  Indicates that the requested operation could not be performed because of
 * prior state. Ie there is an existing resource preventing a new one from being
 * created.
 */
public class ConflictException extends MiltonException {

    /**
     * The resource idenfitied by the URI.
     *
     * @param r
     */
    public ConflictException(Resource r) {
        super(r);
    }

}
