package com.bradmcevoy.http;

import com.bradmcevoy.http.http11.Http11ResponseHandler;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Milton's main class. All the servlets and stuff is just fluff, this is where stuff really starts to happen
 *
 *
 * @author brad
 */
public class HttpManager {

    private static final Logger log = LoggerFactory.getLogger(HttpManager.class);

    public static String decodeUrl(String s) {
            return Utils.decodePath(s);
    }
   
    private static final ThreadLocal<Request> tlRequest = new ThreadLocal<Request>();
    private static final ThreadLocal<Response> tlResponse = new ThreadLocal<Response>();

    public static Request request() {
        return tlRequest.get();
    }

    public static Response response() {
        return tlResponse.get();
    }


    private final ProtocolHandlers handlers;

    private Map<String, Handler> methodHandlers = new ConcurrentHashMap<String, Handler>();
    
    List<Filter> filters = new ArrayList<Filter>();
    List<EventListener> eventListeners = new ArrayList<EventListener>();
    
    protected final ResourceFactory resourceFactory;

    protected final Http11ResponseHandler responseHandler;
    
    private SessionAuthenticationHandler sessionAuthenticationHandler;

    /**
     * Creates the manager with a DefaultResponseHandler
     *
     * @param resourceFactory
     */
    public HttpManager(ResourceFactory resourceFactory) {
        if( resourceFactory == null ) throw new NullPointerException("resourceFactory cannot be null");
        this.resourceFactory = resourceFactory;
        AuthenticationService authenticationService = new AuthenticationService();
        DefaultWebDavResponseHandler webdavResponseHandler = new DefaultWebDavResponseHandler(authenticationService);
        this.responseHandler = webdavResponseHandler;
        this.handlers = new ProtocolHandlers(webdavResponseHandler, authenticationService);
        
        initHandlers();
    }

    public HttpManager(ResourceFactory resourceFactory, AuthenticationService authenticationService) {
        if( resourceFactory == null ) throw new NullPointerException("resourceFactory cannot be null");
        this.resourceFactory = resourceFactory;
        DefaultWebDavResponseHandler webdavResponseHandler = new DefaultWebDavResponseHandler(authenticationService);
        this.responseHandler = webdavResponseHandler;
        this.handlers = new ProtocolHandlers(webdavResponseHandler, authenticationService);

        initHandlers();
    }


    public HttpManager(ResourceFactory resourceFactory, WebDavResponseHandler responseHandler, AuthenticationService authenticationService) {
        if( resourceFactory == null ) throw new NullPointerException("resourceFactory cannot be null");
        this.resourceFactory = resourceFactory;
        this.responseHandler = responseHandler;
        this.handlers = new ProtocolHandlers(responseHandler, authenticationService);

        initHandlers();
    }

    public HttpManager(ResourceFactory resourceFactory, WebDavResponseHandler responseHandler, ProtocolHandlers handlers) {
        if( resourceFactory == null ) throw new NullPointerException("resourceFactory cannot be null");
        this.resourceFactory = resourceFactory;
        this.responseHandler = responseHandler;
        this.handlers = handlers;

        initHandlers();
    }


    private void initHandlers() {
        for( HttpExtension ext : handlers ) {
            for( Handler h : ext.getHandlers() ) {
                for( String m : h.getMethods() ) {
                    this.methodHandlers.put( m, h );
                }
            }
        }
        filters.add( createStandardFilter() );
    }



    public Handler getMethodHandler(Request.Method m) {
        return methodHandlers.get( m.code );
    }
    
    public ResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public SessionAuthenticationHandler getSessionAuthenticationHandler() {
        return sessionAuthenticationHandler;
    }

    public void setSessionAuthenticationHandler(SessionAuthenticationHandler sessionAuthenticationHandler) {
        this.sessionAuthenticationHandler = sessionAuthenticationHandler;
    }        

    /**
     * 
     * @param request
     * @return - if no SessionAuthenticationHandler has been set returns null. Otherwise,
     *  calls getSessionAuthentication on it and returns the result
     * 
     * 
     */
    public Auth getSessionAuthentication(Request request) {
        if( this.sessionAuthenticationHandler == null ) return null;
        return this.sessionAuthenticationHandler.getSessionAuthentication(request);
    }
       
    public void process(Request request, Response response) {
        log.debug(request.getMethod() + " :: " + request.getAbsoluteUrl() + " - " + request.getAbsoluteUrl());
        tlRequest.set( request );
        tlResponse.set( response );
        try {
            FilterChain chain = new FilterChain( this );
            chain.process( request, response );
        } finally {
            tlRequest.remove();
            tlResponse.remove();
        }
    }
    
    
    
    protected Filter createStandardFilter() {
        return new StandardFilter();
    }
    
            
    public void addFilter(int pos, Filter filter) {
        filters.add(pos,filter);
    }

    public void addEventListener(EventListener l) {
        eventListeners.add(l);
    }
    
    public void removeEventListener(EventListener l) {
        eventListeners.remove(l);
    }
    
    public void onProcessResourceFinish(Request request, Response response, Resource resource, long duration) {
        for( EventListener l : eventListeners ) {
            l.onProcessResourceFinish(request, response, resource,duration);
        }
    }

    public void onProcessResourceStart(Request request, Response response, Resource resource) {
        for( EventListener l : eventListeners ) {
            l.onProcessResourceStart(request, response, resource);
        }        
    }

    public void onPost(Request request, Response response, Resource resource, Map<String, String> params, Map<String, FileItem> files) {
        for( EventListener l : eventListeners ) {
            l.onPost(request, response, resource, params, files);
        }   
    }

    public void onGet(Request request, Response response, Resource resource, Map<String, String> params) {
        for( EventListener l : eventListeners ) {
            l.onGet(request, response, resource, params);
        }   
    }

    public List<Filter> getFilters() {
        ArrayList<Filter> col = new ArrayList<Filter>(filters);
        return col;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
        filters.add(new StandardFilter());
    }

    public void setEventListeners(List<EventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }

    public Collection<Handler> getAllHandlers() {
        return this.methodHandlers.values();
    }

    public Http11ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public ProtocolHandlers getHandlers() {
        return handlers;
    }
}
