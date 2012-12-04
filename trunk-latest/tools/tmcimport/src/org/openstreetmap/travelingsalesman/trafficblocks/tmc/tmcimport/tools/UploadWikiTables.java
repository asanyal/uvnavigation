package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Properties;

import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.Wikiutils;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.Wikiutils.Wikipage;

public class UploadWikiTables {


    /**
     * @param args
     */
    public static void main(final String[] args) {

        try {

        	File testdir = new File("D:\\workspace\\tmcimport\\generated\\2009-12-13_22_26_56_0056");
        	File[] files = testdir.listFiles(new FilenameFilter() {

				/**
				 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
				 */
				@Override
				public boolean accept(final File aDir, final String aName) {
//TODO: change here to upload not only roads but areas, segments and points too
//					return aName.startsWith("Roads.wiki.") && !aName.equals("Roads.wiki.txt");
//					return aName.startsWith("OtherAreas.wiki.") && !aName.equals("OtherAreas.wiki.txt");
//					return aName.startsWith("Segments.wiki.") && !aName.equals("Segments.wiki.txt");
					return aName.startsWith("Points.wiki.") && !aName.equals("Points.wiki.txt");
				}    		
        	});
        	Properties mySettings = new Properties();
            mySettings.load(UploadWikiTables.class.getClassLoader()
                     .getResourceAsStream("org/openstreetmap/travelingsalesman/trafficblocks/tmc/tmcimport/tools/tmcimport.properties"));


//            String unchangedRowPattern = "\\|.*none.*TODO.*your name here.*";
        	for (int i = 0; i < files.length; i++) {
        		File testfile = files[i];
        		String wikipage = mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Points/" + files[i].getName().substring("Points.wiki.".length(), files[i].getName().length() - ".txt".length());
        		System.out.println("uploading wiki-page: " + wikipage);
                Wikiutils testSubject = new Wikiutils("TMCImporter", "TMCImporterTMCImporter", "http://wiki.openstreetmap.org/");
                Wikipage updates = new Wikipage(testSubject.readTextFromStream(new FileInputStream(testfile)));
                Wikipage page = testSubject.getPageFromWiki(wikipage);
				if (page == null) {
					testSubject.uploadPageToWiki(wikipage, updates);
				} else {
					page.applyUpdates(updates/*, unchangedRowPattern*/);
					testSubject.uploadPageToWiki(wikipage, page);
				}
        	}
//        	
//            //TESTRUN
//            File testfile = new File("D:\\workspace\\osmnavigation\\src\\org\\openstreetmap\\travelingsalesman\\trafficblocks\\tmc\\TMC-Testdata\\Germany\\LCL8.00.D-081201\\2009-10-07_16_17_25_0025\\Areas.wiki.400_to_500.txt");
//            //URL wikipage = new URL("http://wiki.openstreetmap.org/index.php?title=TMC/TMC_Import_Germany/Areas/400_to_500&action=edit");
//            String wikipage = "TMC/TMC_Import_Germany/Areas/400_to_500";
//            String unchangedRowPattern = "\\|.*none.*TODO.*your name here.*";
//            int rowIDColumn = 0;
//
//            Wikiutils testSubject = new Wikiutils("TMCImporter", "TMCImporterTMCImporter", "http://wiki.openstreetmap.org/");
//            Wikipage page = testSubject.getPageFromWiki(wikipage);
//            Wikipage updates = new Wikipage(testSubject.readTextFromStream(new FileInputStream(testfile)));
//            page.applyUpdates(updates, unchangedRowPattern);
//            String wikitext = page.getWikitext();
//            //System.out.println(wikitext);
//            testSubject.uploadPageToWiki("TMC/TMC_Import_Germany/Areas/400_to_500", page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
