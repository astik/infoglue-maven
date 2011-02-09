package com.bradmcevoy.http.values;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.webdav.LockWriterHelper;
import java.util.Map;

public class LockTokenValueWriter implements ValueWriter {

    private LockWriterHelper lockWriterHelper = new LockWriterHelper();

    public LockWriterHelper getLockWriterHelper() {
        return lockWriterHelper;
    }

    public void setLockWriterHelper( LockWriterHelper lockWriterHelper ) {
        this.lockWriterHelper = lockWriterHelper;
    }

    public boolean supports( String nsUri, String localName, Class c ) {
        return LockToken.class.isAssignableFrom( c );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        LockToken token = (LockToken) val;
        Element lockentry = writer.begin( "D:lockdiscovery" ).open();
        if( token != null ) {
            LockInfo info = token.info;
            lockWriterHelper.appendType( writer, info.type );
            lockWriterHelper.appendScope( writer, info.scope );
            lockWriterHelper.appendDepth( writer, info.depth );
            lockWriterHelper.appendOwner( writer, info.lockedByUser );
            lockWriterHelper.appendTimeout( writer, token.timeout.getSeconds() );
            lockWriterHelper.appendTokenId( writer, token.tokenId );
            lockWriterHelper.appendRoot( writer, href );
        }
        lockentry.close();
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
