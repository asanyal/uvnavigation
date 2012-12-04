package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;

/**
 * Base-class for our object-model.
 */
public abstract class TMCEntity {

    /**
     * @see #addUserProperty(String, Object)
     */
    private Map<String, Object> myUserProperties = new HashMap<String, Object>();
    private TMCLocationTable myLocationTable;

    /**
     * @return Returns the locationTable.
     * @see #myLocationTable
     */
    public TMCLocationTable getLocationTable() {
        return this.myLocationTable;
    }
    public TMCEntity(final TMCLocationTable aLocationTable) {
        this.myLocationTable = aLocationTable;
    }
    /**
     * Associate any object with this entity.<br/>
     * This is used to simplify imports
     * @param aKey key to retrieve it later
     * @param aValue associated value
     */
    public void addUserProperty(final String aKey, final Object aValue) {
        this.myUserProperties.put(aKey, aValue);
    }
    /**
     * Get any associated object of this entity.<br/>
     * This is used to simplify imports
     * @param aKey key to retrieve it later
     * @return the associated value
     */
    public Object getUserProperty(final String aKey) {
        return this.myUserProperties.get(aKey);
    }
    /**
     * Generate osm-entities as a reference for comaprison in an editor
     * like JOSM. These are NOT to be uploaded.
     * @param aOutData where to write to
     * @return the generated entity
     */
    public abstract Entity generateReferenceOSM(final MemoryDataSet aOutData);
}
