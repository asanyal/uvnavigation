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
 * Data-model of a TMC location-subtype.<br/>
 * Used by {@link TMCLocationTable}.
 */
public final class TMCSubType extends TMCEntity {
    private char myCLASS;
    private int  myTCD;
    private int  mySTCD;
    private String myTDESC;
    private int myTNATCD;
    private String myTNATDESC;
    /**
     * Load all elements of this type from the TMC LocationCodeList.
     * @param aDirectory the directory where the location-list is stored.
     * @param aLocationTable the top-level class we are working for
     * @return all elements indexed by LocationCode
     * @throws IOException if we cannot read
     */
    public static Map<Character, Map<Integer, Map<Integer, TMCSubType>>> loadAll(final File aDirectory, final TMCLocationTable aLocationTable) throws IOException {
        Map<Character, Map<Integer, Map<Integer, TMCSubType>>> retval = new HashMap<Character, Map<Integer, Map<Integer, TMCSubType>>>();

        File inFile = new File(aDirectory, "SUBTYPES.DAT");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        String line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 0;
            TMCSubType type = new TMCSubType(
                    split[i++].charAt(0), //CLASS;
                    Integer.parseInt(split[i++]), //TCD;
                    Integer.parseInt(split[i++]), //STCD;
                    split[i++], //SDESC;
                    Integer.parseInt(split[i++]), //SNATCODE;
                    split[i++]); //SNATDESC;
            Map<Integer, Map<Integer, TMCSubType>> map = retval.get(type.getCLASS());
            if (map == null) {
                map = new HashMap<Integer, Map<Integer, TMCSubType>>();
                retval.put(type.getCLASS(), map);
            }
            Map<Integer, TMCSubType> map2 = map.get(type.getTCD());
            if (map2 == null) {
                map2 = new HashMap<Integer, TMCSubType>();
                map.put(type.getTCD(), map2);
            }
            map2.put(type.getSTCD(), type);
        }
        return retval;
    }
    /**
     * @param aClass
     * @param aTcd
     * @param aTdesc
     * @param aTnatcd
     * @param aTnatdesc
     */
    private TMCSubType(
            final char aClass,
            final int aTcd,
            final int aSTcd,
            final String aTdesc,
            final int aTnatcd,
            final String aTnatdesc) {
        super(null);
        myCLASS = aClass;
        myTCD = aTcd;
        mySTCD = aSTcd;
        myTDESC = aTdesc;
        myTNATCD = aTnatcd;
        myTNATDESC = aTnatdesc;
    }
    /**
     * @return the cLASS
     */
    public char getCLASS() {
        return myCLASS;
    }
    /**
     * @return the tCD
     */
    public int getTCD() {
        return myTCD;
    }
    /**
     * @return the tDESC
     */
    public String getTDESC() {
        return myTDESC;
    }
    /**
     * @return the tNATCD
     */
    public int getTNATCD() {
        return myTNATCD;
    }
    /**
     * @return the tNATDESC
     */
    public String getTNATDESC() {
        return myTNATDESC;
    }
    /**
     * @return the sTCD
     */
    public int getSTCD() {
        return mySTCD;
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
