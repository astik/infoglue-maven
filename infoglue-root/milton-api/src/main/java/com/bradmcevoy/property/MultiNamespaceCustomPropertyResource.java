package com.bradmcevoy.property;

import com.bradmcevoy.property.PropertySource.PropertyMetaData;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * A resource interface similar to CustomPropertyResource, except that it doesnt
 * use accessor objects, and it supports multiple namespaces.
 *
 * 
 *
 * @author brad
 */
public interface MultiNamespaceCustomPropertyResource {
    Object getProperty( QName name );

    void setProperty( QName name, Object value );

    PropertyMetaData getPropertyMetaData( QName name );

    List<QName> getAllPropertyNames();
}
