package org.nrg.xnat.services.cache;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultItemICache implements ItemICache {
    private static final String CACHE_NAME = "itemICache";

    private final Map<String, String>                _dataTypeTables   = new HashMap<>();
    private final Map<String, GenericWrapperElement> _dataTypeElements = new HashMap<>();

    private final JCacheHelper               _cacheHelper;
    private final NamedParameterJdbcTemplate _template;

    @Autowired
    public DefaultItemICache(final JCacheHelper cacheHelper, final NamedParameterJdbcTemplate template) {
        _cacheHelper = cacheHelper;
        _template    = template;
    }

    @Override
    public String getCacheName() {
        return CACHE_NAME;
    }

    @Override
    public Optional<ItemI> get(String xsiType, String itemId) {
        return Optional.ofNullable(getItem(xsiType, itemId));
    }

    @Override
    public List<ItemI> getAll(String xsiType) {
        final String tableName = getTableName(xsiType);
        List<String> ids       = _template.queryForList("SELECT id FROM " + tableName, EmptySqlParameterSource.INSTANCE, String.class);
        log.debug("Found {} IDs for existing objects of type {}", ids.size(), xsiType);

        return ids.stream().map(id -> getItem(xsiType, id)).collect(Collectors.toList());
    }

    @Override
    public void clear(String xsiType, String itemId) {
        getCache().remove(getCacheId(xsiType, itemId));
    }

    protected ItemI getItem(final String xsiType, final String itemId) {
        String cacheId = getCacheId(xsiType, itemId);
        ItemI  item    = getCache().get(cacheId);
        if (item != null) {
            return item;
        }
        try {
            XFTItem found = XFTItem.SelectItemByIds(getElement(xsiType), new String[] {itemId}, null, true, true);
            if (found == null) {
                throw new NotFoundException(xsiType, itemId);
            }
            getCache().put(cacheId, found);
            return found;
        } catch (Exception e) {
            throw new NrgServiceRuntimeException("An error occurred trying to retrieve the item of type " + xsiType + " with ID " + itemId, e);
        }

    }

    protected Cache<String, ItemI> getCache() {
        return _cacheHelper.getCache(CACHE_NAME, String.class, ItemI.class);
    }

    protected static String getCacheId(final String xsiType, final String itemId) {
        return String.join(":", xsiType, itemId);
    }

    private String getTableName(String xsiType) {
        return _dataTypeTables.computeIfAbsent(xsiType, (type) -> getElement(type).getSQLName());
    }

    private GenericWrapperElement getElement(String xsiType) {
        return _dataTypeElements.computeIfAbsent(xsiType, (type) -> {
            try {
                GenericWrapperElement element = GenericWrapperElement.GetElement(type);
                if (element == null) {
                    throw new NrgServiceRuntimeException("The data-type element for XSI type " + type + " doesn't seem to exist on this system");
                }
                return element;
            } catch (XFTInitException | ElementNotFoundException e) {
                throw new NrgServiceRuntimeException("An error occurred trying to retrieve the data-type element for XSI type " + type, e);
            }
        });
    }
}
