package com.bradmcevoy.property;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Defines a source of properties. This is used by both propfind and proppatch
 * 
 *
 * @author brad
 */
public interface PropertySource {

    public enum PropertyAccessibility {

        UNKNOWN,
        READ_ONLY,
        WRITABLE
    }

    public static class PropertyMetaData {

        private final PropertyAccessibility accessibility;
        private final Class valueType;
        public static final PropertyMetaData UNKNOWN = new PropertyMetaData( PropertyAccessibility.UNKNOWN, null );

        public PropertyMetaData( PropertyAccessibility accessibility, Class valueType ) {
            this.accessibility = accessibility;
            this.valueType = valueType;
        }

        public PropertyAccessibility getAccessibility() {
            return accessibility;
        }

        public Class getValueType() {
            return valueType;
        }

        public boolean isUnknown() {
            return accessibility.equals( PropertyAccessibility.UNKNOWN );
        }

        public boolean isWritable() {
            return accessibility.equals( PropertyAccessibility.WRITABLE );
        }
    }


    Object getProperty( QName name, Resource r ) throws NotAuthorizedException;

    void setProperty( QName name, Object value, Resource r ) throws PropertySetException, NotAuthorizedException;

    /**
     * Check to see if the property is known, and if it is writable.
     *
     * The returned value also contains a class which is the most specific known
     * class of the values which can be contained in this property. This class
     * must be sufficient to locate a ValueWriter to parse the textual representation
     * sent in PROPPATCH requests.
     *
     * @param name - the qualified name of the property
     * @param r - the resource which might contain the property
     * @return - never null, contains an enum value indicating if the property is known
     * to this source, and if it is writable, and a class indicating the type of the property.
     */
    PropertyMetaData getPropertyMetaData( QName name, Resource r );

    /**
     * Remove the given property. There may be a semantic difference in some
     * cases between setting a property to a null value vs removing the property.
     * Generally this should completely the remove the property if possible.
     *
     * @param name
     * @param r
     */
    void clearProperty( QName name, Resource r ) throws PropertySetException, NotAuthorizedException;

    /**
     *
     * @param r - the resource which may contain properties
     * 
     * @return - all properties known by this source on the given resource.
     * This list should be exclusive. Ie only return properties not returned
     * by any other source
     */
    List<QName> getAllPropertyNames( Resource r );

    /**
     * Exception from setting a field
     */
    public class PropertySetException extends Exception {
        private static final long serialVersionUID = 1L;
        private Response.Status status;
        private QName field;
        private String notes;

        public PropertySetException(Status status, QName field, String notes) {
            this.status = status;
            this.field = field;
            this.notes = notes;
        }

        /**
         * A response code indicating the problem. Eg 500 for a processing error,
         * or conflict, forbidden, etc
         *
         * @return
         */
        public Status getStatus() {
            return status;
        }

        public QName getField() {
            return field;
        }

        /**
         * Should contain a human readable description of the problem
         *
         * @return
         */
        public String getErrorNotes() {
            return notes;
        }
    }
}
