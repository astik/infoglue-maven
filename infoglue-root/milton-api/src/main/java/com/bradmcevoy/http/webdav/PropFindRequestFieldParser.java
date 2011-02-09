package com.bradmcevoy.http.webdav;

import java.io.InputStream;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Parses the body of a PROPFIND request and returns the requested fields
 *
 * @author brad
 */
public interface PropFindRequestFieldParser {

    ParseResult getRequestedFields( InputStream in );

    public class ParseResult {
        private final boolean allProp;
        private final Set<QName> names;

        public ParseResult( boolean isAllProp, Set<QName> names ) {
            this.allProp = isAllProp;
            this.names = names;
        }

        public boolean isAllProp() {
            return allProp;
        }

        public Set<QName> getNames() {
            return names;
        }
    }
}
