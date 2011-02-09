package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.BadRequestException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class StandardFilter implements Filter {
    
    private Logger log = LoggerFactory.getLogger(StandardFilter.class);
    
    public static final String INTERNAL_SERVER_ERROR_HTML = "<html><body><h1>Internal Server Error (500)</h1></body></html>";
    

    public StandardFilter() {
    }
    
    public void process(FilterChain chain, Request request, Response response) {
        HttpManager manager = chain.getHttpManager();
        try {
            Request.Method method = request.getMethod();
            
            Handler handler = manager.getMethodHandler( method );
            if( handler == null ) throw new RuntimeException("No handler for method: " + method.code);        
            
            handler.process(manager,request,response);

        } catch(BadRequestException ex) {
            log.warn( "BadRequestException");
            manager.getResponseHandler().respondBadRequest(ex.getResource(), response, request);
        } catch (ConflictException ex) {
            log.warn( "conflictException");
            manager.getResponseHandler().respondConflict(ex.getResource(), response, request, INTERNAL_SERVER_ERROR_HTML);
        } catch (NotAuthorizedException ex) {
            log.warn( "NotAuthorizedException");
            manager.getResponseHandler().respondUnauthorised(ex.getResource(), response, request);
        } catch(Throwable e) {
            log.error("process", e);
            try {
                manager.getResponseHandler().respondServerError( request, response, INTERNAL_SERVER_ERROR_HTML );
            } catch (Throwable ex) {
                log.error("Exception generating server error response, setting response status to 500", ex);
                response.setStatus(Response.Status.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            response.close();            
        }
    }    
}