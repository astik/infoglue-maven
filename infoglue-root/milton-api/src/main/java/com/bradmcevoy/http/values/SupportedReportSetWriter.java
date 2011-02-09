package com.bradmcevoy.http.values;

import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import java.util.Map;

/**
 *
 * @author alex
 */
public class SupportedReportSetWriter  implements ValueWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return SupportedReportSetList.class.isAssignableFrom( c );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        
      
        SupportedReportSetList list = (SupportedReportSetList) val;
        Element reportSet = writer.begin( "supported-report-set" ).open();
        if( list != null ) {
            for( String s : list) {
                Element supportedReport = writer.begin( "supported-report" ).open();
                Element report = writer.begin( "report" ).open();
                writer.writeProperty( s );
                report.close();
                supportedReport.close();
            }
        }
        reportSet.close();
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
