package com.bradmcevoy.http;

import com.bradmcevoy.http.http11.Http11Protocol;
import com.bradmcevoy.http.quota.DefaultStorageChecker;
import com.bradmcevoy.http.quota.StorageChecker;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author brad
 */
public class ProtocolHandlers implements Iterable<HttpExtension> {

    private final List<HttpExtension> handlers;

    public ProtocolHandlers( List<HttpExtension> handlers ) {
        this.handlers = handlers;
    }

    public ProtocolHandlers( WebDavResponseHandler responseHandler, AuthenticationService authenticationService ) {
        this.handlers = new ArrayList<HttpExtension>();
        List<StorageChecker> quotaCheckers = new ArrayList<StorageChecker>();
        quotaCheckers.add( new DefaultStorageChecker() );
        HandlerHelper handlerHelper = new HandlerHelper( authenticationService, quotaCheckers );
        this.handlers.add( new Http11Protocol( responseHandler, handlerHelper ) );
        this.handlers.add( new WebDavProtocol( responseHandler, handlerHelper ) );
    }

    public ProtocolHandlers( WebDavResponseHandler responseHandler, HandlerHelper handlerHelper ) {
        this.handlers = new ArrayList<HttpExtension>();
        this.handlers.add( new Http11Protocol( responseHandler, handlerHelper ) );
        this.handlers.add( new WebDavProtocol( responseHandler, handlerHelper ) );
    }

    public ProtocolHandlers() {
        this.handlers = new ArrayList<HttpExtension>();
        AuthenticationService authenticationService = new AuthenticationService();
        WebDavResponseHandler responseHandler = new DefaultWebDavResponseHandler( authenticationService );
        HandlerHelper handlerHelper = new HandlerHelper( authenticationService, new ArrayList<StorageChecker>() );
        this.handlers.add( new Http11Protocol( responseHandler, handlerHelper ) );
        this.handlers.add( new WebDavProtocol( responseHandler, handlerHelper ) );
    }

    public Iterator<HttpExtension> iterator() {
        return handlers.iterator();
    }
}
