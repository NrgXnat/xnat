/*
 * core: org.nrg.xft.schema.XFTDataField
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.design.XFTNode;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xft.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XFTDataField extends XFTField {
    private static Logger logger  = LoggerFactory.getLogger(XFTDataField.class);
    private        String  _name  = "";
    private        XMLType _type  = null;

    private XFTDataField() {
        //org.nrg.xft.logger.debug("Creating Data Field");
    }

    /**
     * Used to construct and populate a Data Field from the supplied node.
     *
     * @param node   corresponding XML Node
     * @param header header used for full name specification
     * @param prefix of schema XMLNS
     * @param s      parent schema
     */
    public XFTDataField(Node node, String header, String prefix, XFTSchema s, XFTNode parent) {
        //this.setParent(parent);
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            _name = NodeUtils.GetAttributeValue(nnm, "name", "").intern();
            if (!NodeUtils.GetAttributeValue(nnm, "type", "").equalsIgnoreCase("")) {
                _type = new XMLType(NodeUtils.GetAttributeValue(nnm, "type", "").intern(), s);
            }
            setUse(NodeUtils.GetAttributeValue(nnm, "use", "").intern());
            setFixed(NodeUtils.GetAttributeValue(nnm, "fixed", "").intern());
            this.setMaxOccurs(NodeUtils.GetAttributeValue(nnm, "maxOccurs", "").intern());
            this.setMinOccurs(NodeUtils.GetAttributeValue(nnm, "minOccurs", "").intern());

            String defaultValue = NodeUtils.GetAttributeValue(nnm, "default", "").intern();
            if (!defaultValue.equalsIgnoreCase("")) {
                this.getSqlField().setDefaultValue(defaultValue);
            }
        }

        logger.debug("Creating Data Field: '" + _name + "'");

        setProperties(node, prefix, s);

        this.setParent(parent);
        this.setChildFields(XFTField.GetFields(node, header + "_" + _name, this, prefix, s));
        validateSQLName();
    }

    public void setProperties(Node node, String prefix, XFTSchema s) {
        Node xftField       = NodeUtils.GetLevel3Child(node, XFTElement.XML_TAG_PREFIX + ":field");
        Node xftSqlField    = NodeUtils.GetLevel4Child(node, XFTElement.XML_TAG_PREFIX + ":sqlField");
        Node xftTorqueField = NodeUtils.GetLevel4Child(node, XFTElement.XML_TAG_PREFIX + ":torqueField");
        Node xftRelation    = NodeUtils.GetLevel4Child(node, XFTElement.XML_TAG_PREFIX + ":relation");

        Node xftDescription = NodeUtils.GetLevel2Child(node, prefix + ":documentation");
        if (xftDescription != null) {
            if (xftDescription.hasChildNodes()) {
                this.setDescription(xftDescription.getChildNodes().item(0).getNodeValue());
            }
        }

        if (xftField != null) {
            NamedNodeMap columnNNM = xftField.getAttributes();
            this.setDisplayName(NodeUtils.GetAttributeValue(columnNNM, "displayName", ""));
            this.setExpose(NodeUtils.GetAttributeValue(columnNNM, "expose", ""));
            this.setRequired(NodeUtils.GetAttributeValue(columnNNM, "required", ""));
            this.setSize(NodeUtils.GetAttributeValue(columnNNM, "size", ""));
            this.setUnique(NodeUtils.GetAttributeValue(columnNNM, "unique", ""));
            this.setUniqueComposite(NodeUtils.GetAttributeValue(columnNNM, "uniqueComposite", ""));
            this.setXmlOnly(NodeUtils.GetAttributeValue(columnNNM, "xmlOnly", ""));
            this.setXmlDisplay(NodeUtils.GetAttributeValue(columnNNM, "xmlDisplay", "always"));

            this.setBaseElement(NodeUtils.GetAttributeValue(columnNNM, "baseElement", ""));
            this.setBaseCol(NodeUtils.GetAttributeValue(columnNNM, "baseCol", ""));
            this.setLocalMap(NodeUtils.GetBooleanAttributeValue(xftField, "local_map", false));

            this.setFilter(NodeUtils.GetBooleanAttributeValue(xftField, "filter", false));
        }

        XFTRule xr = new XFTRule(node, prefix, s);
        this.setRule(xr);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(xr.getBaseType()) && (this.getXMLType() == null)) {
            this.setXMLType(new XMLType(xr.getBaseType(), s));
        }

        if (org.apache.commons.lang3.StringUtils.isNotBlank(xr.getMaxLength()) && org.apache.commons.lang3.StringUtils.isBlank(getSize())) {
            this.setSize(xr.getMaxLength());
        }

        if (org.apache.commons.lang3.StringUtils.isNotBlank(xr.getLength()) && org.apache.commons.lang3.StringUtils.isBlank(getSize())) {
            this.setSize(xr.getLength());
        }

        if (xftSqlField != null) {
            this.setSqlField(new XFTSqlField(xftSqlField));
        }

        if (xftTorqueField != null) {
            this.setWebAppField(new TorqueField(xftTorqueField));
        }

        if (xftRelation != null) {
            this.setRelation(new XFTRelation(xftRelation));
        }

        Node simpleTypeElement = NodeUtils.GetLevel1Child(node, prefix + ":simpleType");
        if (simpleTypeElement != null) {
            logger.debug("Loading simpleType element");
            if (simpleTypeElement.hasChildNodes()) {
                for (int i = 0; i < simpleTypeElement.getChildNodes().getLength(); i++) {
                    //level 1
                    Node child1 = simpleTypeElement.getChildNodes().item(i);
                    if (child1.getNodeName().equalsIgnoreCase(prefix + ":list")) {
                        setXMLType(s.getBasicDataType("string"));
                        setSize("250");
                    } else if (getXMLType() == null && (!hasChildren())) {
                        //UNKNOWN TYPE
                        setXMLType(s.getBasicDataType("string"));
                        setSize("250");
                    }
                }
            }
        }
    }

    /**
     * Validates the SQL name of the field.  If it is a protected DB value (i.e. 'user') it is changed here.
     * Performed during XFTDataField(with node) construction.
     */
    public void validateSQLName() {
        if (getName().equalsIgnoreCase("user") && getSQLName().equalsIgnoreCase("user")) {
            if (getSqlField() == null) {
                setSqlField(new XFTSqlField());
            }
            if (getSqlField().getSqlName().equals("")) {
                getSqlField().setSqlName("user_name");
            }
        }

        if (getName().equalsIgnoreCase("order") && getSQLName().equalsIgnoreCase("order")) {
            if (getSqlField() == null) {
                setSqlField(new XFTSqlField());
            }
            if (getSqlField().getSqlName().equals("")) {
                getSqlField().setSqlName("order1");
            }
        }

        if (getName().equalsIgnoreCase("group") && getSQLName().equalsIgnoreCase("group")) {
            if (getSqlField() == null) {
                setSqlField(new XFTSqlField());
            }

            if (getSqlField().getSqlName().equals("")) {
                getSqlField().setSqlName("_group");
            }
        }
    }

    /**
     * If this field has a specified sql name it is returned, Else an empty string is returned.
     *
     * @return The XML SQL name value.
     */
    public String getXMLSqlNameValue() {
        String _return = "";
        if (getSqlField() != null) {
            _return = getSqlField().getSqlName();
        }

        return _return;
    }

    /**
     * If this field has a specified sql name, it is returned (lower case).  Else, the full name of the field is returned.
     *
     * @return The SQL name.
     */
    public String getSQLName() {
        String temp = "";
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getXMLSqlNameValue())) {
            temp = this.getXMLSqlNameValue().toLowerCase();
        }

        if (temp.equalsIgnoreCase("")) {
            temp = getFullName().toLowerCase();
        }

        if (temp.length() > 63) {
            temp = temp.substring(0, 63);
        }
        temp = XftStringUtils.CleanForSQL(temp);
        return temp;
    }

    /**
     * local xml name of the field.
     *
     * @return The name.
     */
    public String getName() {
        return _name;
    }

    /**
     * XMLType of the field (i.e. XMLType('xs:string'))
     *
     * @return The type.
     */
    public XMLType getXMLType() {
        return _type;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * @param type The XML type to set.
     */
    public void setXMLType(XMLType type) {
        _type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        Document doc = toXML();
        return XMLUtils.DOMToString(doc);
    }

    public Document toXML() {
        XMLWriter writer = new XMLWriter();
        Document  doc    = writer.getDocument();

        doc.appendChild(this.toXML(doc));

        return doc;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.schema.XFTField#toString(java.lang.String)
     */
    public String toString(String header) {
        final StringBuilder sb = new StringBuilder();
        sb.append(header).append("XFTDataField\n");
        sb.append(header).append(this.displayAttribute()).append("name:").append(this.getName()).append("\n").append(header).append(" FullName:").append(this.getFullName()).append("\n");
        sb.append(header).append("sequence:").append(this.getSequence()).append("\n");
        if (getXMLType() != null) {
            sb.append(header).append("type:").append(this.getXMLType().getFullLocalType()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getUse())) {
            sb.append(header).append("use:").append(this.getUse()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getMaxOccurs())) {
            sb.append(header).append("maxOccurs:").append(this.getMaxOccurs()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getMinOccurs())) {
            sb.append(header).append("minOccurs:").append(this.getMinOccurs()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getSize())) {
            sb.append(header).append("size:").append(this.getSize()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getDescription())) {
            sb.append(header).append("description:").append(this.getDescription()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getDisplayName())) {
            sb.append(header).append("displayName:").append(this.getDisplayName()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getFixed())) {
            sb.append(header).append("fixed:").append(this.getFixed()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getRequired())) {
            sb.append(header).append("required:").append(this.getRequired()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getExpose())) {
            sb.append(header).append("expose:").append(this.getExpose()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getUnique())) {
            sb.append(header).append("unique:").append(this.getUnique()).append("\n");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getUniqueComposite())) {
            sb.append(header).append("uniqueComposite:").append(this.getUniqueComposite()).append("\n");
        }

        if (getRule() != null) {
            sb.append("Rule:").append(this.getRule().toString(header + "\t"));
        }
        if (getSqlField() != null) {
            sb.append("SQLField:").append(this.getSqlField().toString(header + "\t"));
        }
        if (getWebAppField() != null) {
            sb.append("WebAppField:").append(this.getWebAppField().toString(header + "\t"));
        }
        if (getRelation() != null) {
            sb.append("Relation:").append(this.getRelation().toString(header + "\t"));
        }

        for (final Object o : this.getSortedFields()) {
            sb.append(((XFTField) o).toString(header + "\t"));
        }

        return sb.toString();
    }


    public Node toXML(Document doc) {
        Node main = doc.createElement("data-field");
        if (getXMLType() != null) {
            main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "type", this.getXMLType().getFullForeignType()));
        }
        main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "name", this.getName()));
        main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "full-name", this.getFullName()));

        Node props = doc.createElement("properties");
        main.appendChild(props);
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "use", getUse()));
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "maxOccurs", this.getMaxOccurs()));
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "minOccurs", this.getMinOccurs()));
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "sequence", Integer.toString(this.getSequence())));
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "size", this.getSize()));
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "description", this.getDescription()));
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "displayName", this.getDisplayName()));
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "fixed", this.getFixed()));
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getRequired())) {
            props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "required", this.getRequired()));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getExpose())) {
            props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "expose", this.getExpose()));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getUnique())) {
            props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "unique", this.getUnique()));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(getUniqueComposite())) {
            props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "uniqueComposite", this.getUniqueComposite()));
        }
        props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "local-map", XftStringUtils.ToString(this.isLocalMap())));

        if (getRule() != null) {
            main.appendChild(this.getRule().toXML(doc));
        }
        if (getSqlField() != null) {
            main.appendChild(this.getSqlField().toXML(doc));
        }
        if (getWebAppField() != null) {
            main.appendChild(this.getWebAppField().toXML(doc));
        }

        if (getRelation() != null) {
            main.appendChild(this.getRelation().toXML(doc));
        }

        for (final Object o : this.getSortedFields()) {
            main.appendChild((((XFTField) o).toXML(doc)));
        }

        return main;
    }

    /**
     * Constructs a new empty primary key field.  AutoIncrement = true, PrimaryKey= true, XMLType = integer
     *
     * @param prefix XMLNS prefix. This is ignored.
     * @param schema Parent schema
     * @return An empty key data field.
     * @deprecated Use {@link #GetEmptyKey(XFTSchema)} instead.
     */
    @Deprecated
    public static XFTDataField GetEmptyKey(@SuppressWarnings("UnusedParameters") String prefix, XFTSchema schema) {
        return GetEmptyKey(schema);
    }

    /**
     * Constructs a new empty primary key field.  AutoIncrement = true, PrimaryKey= true, XMLType = integer
     *
     * @param schema Parent schema
     * @return An empty key data field.
     */
    public static XFTDataField GetEmptyKey(final XFTSchema schema) {
        XFTDataField field = new XFTDataField();
        XFTSqlField  sql   = new XFTSqlField();
        sql.setAutoIncrement("true");
        sql.setPrimaryKey("true");
        field.setSqlField(sql);
        field.setXMLType(schema.getBasicDataType("integer"));

        return field;
    }

    /**
     * Constructs an empty data field.
     *
     * @return An empty field.
     */
    public static XFTDataField GetEmptyField() {
        XFTDataField field = new XFTDataField();
        XFTSqlField  sql   = new XFTSqlField();
        field.setSqlField(sql);

        return field;
    }

    public XFTField clone(XFTElement e, boolean accessLayers) {
        XFTDataField clone = GetEmptyField();
        clone.setName(_name);
        clone.setFullName(getFullName());
        clone.setUse(getUse());
        clone.setXMLType(_type);
        clone.setMaxOccurs(getMaxOccurs());
        clone.setMinOccurs(getMinOccurs());
        clone.setFixed(getFixed());
        clone.setDescription(getDescription());

        clone.setDisplayName(getDisplayName());
        clone.setExpose(getExpose());
        clone.setRequired(getRequired());
        clone.setSize(getSize());
        clone.setUnique(getUnique());
        clone.setUniqueComposite(getUniqueComposite());
        clone.setXmlOnly(getXmlOnly());
        clone.setXmlDisplay(getXmlDisplay());

        clone.setBaseElement(getBaseElement());
        clone.setBaseCol(getBaseCol());

        XFTRule xr = getRule();
        clone.setRule(xr);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(xr.getBaseType()) && (clone.getXMLType() == null)) {
            clone.setXMLType(new XMLType(xr.getBaseType(), e.getSchema()));
        }

        if (org.apache.commons.lang3.StringUtils.isNotBlank(xr.getMaxLength()) && org.apache.commons.lang3.StringUtils.isBlank(clone.getSize())) {
            clone.setSize(xr.getMaxLength());
        }

        if (org.apache.commons.lang3.StringUtils.isNotBlank(xr.getLength()) && org.apache.commons.lang3.StringUtils.isBlank(clone.getSize())) {
            clone.setSize(xr.getLength());
        }

        if (getSqlField() != null) {
            clone.setSqlField(getSqlField().clone(e));
        }

        if (getWebAppField() != null) {
            clone.setWebAppField(getWebAppField().clone(e));
        }

        if (getRelation() != null) {
            clone.setRelation(getRelation().clone(e));
        }

        if (accessLayers) {
            int      counter = 2002;
            for (final Object o : getSortedFields()) {
                XFTField field = (XFTField) o;
                if (field.isLocalMap()) {
                    XFTReferenceField ref = XFTReferenceField.GetEmptyRef();
                    ref.setName(field.getName());
                    ref.setXMLType(e.getType());
                    ref.setFullName(field.getName());
                    ref.setMinOccurs("0");
                    ref.getRelation().setOnDelete("SET NULL");
                    ref.setParent(clone);
                    ref.setSequence(counter++);
                    ref.getSqlField().setPrimaryKey(field.getSqlField().getPrimaryKey());
                    clone.addChildField(ref);
                } else {
                    clone.addChildField(field.clone(e, true));
                }
            }
        }

        return clone;
    }

    public String getId() {
        return this.getFullName();
    }
}

