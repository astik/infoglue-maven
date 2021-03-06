package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.HrefStatus;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.Utils;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.DefaultHttp11ResponseHandler;
import com.bradmcevoy.http.http11.Http11ResponseHandler;
import com.bradmcevoy.http.values.ValueWriters;
import com.bradmcevoy.http.quota.StorageChecker.StorageErrorReason;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class DefaultWebDavResponseHandler implements WebDavResponseHandler {

    protected final Http11ResponseHandler wrapped;
    protected final ResourceTypeHelper resourceTypeHelper;
    protected final PropFindXmlGenerator propFindXmlGenerator;

    public DefaultWebDavResponseHandler(AuthenticationService authenticationService) {
        ValueWriters valueWriters = new ValueWriters();
        wrapped = new DefaultHttp11ResponseHandler(authenticationService);
        resourceTypeHelper = new WebDavResourceTypeHelper();
        propFindXmlGenerator = new PropFindXmlGenerator( valueWriters );
    }

    public DefaultWebDavResponseHandler(AuthenticationService authenticationService, ResourceTypeHelper resourceTypeHelper) {
        ValueWriters valueWriters = new ValueWriters();
        wrapped = new DefaultHttp11ResponseHandler(authenticationService);
        this.resourceTypeHelper = resourceTypeHelper;
        propFindXmlGenerator = new PropFindXmlGenerator( valueWriters );

    }

    public DefaultWebDavResponseHandler( ValueWriters valueWriters, AuthenticationService authenticationService ) {
        wrapped = new DefaultHttp11ResponseHandler(authenticationService);
        resourceTypeHelper = new WebDavResourceTypeHelper();
        propFindXmlGenerator = new PropFindXmlGenerator( valueWriters );
    }

    public DefaultWebDavResponseHandler( ValueWriters valueWriters, AuthenticationService authenticationService, ResourceTypeHelper resourceTypeHelper ) {
        wrapped = new DefaultHttp11ResponseHandler(authenticationService);
        this.resourceTypeHelper = resourceTypeHelper;
        propFindXmlGenerator = new PropFindXmlGenerator( valueWriters );
    }

    public DefaultWebDavResponseHandler( Http11ResponseHandler wrapped, ResourceTypeHelper resourceTypeHelper, PropFindXmlGenerator propFindXmlGenerator ) {
        this.wrapped = wrapped;
        this.resourceTypeHelper = resourceTypeHelper;
        this.propFindXmlGenerator = propFindXmlGenerator;
    }

    public String generateEtag( Resource r ) {
        return wrapped.generateEtag( r );
    }



    public void respondWithOptions( Resource resource, Response response, Request request, List<String> methodsAllowed ) {
        wrapped.respondWithOptions( resource, response, request, methodsAllowed );
        List<String> supportedLevels = resourceTypeHelper.getSupportedLevels( resource );
        String s = Utils.toCsv( supportedLevels );
        response.setDavHeader( s );
        response.setNonStandardHeader( "MS-Author-Via", "DAV" );
    }

    public void responseMultiStatus( Resource resource, Response response, Request request, List<HrefStatus> statii ) {
        response.setStatus( Response.Status.SC_MULTI_STATUS );
        response.setContentTypeHeader( Response.XML );

        XmlWriter writer = new XmlWriter( response.getOutputStream() );
        writer.writeXMLHeader();
        writer.open( "multistatus xmlns:D" + "=\"" + WebDavProtocol.NS_DAV + ":\"" ); // only single namespace for this method
        writer.newLine();
        for( HrefStatus status : statii ) {
            XmlWriter.Element elResponse = writer.begin( "response" ).open();
            writer.writeProperty( "", "href", status.href );
            writer.writeProperty( "", "status", status.status.code + "" );
            elResponse.close();
        }
        writer.close( "multistatus" );
        writer.flush();

    }

    public void respondNoContent( Resource resource, Response response, Request request ) {
        wrapped.respondNoContent( resource, response, request );
    }

    public void respondContent( Resource resource, Response response, Request request, Map<String, String> params ) throws NotAuthorizedException, BadRequestException {
        wrapped.respondContent( resource, response, request, params );
    }

    public void respondPartialContent( GetableResource resource, Response response, Request request, Map<String, String> params, Range range ) throws NotAuthorizedException, BadRequestException {
        wrapped.respondPartialContent( resource, response, request, params, range );
    }

    public void respondCreated( Resource resource, Response response, Request request ) {
        wrapped.respondCreated( resource, response, request );
    }

    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        wrapped.respondUnauthorised( resource, response, request );
    }

    public void respondMethodNotImplemented( Resource resource, Response response, Request request ) {
        wrapped.respondMethodNotImplemented( resource, response, request );
    }

    public void respondMethodNotAllowed( Resource res, Response response, Request request ) {
        wrapped.respondMethodNotAllowed( res, response, request );
    }

    public void respondConflict( Resource resource, Response response, Request request, String message ) {
        wrapped.respondConflict( resource, response, request, message );
    }

    public void respondRedirect( Response response, Request request, String redirectUrl ) {
        wrapped.respondRedirect( response, request, redirectUrl );
    }

    public void respondNotModified( GetableResource resource, Response response, Request request ) {
        wrapped.respondNotModified( resource, response, request );
    }

    public void respondNotFound( Response response, Request request ) {
        wrapped.respondNotFound( response, request );
    }

    public void respondHead( Resource resource, Response response, Request request ) {
        wrapped.respondHead( resource, response, request );
    }

    public void respondExpectationFailed( Response response, Request request ) {
        wrapped.respondExpectationFailed( response, request );
    }

    public void respondBadRequest( Resource resource, Response response, Request request ) {
        wrapped.respondBadRequest( resource, response, request );
    }

    public void respondForbidden( Resource resource, Response response, Request request ) {
        wrapped.respondForbidden( resource, response, request );
    }



    public void respondServerError( Request request, Response response, String reason ) {
        wrapped.respondServerError( request, response, reason );
    }



    public void respondDeleteFailed( Request request, Response response, Resource resource, Status status ) {
        List<HrefStatus> statii = new ArrayList<HrefStatus>();
        statii.add( new HrefStatus( request.getAbsoluteUrl(), status ) );
        responseMultiStatus( resource, response, request, statii );

    }

    public void respondPropFind( List<PropFindResponse> propFindResponses, Response response, Request request, Resource r ) {
        response.setStatus( Status.SC_MULTI_STATUS );
        response.setContentTypeHeader( Response.XML );
        String xml = propFindXmlGenerator.generate( propFindResponses );
        byte[] arr;
        try {
            arr = xml.getBytes( "UTF-8" );
        } catch( UnsupportedEncodingException ex ) {
            throw new RuntimeException( ex );
        }
        response.setContentLengthHeader( (long)arr.length );
        try {
            response.getOutputStream().write( arr );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public void respondInsufficientStorage( Request request, Response response, StorageErrorReason storageErrorReason ) {
        response.setStatus( Status.SC_INSUFFICIENT_STORAGE );
    }

    public void respondLocked( Request request, Response response, Resource existingResource ) {
        response.setStatus( Status.SC_LOCKED );
    }

    public void respondPreconditionFailed( Request request, Response response, Resource resource ) {
        response.setStatus( Status.SC_PRECONDITION_FAILED );
    }


}
