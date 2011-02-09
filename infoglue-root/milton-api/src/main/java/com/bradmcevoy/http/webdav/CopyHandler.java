package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class CopyHandler implements ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger( CopyHandler.class );
    private final WebDavResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;
    private final ResourceHandlerHelper resourceHandlerHelper;
    private DeleteHelper deleteHelper;
    private boolean deleteExistingBeforeCopy = true;

    public CopyHandler( WebDavResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceHandlerHelper resourceHandlerHelper ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.deleteHelper = new DeleteHelperImpl( handlerHelper );
    }

    public String[] getMethods() {
        return new String[]{Method.COPY.code};
    }

    @Override
    public boolean isCompatible( Resource handler ) {
        return ( handler instanceof CopyableResource );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource( manager, request, response, r, this );
    }

    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        CopyableResource r = (CopyableResource) resource;
        String sDest = request.getDestinationHeader();
//        sDest = HttpManager.decodeUrl(sDest);
        URI destUri = URI.create( sDest );
        sDest = destUri.getPath();

        Dest dest = new Dest( destUri.getHost(), sDest );
        Resource rDest = manager.getResourceFactory().getResource( dest.host, dest.url );
        log.debug( "process: copying from: " + r.getName() + " -> " + dest.url + "/" + dest.name );

        if( rDest == null ) {
            log.debug( "process: destination parent does not exist: " + sDest );
            responseHandler.respondConflict( resource, response, request, "Destination does not exist: " + sDest );
        } else if( !( rDest instanceof CollectionResource ) ) {
            log.debug( "process: destination exists but is not a collection" );
            responseHandler.respondConflict( resource, response, request, "Destination exists but is not a collection: " + sDest );
        } else {
            log.debug( "process: copy resource to: " + rDest.getName() );

            Resource fDest = manager.getResourceFactory().getResource( dest.host, dest.url + "/" + dest.name );
            if( handlerHelper.isLockedOut( request, fDest ) ) {
                responseHandler.respondLocked( request, response, resource );
                return ;
            } else {
                CollectionResource colDest = (CollectionResource) rDest;
                Resource rExisting = colDest.child( dest.name );
                if( rExisting != null ) {
                    Boolean overwrite = request.getOverwriteHeader();
                    if( overwrite != null && overwrite.booleanValue() ) {
                        // Exists, and overwrite = F, disallow - http://www.webdav.org/specs/rfc4918.html#rfc.section.9.8.4
                        responseHandler.respondPreconditionFailed( request, response, resource );
                        return;
                    } else {
                        // Overwrite is absent or T, so continue
                        if( deleteHelper.isLockedOut( request, rExisting ) ) {
                            log.info( "copy destination exists but is locked" );
                            responseHandler.respondPreconditionFailed( request, response, resource );
                            return;
                        } else {
                            if( deleteExistingBeforeCopy ) {
                                if( rExisting instanceof DeletableResource ) {
                                    log.debug( "copy destination exists and is deletable, delete it..");
                                    DeletableResource dr = (DeletableResource) rExisting;
                                    deleteHelper.delete( dr );
                                } else {
                                    log.warn( "copy destination exists and is a collection so must be deleted, but does not implement: " + DeletableResource.class );
                                    responseHandler.respondConflict( rExisting, response, request, sDest );
                                    return;
                                }
                            }
                        }
                    }
                }
                r.copyTo( colDest, dest.name );
                responseHandler.respondCreated( resource, response, request );

            }
        }
    }

    public void setDeleteExistingBeforeCopy( boolean deleteExistingBeforeCopy ) {
        this.deleteExistingBeforeCopy = deleteExistingBeforeCopy;
    }

    public boolean isDeleteExistingBeforeCopy() {
        return deleteExistingBeforeCopy;
    }
}
