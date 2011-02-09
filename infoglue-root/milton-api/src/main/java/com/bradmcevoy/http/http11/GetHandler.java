package com.bradmcevoy.http.http11;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetHandler implements ExistingEntityHandler {

    private static final Logger log = LoggerFactory.getLogger( GetHandler.class );
    private final Http11ResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;
    private final ResourceHandlerHelper resourceHandlerHelper;


    public GetHandler( Http11ResponseHandler responseHandler, HandlerHelper handlerHelper ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
        this.resourceHandlerHelper = new ResourceHandlerHelper( handlerHelper, responseHandler );
    }

    @Override
    public void process( HttpManager manager, Request request, Response response ) throws NotAuthorizedException, ConflictException, BadRequestException {
        this.resourceHandlerHelper.process( manager, request, response, this );
    }

    @Override
    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        manager.onGet( request, response, r, request.getParams() );
        resourceHandlerHelper.processResource( manager, request, response, r, this, true, request.getParams(), null);
    }

    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource) throws NotAuthorizedException, BadRequestException, ConflictException {
//        log.debug( "process: " + request.getAbsolutePath() );
        GetableResource r = (GetableResource) resource;
        if( checkConditional( r, request ) ) {
            responseHandler.respondNotModified( r, response, request );
            return;
        }
        
        sendContent( manager, request, response, r, request.getParams() );
    }

    public Range getRange( Request requestInfo ) {
        // Thanks Igor!
        String rangeHeader = requestInfo.getRangeHeader();
        if( rangeHeader == null ) return null;
        final Matcher matcher = Pattern.compile( "\\s*bytes\\s*=\\s*(\\d+)-(\\d+)" ).matcher( rangeHeader );
        if( matcher.matches() ) {
            return new Range( Long.parseLong( matcher.group( 1 ) ), Long.parseLong( matcher.group( 2 ) ) );
        }
        return null;
    }

    /** Return true if the resource has not been modified
     */
    private boolean checkConditional( GetableResource resource, Request request ) {
        // If maxAgeSeconds is null then we do not cache
        if( resource.getMaxAgeSeconds( request.getAuthorization()) == null ) {
            return false;
        }
        if( checkIfMatch( resource, request ) ) {
            return true;
        }
        if( checkIfModifiedSince( resource, request ) ) {
            return true;
        }
        if( checkIfNoneMatch( resource, request ) ) {
            return true;
        }
        return false;
    }


    private boolean checkIfMatch( GetableResource handler, Request requestInfo ) {
        return false;   // TODO: not implemented
    }

    /**
     *
     * @param resource
     * @param requestInfo
     * @return - true if the resource has NOT been modified since that date in the request
     */
    private boolean checkIfModifiedSince( GetableResource resource, Request requestInfo ) {
        Long maxAgeSecs = resource.getMaxAgeSeconds( requestInfo.getAuthorization());

        if( maxAgeSecs == null  ) {
            return false; // if null, always generate a fresh response
        } else {
            Date dtRequest = requestInfo.getIfModifiedHeader();
            if( dtRequest == null ) return false;
            Date dtCurrent = resource.getModifiedDate();
            if( dtCurrent == null ) return true;
            long timeNow = System.currentTimeMillis();
            long timeRequest = dtRequest.getTime() + 1000; // allow for rounding to nearest second
            long timeElapsed = timeNow - timeRequest;
            if( timeElapsed > maxAgeSecs) {
                // its been longer then the max age period, so generate fresh response
                return false;
            } else {
                long timeActual = dtCurrent.getTime();
        //        log.debug("times as long: " + dtCurrent.getTime() + " - " + dtRequest.getTime());
                boolean unchangedSince = ( timeRequest >= timeActual );
        //        log.debug("checkModifiedSince: actual: " + dtCurrent + " - request:" + dtRequest + " = " + unchangedSince  + " (true indicates no change)");
                
                // If the modified time requested is greater or equal then the actual modified time, do not generate response
                return unchangedSince;
            }
        }
    }

    private boolean checkIfNoneMatch( GetableResource handler, Request requestInfo ) {
        return false;   // TODO: not implemented
    }

    @Override
    public String[] getMethods() {
        return new String[]{Request.Method.GET.code, Request.Method.HEAD.code};
    }

    @Override
    public boolean isCompatible( Resource handler ) {
        return ( handler instanceof GetableResource );
    }

    private void sendContent( HttpManager manager, Request request, Response response, GetableResource resource, Map<String, String> params ) throws NotAuthorizedException, BadRequestException {
        try {
            if( request.getMethod().equals( Method.HEAD ) ) {
                responseHandler.respondHead( resource, response, request );
            } else {
                Range range = getRange( request );
                if( range != null ) {
                    log.trace( "partial");
                    responseHandler.respondPartialContent( resource, response, request, params, range );
                } else {
                    log.trace( "normal content");
                    responseHandler.respondContent( resource, response, request, params );
                }
            }
        } catch( NotAuthorizedException notAuthorizedException ) {
            throw notAuthorizedException;
        } catch( BadRequestException badRequestException ) {
            throw badRequestException;
        } catch(Throwable e) {
            log.error( "Exception sending content for:" + request.getAbsolutePath() + " of resource type: " + resource.getClass().getCanonicalName());
            throw new RuntimeException( e );
        }
    }
}
