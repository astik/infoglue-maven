package com.bradmcevoy.http.values;

import com.bradmcevoy.http.XmlWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * Default list of value writers. These are used to format strongly types
 * property values (eg Date, Boolean, Locks) into the appropriate XML
 *
 * They also parse string values in PROPPATCH requests into the strongly
 * typed values.
 *
 * @author brad
 */
public class ValueWriters {

    private final List<ValueWriter> writers;

    /**
     * Allows the set of value writers to be injected
     *
     * @param valueWriters
     */
    public ValueWriters(List<ValueWriter> valueWriters) {
        this.writers = valueWriters;
    }

    /**
     * Initialised the default set of writers
     *
     */
    public ValueWriters() {
        writers = new ArrayList<ValueWriter>();
        writers.add(new LockTokenValueWriter());
        writers.add(new SupportedLockValueWriter());
        writers.add(new ModifiedDateValueWriter());
        writers.add(new DateValueWriter());
        writers.add(new ResourceTypeValueWriter());
        writers.add(new BooleanValueWriter());
        writers.add(new CDataValueWriter());
        writers.add(new CDataValueWriter());
        writers.add(new UUIDValueWriter());
        writers.add(new HrefListValueWriter());
        writers.add(new WrappedHrefWriter());
        writers.add(new SupportedReportSetWriter()); 
        writers.add(new ToStringValueWriter());
    }

    /**
     * Find the first value writer which supports the given property and use it
     * to output the XML.
     *
     * @param writer
     * @param qname
     * @param prefix
     * @param vat
     * @param href
     * @param nsPrefixes
     */
    public void writeValue(XmlWriter writer, QName qname, String prefix, ValueAndType vat, String href, Map<String, String> nsPrefixes) {
        for (ValueWriter vw : writers) {
            if (vw.supports(qname.getNamespaceURI(), qname.getLocalPart(), vat.getType())) {
                vw.writeValue(writer, qname.getNamespaceURI(), prefix, qname.getLocalPart(), vat.getValue(), href, nsPrefixes);
                break;
            }
        }
    }

    public List<ValueWriter> getValueWriters() {
        return writers;
    }

    /**
     * Find the first ValueWriter which supports the given property and use it
     * to parse the value
     *
     * @param qname
     * @param valueType
     * @param value
     * @return
     */
    public Object parse(QName qname, Class valueType, String value) {
        for (ValueWriter vw : writers) {
            if (vw.supports(qname.getNamespaceURI(), qname.getLocalPart(), valueType)) {
                return vw.parse(qname.getNamespaceURI(), qname.getLocalPart(), value);
            }
        }
        return null;
    }
}
