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
 * Data-model of a TMC location-type.<br/>
 * Used by {@link TMCLocationTable}.
 */
public final class TMCType extends TMCEntity {
    private char myCLASS;
    private int  myTCD;
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
    public static Map<Character, Map<Integer, TMCType>> loadAll(final File aDirectory, final TMCLocationTable aLocationTable) throws IOException {
        Map<Character, Map<Integer, TMCType>> retval = new HashMap<Character, Map<Integer,TMCType>>();

        File inFile = new File(aDirectory, "TYPES.DAT");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        String line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 0;
            TMCType type = new TMCType(
                    split[i++].charAt(0), //CLASS;
                    Integer.parseInt(split[i++]), //TCD;
                    split[i++], //TDESC;
                    Integer.parseInt(split[i++]), //TNATCD;
                    split[i++]); //TNATDESC;
            Map<Integer, TMCType> map = retval.get(type.getCLASS());
            if (map == null) {
                map = new HashMap<Integer, TMCType>();
                retval.put(type.getCLASS(), map);
            }
            map.put(type.getTCD(), type);
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
    private TMCType(char aClass, int aTcd, String aTdesc, int aTnatcd,
            String aTnatdesc) {
        super(null);
        myCLASS = aClass;
        myTCD = aTcd;
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
     * Does nothing.
     * @param aOutData where to write to
     * @return null
     */
    public Entity generateReferenceOSM(final MemoryDataSet aOutData) {
        return null;
    }
}
