/*
 * core: org.nrg.xft.presentation.FlattenedItem
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.presentation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.loaders.XFTItemDBLoader.ItemCache;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperFactory;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperField;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.nrg.xft.compare.ItemComparator.DEFAULT_ITEM_COMPARATOR;

@Slf4j
public class FlattenedItem extends FlattenedItemA {
    public FlattenedItem(XFTItem item, FlattenedItemA.HistoryConfigI includeHistory, Callable<Integer> idGenerator, FlattenedItemA.FilterI filter, String refName, List<FlattenedItemA.ItemObject> parents, List<? extends FlattenedItemModifierI> injectors, ItemCache cache) throws Exception {
        this(item, includeHistory, idGenerator, filter, convertElement(item.getGenericSchemaElement()), refName, parents, injectors, cache);
    }

    @SuppressWarnings("unchecked")
    public FlattenedItem(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory, Callable<Integer> idGenerator, FlattenedItemA.FilterI filter, XMLWrapperElement root, String refName, List<FlattenedItemA.ItemObject> parents, List<? extends FlattenedItemModifierI> injectors, ItemCache cache) throws Exception {
        super(FlattenedItem.getFlattenedSingleParams(i), i.isHistory(), i.getRowLastModified(), i.getInsertDate(), i.getStatus(), idGenerator.call(), root.getXSIType(), parents);

        if (isHistory) {
            setChange_date(FlattenedItemA.parseDate(i.getProps().get("change_date")));
            if (i.getMeta() != null) {
                create_username = Users.getUsername(i.getMeta().getProperty("insert_user_xdat_user_id"));
            }
            modified_username = Users.getUsername(i.getProps().get("change_user"));
            create_event_id = translateNumber(i.getProps().get("xft_version"));
            if (isDeleted()) {
                modified_event_id = translateNumber(i.getXFTVersion());
            }
            setPrevious_change_date(FlattenedItemA.parseDate(i.getProps().get("previous_change_date")));
        } else {
            create_event_id = translateNumber(i.getXFTVersion());
            if (i.getMeta() != null) {
                create_username = Users.getUsername(i.getMeta().getProperty("insert_user_xdat_user_id"));
            }
        }

        pks = root.getPkNames();
        displayIdentifiers = root.getDisplayIdentifiers();

        this.o = new FlattenedItemA.ItemObject((refName == null) ? root.getProperName() : refName, this.getLabel(), getPKString(), root.getExtendedXSITypes());
        FlattenedItem.renderChildren(i, root, this, root.getChildren(), includeHistory, idGenerator, filter, injectors, cache);

        if (injectors != null && !injectors.isEmpty()) {
            for (final FlattenedItemModifierI injector : injectors) {
                injector.modify(i, includeHistory, idGenerator, filter, root, parents, this);
            }
        }
    }

    public Number translateNumber(final Object object) {
        return object == null || object instanceof Number ? (Number) object : Integer.valueOf(object.toString());
    }

    public FlattenedItem(FlattenedItem fi) throws Exception {
        super(fi);
    }

    @SuppressWarnings("unchecked")
    static void renderChildren(XFTItem item, final XMLWrapperElement root, FlattenedItem parent, final List<XMLWrapperField> fields, FlattenedItemA.HistoryConfigI includeHistory, Callable<Integer> idGenerator, FlattenedItemA.FilterI filter, List<? extends FlattenedItemModifierI> injectors, ItemCache cache) throws Exception {
        for (final XMLWrapperField field : fields) {
            if (field.isReference() && root.getExtensionFieldName().equalsIgnoreCase(field.getName())) {
                final XFTItem extension = (XFTItem) item.getProperty(field.getId());
                if (extension != null) {
                    final XMLWrapperElement ext = convertElement(extension.getGenericSchemaElement());
                    renderChildren(extension, ext, parent, ext.getChildren(), includeHistory, idGenerator, filter, injectors, cache);
                }
            } else if (field.isReference()) {
                if (field.getExpose()) {
                    final List<XFTItem> refs = item.getChildItems(field, includeHistory.getIncludeHistory(), cache);
                    if (!refs.isEmpty()) {
                        refs.sort(DEFAULT_ITEM_COMPARATOR);
                        final List<FlattenedItemA.ItemObject> parents = new ArrayList<>(parent.getParents().size() + 1);
                        if (parent.getParents() != null) {
                            parents.addAll(parent.getParents());
                        }
                        parents.add(parent.getItemObject());
                        parent.addChildCollection(field.getXMLPathString(), field.getDisplayName(), buildLayeredProps(refs, includeHistory, idGenerator, filter, StringUtils.getIfBlank(field.getDisplayName(), field::getName), parents, injectors, cache));
                    }
                }
            } else {
                renderChildren(item, root, parent, field.getChildren(), includeHistory, idGenerator, filter, injectors, cache);
            }
        }
    }

    @SuppressWarnings("unchecked")
    static FlattenedItemA.FieldTracker getFlattenedSingleParams(XFTItem item, XMLWrapperField field) throws Exception {
        FlattenedItemA.FieldTracker params = new FlattenedItemA.FieldTracker();

        parseAttributes(item, field.getAttributes(), params);

        handleChildFields(field.getChildren(), item, params);

        if (field.isReference()) {
            if (!field.isMultiple()) {
                XFTItem ref = (XFTItem) item.getProperty(field.getId());
                if (ref != null && item.getGenericSchemaElement().getExtensionFieldName().equalsIgnoreCase(field.getName())) {
                    params.putAll(getFlattenedSingleParams(ref));
                }
            }
        } else {
            FlattenedItemA.putValue(field.getXMLPathString(), field.getDisplayName(), parseProperty(field, item.getProperty(field.getId())), params);
        }

        return params;
    }

    static void handleChildFields(List<XMLWrapperField> fields, XFTItem item, FlattenedItemA.FieldTracker params) throws Exception {
        for (XMLWrapperField field : fields) {
            params.putAll(FlattenedItem.getFlattenedSingleParams(item, field));
        }
    }

    @SuppressWarnings("unchecked")
    static FlattenedItemA.FieldTracker getFlattenedSingleParams(XFTItem item) throws Exception {
        XMLWrapperElement           root   = convertElement(item.getGenericSchemaElement());
        FlattenedItemA.FieldTracker params = new FlattenedItemA.FieldTracker();

        parseAttributes(item, (List<XMLWrapperField>) root.getAttributes(), params);

        FlattenedItem.handleChildFields(root.getChildren(), item, params);

        for (GenericWrapperField field : root.getAllPrimaryKeys()) {
            if (!params.getParams().containsKey(field.getXMLPathString())) {
                FlattenedItemA.putValue(field.getXMLPathString(), field.getName(), parseProperty(field, item.getProperty(field.getId())), params);
            }
        }

        return params;
    }

    static void parseAttributes(XFTItem item, List<XMLWrapperField> fields, FlattenedItemA.FieldTracker params) throws Exception {
        for (XMLWrapperField field : fields) {
            if (field.isReference()) {
                if (!field.isMultiple()) {
                    XFTItem ref = (XFTItem) item.getField(field.getId());
                    if (ref != null) {
                        params.putAll(FlattenedItem.getFlattenedSingleParams(ref));
                    }
                }
            } else {
                FlattenedItemA.putValue(field.getXMLPathString(), field.getDisplayName(), parseProperty(field, item.getField(field.getId())), params);
            }
        }
    }

    private static Object parseProperty(final GenericWrapperField field, final Object object) {
        if (field != null && field.getXMLType() != null && field.getXMLType().getLocalType() != null && field.getXMLType().getLocalType().equals("boolean") && object != null) {
            if (object.toString().equals("0")) {
                return "false";
            }
            if (object.toString().equals("1")) {
                return "true";
            }
        }
        return object;
    }


    public static XMLWrapperElement convertElement(final GenericWrapperElement element) throws XFTInitException, ElementNotFoundException {
        return (XMLWrapperElement) XMLWrapperFactory.GetInstance().convertElement(element.getFullXMLName().endsWith("_history") ? GenericWrapperElement.GetElement(element.getFullXMLName().substring(0, element.getFullXMLName().indexOf("_history"))) : element);
    }

    static List<FlattenedItemI> buildLayeredProps(final List<XFTItem> items, final FlattenedItemA.HistoryConfigI includeHistory, final Callable<Integer> idGenerator, final @Nullable FlattenedItemA.FilterI filter, final String refName, final List<FlattenedItemA.ItemObject> parents, final List<? extends FlattenedItemModifierI> files, final ItemCache cache) {
        final FlattenedItemA.FilterI resolved = Optional.ofNullable(filter).orElseGet(() -> i -> true);
        return items.stream()
                    .map(item -> FlattenedItem.create(item, Optional.ofNullable(includeHistory).orElseGet(() -> FlattenedItemA.GET_ALL), idGenerator, resolved, refName, parents, files, cache))
                    .filter(Objects::nonNull)
                    .filter(resolved::accept)
                    .collect(Collectors.toList());
    }

    private static FlattenedItem create(final XFTItem item, final HistoryConfigI includeHistory, final Callable<Integer> idGenerator, final FilterI filter, final String refName, final List<ItemObject> parents, final List<? extends FlattenedItemModifierI> injectors, final ItemCache cache) {
        try {
            return new FlattenedItem(item, includeHistory, idGenerator, filter, refName, parents, injectors, cache);
        } catch (Exception e) {
            log.error("An error occurred while creating a new FlattenedItem instance. This is the exception-safe create() method, so I'm just logging this error and returning null.", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public FlattenedItem filteredCopy(final FlattenedItemA.FilterI fi) throws Exception {
        FlattenedItem temp = new FlattenedItem(this);

        for (Map.Entry<String, FlattenedItemA.ChildCollection> cc : this.getChildCollections().entrySet()) {
            FlattenedItemA.ChildCollection tempCC = cc.getValue().copy(fi);
            if (!tempCC.getChildren().isEmpty()) {
                temp.getChildCollections().put(cc.getKey(), tempCC);
            }
        }

        temp.getParents().addAll(this.getParents());

        return temp;
    }

    public static String writeValuesToString(final List<String> values, final FlattenedItemI item) {
        return values.stream().map(value -> item.getFields().getParams().get(value)).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(", "));
    }

    public Object getPKString() {
        final List<String> primaryKeys = getPks();
        if (primaryKeys.isEmpty()) {
            return id;
        }
        final String primaryKey = writeValuesToString(primaryKeys, this);
        return StringUtils.isNotBlank(primaryKey) ? primaryKey : id;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.presentation.FlattenedItemI#getLabel()
     */
    public Object getLabel() {
        if (displayIdentifiers != null && !displayIdentifiers.isEmpty()) {
            final String primaryKey = writeValuesToString(displayIdentifiers, this);
            if (StringUtils.isNotBlank(primaryKey)) {
                return primaryKey;
            }
        }
        return getPKString();
    }

    public interface FlattenedItemModifierI {
        void modify(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory, Callable<Integer> idGenerator, FlattenedItemA.FilterI filter, XMLWrapperElement root, List<FlattenedItemA.ItemObject> parents, FlattenedItemI fi);
    }

    public static class FlattenedFile extends FlattenedItemA {

        public FlattenedFile(FlattenedItemA.FieldTracker ft, boolean isHistory,
                             Date last_modified, Date insert_date,
                             Integer id, String xsiType, String modifiedBy, Integer modifiedEventId, Integer createEventId, String label, List<FlattenedItemA.ItemObject> parents, String createdBy) {
            super(ft, isHistory, last_modified, insert_date, (isHistory) ? FlattenedItemA.DELETED : "active", id, xsiType, parents);

            if (isHistory) {
                setChange_date(last_modified);
                modified_username = modifiedBy;
                setPrevious_change_date(insert_date);
            }
            modified_event_id = modifiedEventId;
            create_event_id = createEventId;
            create_username = createdBy;

            String lbl = label.intern();
            FlattenedItemA.putValue("uri", "uri", lbl, ft);

            pks = Collections.singletonList("uri");
            displayIdentifiers = Collections.singletonList("uri");

            o = new FlattenedItemA.ItemObject("file", lbl, lbl, Collections.singletonList("system:file"));
        }

        public FlattenedFile(FlattenedFile fi) throws Exception {
            super(fi);
        }

        public FlattenedFile filteredCopy(final FlattenedItemA.FilterI filter) throws Exception {
            final FlattenedFile temp = new FlattenedFile(this);
            for (final Map.Entry<String, FlattenedItemA.ChildCollection> cc : getChildCollections().entrySet()) {
                final FlattenedItemA.ChildCollection tempCC = cc.getValue().copy(filter);
                if (!tempCC.getChildren().isEmpty()) {
                    temp.getChildCollections().put(cc.getKey(), tempCC);
                }
            }
            temp.modified_event_id = this.modified_event_id;
            temp.create_event_id = this.create_event_id;
            temp.getParents().addAll(this.getParents());
            return temp;
        }

        public Number getCreateEventId() {
            return ObjectUtils.getIfNull(getCreate_event_id(), () -> getStartDate() == null ? null : getStartDate().getTime());
        }

        public Number getModifiedEventId() {
            return ObjectUtils.getIfNull(getModified_event_id(), () -> getEndDate() == null ? null : getEndDate().getTime());
        }
    }

    public static class FlattenedFileSummary extends FlattenedItemA {
        public FlattenedFileSummary(FlattenedItemA.FieldTracker ft, boolean isHistory,
                                    Date last_modified, Date insert_date,
                                    Integer id, String xsiType, String modifiedBy, Integer modifiedEventId, Integer createEventId, String label, List<FlattenedItemA.ItemObject> parents, String createdBy, Integer count, String action) {
            super(ft, isHistory, last_modified, insert_date, (isHistory) ? FlattenedItemA.DELETED : "active", id, xsiType, parents);

            if (isHistory) {
                setChange_date(last_modified);
                modified_username = modifiedBy;
                setPrevious_change_date(insert_date);
            }
            modified_event_id = modifiedEventId;
            create_event_id = createEventId;
            create_username = createdBy;

            FlattenedItemA.putValue("count", "count", count, ft);
            FlattenedItemA.putValue("action", "action", action, ft);
            FlattenedItemA.putValue("label", "label", label, ft);

            pks = Collections.singletonList("label");
            displayIdentifiers = Collections.singletonList("label");

            o = new FlattenedItemA.ItemObject("file:summary", label, label, Collections.singletonList("file:summary"));
        }

        public FlattenedFileSummary(FlattenedFileSummary fi) throws Exception {
            super(fi);
        }

        public FlattenedFileSummary filteredCopy(FlattenedItemA.FilterI fi) throws Exception {
            FlattenedFileSummary temp = new FlattenedFileSummary(this);

            for (Map.Entry<String, FlattenedItemA.ChildCollection> cc : this.getChildCollections().entrySet()) {
                FlattenedItemA.ChildCollection tempCC = cc.getValue().copy(fi);
                if (!tempCC.getChildren().isEmpty()) {
                    temp.getChildCollections().put(cc.getKey(), tempCC);
                }
            }

            temp.modified_event_id = this.modified_event_id;
            temp.create_event_id = this.create_event_id;

            temp.getParents().addAll(this.getParents());

            return temp;
        }
    }
}
