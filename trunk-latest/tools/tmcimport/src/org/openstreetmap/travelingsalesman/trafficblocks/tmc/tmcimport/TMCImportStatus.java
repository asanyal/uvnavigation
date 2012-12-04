package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

/**
 * Helper-class to store the status of the currently
 * running import permanently.
 */
public class TMCImportStatus {

    /**
     * This is where we store our status in memory.
     */
    private Properties myStatus = null;

    /**
     * The directory where to store {@link #myStatus} permanently.
     */
    private File myDirectory;

    /**
     * Load our properties and cache them.
     * @return see {@link #myStatus}
     * @throws IOException if we cannot load our status.
     */
    private Properties getStatus() throws IOException {
        if (myStatus == null) {
            myStatus = new Properties();
            File file = new File(myDirectory, "tmcimport.status.properties");
            if (file.exists()) {
                myStatus.load(new FileReader(file));
            }
        }
        return myStatus;
    }

    /**
     * Get the type of one of the OSM-entities representing this TMC-entity.
     * @param aClass 'L'ine, 'P'oint or 'A'rea
     * @param aLocationCode the TMC-entity
     * @return the entity-type
     * @throws IOException if we cannot load our status.
     */
    public EntityType getImportedEntityType(final char aClass, final int aLocationCode) throws IOException {
        int parsed = Integer.parseInt(getStatus().getProperty("osmelementtype." + aClass + "." + aLocationCode, "-1"));
        if (parsed == EntityType.Node.ordinal()) {
            return EntityType.Node;
        }
        if (parsed == EntityType.Way.ordinal()) {
            return EntityType.Way;
        }
        if (parsed == EntityType.Relation.ordinal()) {
            return EntityType.Relation;
        }
        return null;
    }

    /**
     * Get the ID of one of the OSM-entities representing this TMC-entity.
     * @param aClass 'L'ine, 'P'oint or 'A'rea
     * @param aLocationCode the TMC-entity
     * @return the ID
     * @throws IOException if we cannot load our status.
     */
    public long[] getImportedEntityId(final char aClass, final int aLocationCode) throws IOException {
        return new long[] {
                Long.parseLong(getStatus().getProperty("osmelementid.forward." + aClass + "." + aLocationCode, "-1")),
                Long.parseLong(getStatus().getProperty("osmelementid.reverse." + aClass + "." + aLocationCode, "-1"))
        };
    }

    /**
     * Has this TMC-entity been imported yet?
     * @param aClass 'L'ine, 'P'oint or 'A'rea
     * @param aLocationCode the TMC-entity
     * @return true if imported already
     * @throws IOException if we cannot load our status.
     */
    public boolean isImported(final char aClass, final int aLocationCode) throws IOException {
        return getStatus().getProperty("isimported." + aClass + "." + aLocationCode, "false").equals("true");
    }

    /**
     * Mark the given TMC-entity as having been imported.
     * @param aClass 'L'ine, 'P'oint or 'A'rea
     * @param aLocationCode the TMC-entity
     * @param aOSMentityForwardID  the id (forward-direction) of the one of the OSM-entities representing this TMC-entity
     * @param aOSMentityReverseID  the id (reverse-direction) of the one of the OSM-entities representing this TMC-entity
     * @param aOSMEntityType the type (orginal value of the ENTITYTYPE-enum) of the one of the OSM-entities representing this TMC-entity
     * @throws IOException if we cannot store the status
     */
    public void setImported(final char aClass, final int aLocationCode, final long aOSMentityForwardID, final long aOSMentityReverseID, final EntityType aOSMEntityType) throws IOException {
        Properties status = getStatus();
        status.put("osmelementid.forward." + aClass + "." + aLocationCode, "" + aOSMentityForwardID);
        status.put("osmelementid.reverse." + aClass + "." + aLocationCode, "" + aOSMentityReverseID);
        status.put("osmelementtype." + aClass + "." + aLocationCode, "" + aOSMEntityType.ordinal());
        status.put("isimported." + aClass + "." + aLocationCode, "true");
        File temp = File.createTempFile("tmcimport_status", ".properties");
        myStatus.store(new FileWriter(temp), "");

        File file = new File(myDirectory, "tmcimport.status.properties");
        if (file.exists()) {
            file.delete();
        }
        temp.renameTo(file);
    }

    /**
     * @param aDirectory the directory where the import happens
     */
    public TMCImportStatus(final File aDirectory) {
        super();
        myDirectory = aDirectory;
    }
}
