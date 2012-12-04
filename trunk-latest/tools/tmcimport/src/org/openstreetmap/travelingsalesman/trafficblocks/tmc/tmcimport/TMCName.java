package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;

/**
 * Data-model of a TMC location-name.<br/>
 * Used by {@link TMCLocationTable}.
 */
public final class TMCName extends TMCEntity {
    /**
     * Country-ID.
     */
    private int CID;
    /**
     * Location-Table-ID. (unique in a country)
     * @see #CID
     */
    private int TABCD;
    /**
     * Name-code. (unique in a location-table).
     */
    private int NID;
    private String NAME;
    private String NCOMMENT;
    /**
     * Load all elements of this type from the TMC LocationCodeList.
     * @param aDirectory the directioy where the location-list is stored.
     * @param aLocationTable the top-level class we are working for
     * @return all elements indexed by LocationCode
     * @throws IOException if we cannot read
     */
    public static Map<Integer, TMCName> loadAll(final File aDirectory, final TMCLocationTable aLocationTable) throws IOException {
    Map<Integer, TMCName> retval = new HashMap<Integer, TMCName>();

        File inFile = new File(aDirectory, "NAMES.DAT");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        String line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 0;
            TMCName name = new TMCName(
                    Integer.parseInt(split[i++]), //CID;
                    Integer.parseInt(split[i++]), //TABCD;
                    Integer.parseInt(split[i++]), //NID;
                    split[i++], //NAME;
                    split[i++]); //NCOMMENT;
            retval.put(name.getNID(), name);
        }
        return retval;
    }


    /**
     * @param aCid
     * @param aTabcd
     * @param aLcd
     * @param aClass
     * @param aTcd
     * @param aStcd
     * @param aJunctionnumber
     * @param aRnid
     * @param aN1id
     * @param aN2id
     * @param aPol_lcd
     * @param aOth_lcd
     * @param aSeg_lcd
     * @param aRoa_lcd
     * @param aInpos
     * @param aInneg
     * @param aOutpos
     * @param aOutneg
     * @param aPresentpos
     * @param aPresentneg
     * @param aDiversionpos
     * @param aDiversionneg
     * @param aXcoord
     * @param aYcoord
     * @param aInterruptsroad
     * @param aUrban
     */
    private TMCName(
            final int aCid,
            final int aTabcd,
            final int aNid,
            final String aName,
            final String aNameComment) {
        super(null);
        CID = aCid;
        TABCD = aTabcd;
        NID = aNid;
        NAME = aName;
        NCOMMENT = aNameComment;
    }
    /**
     * @return the NID
     */
    public int getNID() {
        return NID;
    }


    /**
     * @return the cID
     */
    public int getCID() {
        return CID;
    }


    /**
     * @return the tABCD
     */
    public int getTABCD() {
        return TABCD;
    }


    /**
     * @return the nAME
     */
    public String getNAME() {
        return NAME;
    }


    /**
     * @return the nCOMMENT
     */
    public String getNCOMMENT() {
        return NCOMMENT;
    }


    /**
     * Does nothing.
     * @param aOutData where to write to
     * @return null
     */
    public Entity generateReferenceOSM(final MemoryDataSet aOutData) {
        return null;
    }
}
