package com.bradmcevoy.http.webdav;

import com.bradmcevoy.property.PropertySource;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;


/**
 *
 * @author brad
 */
public class PropFindHandler implements ExistingEntityHandler {

    private static final Logger log = LoggerFactory.getLogger( PropFindHandler.class );

    private final ResourceHandlerHelper resourceHandlerHelper;
    private final PropFindRequestFieldParser requestFieldParser;    
    private final WebDavResponseHandler responseHandler;
    private final PropFindPropertyBuilder propertyBuilder;

    /**
     * 
     * @param resourceHandlerHelper
     * @param resourceTypeHelper
     * @param responseHandler
     */
    public PropFindHandler( ResourceHandlerHelper resourceHandlerHelper, ResourceTypeHelper resourceTypeHelper, WebDavResponseHandler responseHandler, List<PropertySource> propertySources ) {
        this.resourceHandlerHelper = resourceHandlerHelper;

        DefaultPropFindRequestFieldParser defaultFieldParse = new DefaultPropFindRequestFieldParser();
        this.requestFieldParser = new MsPropFindRequestFieldParser(defaultFieldParse); // use MS decorator for windows support
        this.responseHandler = responseHandler;

        this.propertyBuilder = new PropFindPropertyBuilder(propertySources);
    }

    /**
     *
     * @param resourceHandlerHelper
     * @param requestFieldParser
     * @param responseHandler
     * @param propertyBuilder
     */
    public PropFindHandler( ResourceHandlerHelper resourceHandlerHelper, PropFindRequestFieldParser requestFieldParser, WebDavResponseHandler responseHandler, PropFindPropertyBuilder propertyBuilder ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.requestFieldParser = requestFieldParser;
        this.responseHandler = responseHandler;
        this.propertyBuilder = propertyBuilder;
    }




    public String[] getMethods() {
        return new String[]{Method.PROPFIND.code};
    }

    @Override
    public boolean isCompatible( Resource handler ) {
        return ( handler instanceof PropFindableResource );
    }

    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource( manager, request, response, r, this );
    }

    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        log.debug( "propfind");
        PropFindableResource pfr = (PropFindableResource) resource;
        int depth = request.getDepthHeader();
        response.setStatus( Response.Status.SC_MULTI_STATUS );
        response.setContentTypeHeader( Response.XML );
        PropFindRequestFieldParser.ParseResult parseResult;
        try {
            parseResult = requestFieldParser.getRequestedFields( request.getInputStream() );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
        String url = request.getAbsoluteUrl();

        List<PropFindResponse> propFindResponses = propertyBuilder.buildProperties(pfr, depth, parseResult, url);
        log.debug("responses: " + propFindResponses.size());
        responseHandler.respondPropFind(propFindResponses, response, request, pfr);
    }


}
