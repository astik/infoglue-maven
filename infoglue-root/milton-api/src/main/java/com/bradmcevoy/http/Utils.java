package com.bradmcevoy.http;

import com.bradmcevoy.common.Path;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Utils {

    private final static char[] hexDigits = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static Resource findChild( Resource parent, Path path ) {
        return _findChild( parent, path.getParts(), 0 );
    }

    /**
     * does percentage decoding on a path portion of a url
     *
     * E.g. /foo  > /foo
     * /with%20space -> /with space
     *
     * @param href
     */
    public static String decodePath( String href ) {
        // For IPv6
        href = href.replace( "[", "%5B" ).replace( "]", "%5D" );

        // Seems that some client apps send spaces.. maybe..
        href = href.replace( " ", "%20");
        try {
            if( href.startsWith( "/" ) ) {
                URI uri = new URI( "http://anything.com" + href );
                return uri.getPath();
            } else {
                URI uri = new URI( "http://anything.com/" + href );
                String s = uri.getPath();
                return s.substring( 1 );
            }
        } catch( URISyntaxException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private static Resource _findChild( Resource parent, String[] arr, int i ) {
        if( parent instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource) parent;
            String childName = arr[i];

            Resource child = col.child( childName );
            if( child == null ) {
                return null;
            } else {
                if( i < arr.length - 1 ) {
                    return _findChild( child, arr, i + 1 );
                } else {
                    return child;
                }
            }
        } else {
            return null;
        }
    }

    public static Date now() {
        return new Date();
    }

    public static Date addSeconds( Date dt, long seconds ) {
        return addSeconds( dt, (int) seconds );
    }

    public static Date addSeconds( Date dt, int seconds ) {
        Calendar cal = Calendar.getInstance();
        cal.setTime( dt );
        cal.add( Calendar.SECOND, seconds );
        return cal.getTime();
    }

    public static String getProtocol( String url ) {
        String protocol = url.substring( 0, url.indexOf( ":" ) );
        return protocol;
    }

    public static String escapeXml( String s ) {
        s = s.replaceAll( "\"", "&quot;" );
        s = s.replaceAll( "&", "&amp;" );
        s = s.replaceAll( "'", "&apos;" );
        s = s.replaceAll( "<", "&lt;" );
        s = s.replaceAll( ">", "&gt;" );
//        s = s.replaceAll("�", "ae");
        return s;
    }

    /**
     * this is a modified verion of java.net.URI.encode(s)
     *
     * the java.net version only encodes characters over \u0080, but this
     * version also applies encoding to characters below char 48
     *
     * this method should be applied only to parts of a URL, not the whole
     * URL as forward slashes, semi-colons etc will be encoded
     *
     * by "part of url" i mean the bits between slashes
     *
     * @param s
     */
    public static String percentEncode( String s ) {
        s = _percentEncode( s ); // the original method, from java.net
        s = s.replace( ":", "%3A");
        s = s.replace( ";", "%3B");
        s = s.replace( "=", "%3D");
        s = s.replace( "?", "%3F");
        s = s.replace( "@", "%40");
        return s;
    }
    
    private static String _percentEncode( String s ) {
        int n = s.length();
        if( n == 0 ) {
            return s;
        }

        // First check whether we actually need to encode
        for( int i = 0;; ) {
            char b = s.charAt( i );
            if( b >= '\u0080' || b <= (char) 48 || isSquareBracket( b ) ) {
                break;
            }
            if( ++i >= n ) {
                return s;
            }
        }

        String ns = normalize( s );
        ByteBuffer bb = null;
        bb = Charset.forName( "UTF-8" ).encode( CharBuffer.wrap( ns ) );

        StringBuilder sb = new StringBuilder();
        while( bb.hasRemaining() ) {
            int b = bb.get() & 0xff;
            if( ( b >= 0x80 || b < (char) 48 || isSquareBracket( b ) ) && ( b != '.' && b != '-' ) ) {
                appendEscape( sb, (byte) b );
            } else {
                sb.append( (char) b );
            }
        }
        return sb.toString();
    }

    private static boolean isSquareBracket( int b ) {
        return b == 0x5B || b == 0x5D;
    }

    private static void appendEscape( StringBuilder sb, byte b ) {
        sb.append( '%' );
        sb.append( hexDigits[( b >> 4 ) & 0x0f] );
        sb.append( hexDigits[( b >> 0 ) & 0x0f] );
    }

    public static Date mostRecent( Date... dates ) {
        if( dates == null || dates.length == 0 ) return null;
        Date recent = dates[0];
        for( Date dt : dates ) {
            if( dt.getTime() > recent.getTime() ) recent = dt;
        }
        return recent;
    }

    /**
     * java.text.Normalizer is only available for jdk 1.6. Since it isnt
     * really required and we don't want to annoy our 1.5 colleagues, this
     * is commented out.
     *
     * It isnt really needed because URLs still get consistently encoded and
     * decoded without it. Its just that you might get different results on different
     * platforms
     *
     * @param s
     * @return
     */
    private static String normalize( String s ) {
        //return Normalizer.normalize(s, Normalizer.Form.NFC);
        return s;
    }

    /**
     * Convert the list of strings to a comma separated string
     *
     * @param list
     */
    public static String toCsv(List<String> list) {
        String res = "";
        Iterator<String> it = list.iterator();
        while(it.hasNext()) {
            res += it.next();
            if( it.hasNext() ) res += ", ";
        }
        return res;
    }
}
