package com.bradmcevoy.http.webdav;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author brad
 */
public class MsPropFindRequestFieldParserTest extends TestCase {

    MsPropFindRequestFieldParser fieldParser;
    PropFindRequestFieldParser wrapped;
    InputStream request;
    Set<QName> set;

    @Override
    protected void setUp() throws Exception {
        request = createMock( InputStream.class );
        wrapped = createMock( PropFindRequestFieldParser.class );
        fieldParser = new MsPropFindRequestFieldParser( wrapped );
        set = new HashSet<QName>();
    }

    public void testGetRequestedFields_WrappedReturnsFields() {
        set.add( new QName( "a" ) );
        PropFindRequestFieldParser.ParseResult res = new PropFindRequestFieldParser.ParseResult( false, set );
        expect( wrapped.getRequestedFields( request ) ).andReturn( res );
        replay( wrapped );
        PropFindRequestFieldParser.ParseResult actual = fieldParser.getRequestedFields( request );

        verify( wrapped );
        assertSame( res, actual );
    }

    public void testGetRequestedFields_WrappedReturnsNothing() {
        PropFindRequestFieldParser.ParseResult res = new PropFindRequestFieldParser.ParseResult( false, set );
        expect( wrapped.getRequestedFields( request ) ).andReturn( res );
        replay( wrapped );
        PropFindRequestFieldParser.ParseResult actual = fieldParser.getRequestedFields( request );

        verify( wrapped );
        assertEquals( 7, actual.getNames().size() );
    }
}
