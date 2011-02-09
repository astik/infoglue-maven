package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.webdav.PropPatchRequestParser.ParseResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;

/**
 * Example request (from ms office)
 *
 * PROPPATCH /Documents/test.docx HTTP/1.1
content-length: 371
cache-control: no-cache
connection: Keep-Alive
host: milton:8080
user-agent: Microsoft-WebDAV-MiniRedir/6.0.6001
pragma: no-cache
translate: f
if: (<opaquelocktoken:900f718e-801c-4152-ae8e-f9395fe45d71>)
content-type: text/xml; charset="utf-8"
<?xml version="1.0" encoding="utf-8" ?>
 * <D:propertyupdate xmlns:D="DAV:" xmlns:Z="urn:schemas-microsoft-com:">
 *  <D:set>
 *  <D:prop>
 *  <Z:Win32LastAccessTime>Wed, 10 Dec 2008 21:55:22 GMT</Z:Win32LastAccessTime>
 *  <Z:Win32LastModifiedTime>Wed, 10 Dec 2008 21:55:22 GMT</Z:Win32LastModifiedTime>
 *  <Z:Win32FileAttributes>00000020</Z:Win32FileAttributes>
 * </D:prop>
 * </D:set>
 * </D:propertyupdate>
 *
 *
 * And another example request (from spec)
 *
 *    <?xml version="1.0" encoding="utf-8" ?>
<D:propertyupdate xmlns:D="DAV:"
xmlns:Z="http://www.w3.com/standards/z39.50/">
<D:set>
<D:prop>
<Z:authors>
<Z:Author>Jim Whitehead</Z:Author>
<Z:Author>Roy Fielding</Z:Author>
</Z:authors>
</D:prop>
</D:set>
<D:remove>
<D:prop><Z:Copyright-Owner/></D:prop>
</D:remove>
</D:propertyupdate>

 *
 *
 * Here is an example response (from the spec)
 *
 *    HTTP/1.1 207 Multi-Status
Content-Type: text/xml; charset="utf-8"
Content-Length: xxxx

<?xml version="1.0" encoding="utf-8" ?>
<D:multistatus xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50">
<D:response>
<D:href>http://www.foo.com/bar.html</D:href>
<D:propstat>
<D:prop><Z:Authors/></D:prop>
<D:status>HTTP/1.1 424 Failed Dependency</D:status>
</D:propstat>
<D:propstat>
<D:prop><Z:Copyright-Owner/></D:prop>
<D:status>HTTP/1.1 409 Conflict</D:status>
</D:propstat>
<D:responsedescription> Copyright Owner can not be deleted or altered.</D:responsedescription>
</D:response>
</D:multistatus>

 *
 *
 * @author brad
 */
public class PropPatchHandler implements ExistingEntityHandler {

    private final static Logger log = LoggerFactory.getLogger( PropPatchHandler.class );

    private final ResourceHandlerHelper resourceHandlerHelper;

    private final PropPatchRequestParser requestParser;

    private final PropPatchSetter patchSetter;

    private final WebDavResponseHandler responseHandler;

    public PropPatchHandler( ResourceHandlerHelper resourceHandlerHelper, WebDavResponseHandler responseHandler, PropPatchSetter propPatchSetter ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.requestParser = new DefaultPropPatchParser();
        patchSetter = propPatchSetter;
        this.responseHandler = responseHandler;
    }

    public PropPatchHandler( ResourceHandlerHelper resourceHandlerHelper, PropPatchRequestParser requestParser, PropPatchSetter patchSetter, WebDavResponseHandler responseHandler ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.requestParser = requestParser;
        this.patchSetter = patchSetter;
        this.responseHandler = responseHandler;
    }





    public String[] getMethods() {
        return new String[]{Method.PROPPATCH.code};
    }

    public boolean isCompatible( Resource r ) {
        return patchSetter.supports( r );
    }

    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource( manager, request, response, r, this );
    }

    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        // todo: check if token header
        try {
            InputStream in = request.getInputStream();
            ParseResult parseResult = requestParser.getRequestedFields( in );
            String href = request.getAbsoluteUrl();
            List<PropFindResponse> responses = new ArrayList<PropFindResponse>();
            PropFindResponse resp = patchSetter.setProperties( href, parseResult, resource );
            responses.add(resp);
            responseHandler.respondPropFind( responses, response, request, resource);
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        } catch( ReadingException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }


    public static class Field {

        public final String name;
        String namespaceUri;

        public Field( String name ) {
            this.name = name;
        }

        public void setNamespaceUri( String namespaceUri ) {
            this.namespaceUri = namespaceUri;
        }

        public String getNamespaceUri() {
            return namespaceUri;
        }
    }

    public static class SetField extends Field {

    	public final String value;

        public SetField( String name, String value ) {
            super( name );
            this.value = value;
        }
    }

    public static class Fields implements Iterable<Field> {

        /**
         * fields to remove
         */
       public  final List<Field> removeFields = new ArrayList<Field>();
        /**
         * fields to set to a value
         */
        public final List<SetField> setFields = new ArrayList<PropPatchHandler.SetField>();

        private int size() {
            return removeFields.size() + setFields.size();
        }

        public Iterator<Field> iterator() {
            List<Field> list = new ArrayList<Field>( removeFields );
            list.addAll( setFields );
            return list.iterator();
        }
    }
}
