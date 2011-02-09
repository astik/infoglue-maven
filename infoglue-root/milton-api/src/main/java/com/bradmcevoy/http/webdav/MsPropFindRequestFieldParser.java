package com.bradmcevoy.http.webdav;

import java.io.InputStream;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Decorator for PropFindRequestFieldParser's.
 *
 * Calls getRequestedFields on the wrapped object. If no fields were requested
 * this class adds the default ones expected
 * by windows clients. This is because windows clients generally do not
 * specify a PROPFIND body and expect the server to return these fields.
 *
 * Note that failing to return exactly the fields expected in the exact order
 * can break webdav on windows.
 *
 * @author brad
 */
public class MsPropFindRequestFieldParser implements PropFindRequestFieldParser{

    private final PropFindRequestFieldParser wrapped;

    public MsPropFindRequestFieldParser( PropFindRequestFieldParser wrapped ) {
        this.wrapped = wrapped;
    }


    public ParseResult getRequestedFields( InputStream in ) {
        ParseResult result = wrapped.getRequestedFields( in );
        if( result.isAllProp() ) return result;
        
        if( result.getNames().size() == 0 ) {
            add( result.getNames(), "creationdate" );
            add( result.getNames(),"getlastmodified" );
            add( result.getNames(),"displayname" );
            add( result.getNames(),"resourcetype" );
            add( result.getNames(),"getcontenttype" );
            add( result.getNames(),"getcontentlength" );
            add( result.getNames(),"getetag" );
        }
        return result;
    }

    private void add( Set<QName> set, String name ) {
        QName qname = new QName( WebDavProtocol.NS_DAV, name);
        set.add( qname );
    }

}
