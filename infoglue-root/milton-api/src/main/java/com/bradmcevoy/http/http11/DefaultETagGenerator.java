package com.bradmcevoy.http.http11;

import com.bradmcevoy.http.Resource;
import java.util.Date;

/**
 * Generates the ETag as follows:
 *
 * - if the resource has a null unique id, returns null
 * - if the resource has a modified date it's hashcode is appended to the unique id
 * - the result is returned
 *
 * @author brad
 */
public class DefaultETagGenerator implements ETagGenerator{

    public String generateEtag( Resource r ) {
        String s = r.getUniqueId();
        if( s == null ) return null;
        Date dt = r.getModifiedDate();
        if( dt != null ) {
            s = s + "_" + dt.hashCode();
        }
        return s;
    }

}
