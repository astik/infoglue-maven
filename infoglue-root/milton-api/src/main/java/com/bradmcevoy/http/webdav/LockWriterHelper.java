package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockInfo.LockScope;
import com.bradmcevoy.http.LockInfo.LockType;
import com.bradmcevoy.http.XmlWriter;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class LockWriterHelper {

    private static final Logger log = LoggerFactory.getLogger( LockWriterHelper.class );

    private boolean stripHrefOnOwner = true;

    public void appendDepth( XmlWriter writer, LockInfo.LockDepth depthType ) {
        String s = "Infinity";
        if( depthType != null ) {
            if( depthType.equals( LockInfo.LockDepth.INFINITY ) )
                s = depthType.name().toUpperCase();
        }
        writer.writeProperty( null, "D:depth", s );

    }

    public void appendOwner( XmlWriter writer, String owner ) {
        boolean validHref;
        if( owner == null ) {
            log.warn( "owner is null");
            validHref = false;
        } else {
            validHref = isValidHref( owner );
        }
        log.debug( "appendOwner: " + validHref + " - " + stripHrefOnOwner);
        if( !validHref && stripHrefOnOwner ) { // BM: reversed login on validHref - presumably only write href tag for href values???
            writer.writeProperty( null, "D:owner", owner );
        } else {
            XmlWriter.Element el = writer.begin( "D:owner" ).open();
            XmlWriter.Element el2 = writer.begin( "D:href" ).open();
            if( owner != null ) {
                el2.writeText( owner );
            }
            el2.close();
            el.close();
        }        
    }

    public void appendScope( XmlWriter writer, LockScope scope ) {
        writer.writeProperty( null, "D:lockscope", "<D:" + scope.toString().toLowerCase() + "/>" );
    }

    public void appendTimeout( XmlWriter writer, Long seconds ) {
        if( seconds != null && seconds > 0 ) {
            writer.writeProperty( null, "D:timeout", "Second-" + seconds );
        }
    }

    public void appendTokenId( XmlWriter writer, String tokenId ) {
        XmlWriter.Element el = writer.begin( "D:locktoken" ).open();
        writer.writeProperty( null, "D:href", "opaquelocktoken:" + tokenId );
        el.close();
    }

    public void appendType( XmlWriter writer, LockType type ) {
        writer.writeProperty( null, "D:locktype", "<D:" + type.toString().toLowerCase() + "/>" );
    }

    public void appendRoot( XmlWriter writer, String lockRoot ) {
        XmlWriter.Element el = writer.begin( "D:lockroot" ).open();
        writer.writeProperty( null, "D:href", lockRoot );
        el.close();
    }

    /**
     * If set the owner value will not be wrapped in an href tag unless it is
     * a valid URL.
     * E.g. true: this -> <owner>this</owner>
     *    false: that -> <owner><href>that</href></owner>
     *
     * See also LockTokenValueWriter.java
     *
     * @return
     */
    public boolean isStripHrefOnOwner() {
        return stripHrefOnOwner;
    }

    public void setStripHrefOnOwner( boolean stripHrefOnOwner ) {
        this.stripHrefOnOwner = stripHrefOnOwner;
    }

    private boolean isValidHref( String owner ) {
        log.debug( "isValidHref: " + owner);
        if(owner.startsWith( "http")) {
            try {
                URI u = new URI( owner );
                log.debug( "uri: " + u);
                return true;
            } catch( URISyntaxException ex ) {
                log.debug( "ex: " + ex);
                return false;
            }
        } else {
            return false;
        }
    }
}
