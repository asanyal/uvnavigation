package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.tools;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.data.WayHelper;
import org.openstreetmap.osm.data.coordinates.Bounds;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeWriter;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.AbstractAutomaticImporter;

/**
 * Import roads represented by relatons automatically.
 * 1. get missing ones from wiki (the wiki contains a table with all entries and the informaton if they have already been imported)
 * 2. search using osmxapi
 * 3. upload import
 * 4. upload wiki
 * This is a helper-program that only applies to Germany.
 * Thus a lot of alternative names are hardcoded.
 */
public class AutoImportRoads extends AbstractAutomaticImporter {

	/**
	 * @param aLastPageNumber the last wiki-page to search for elements to import is aWikiNamespace + (aLastPageNumber - 1) + "00_to_" + (aLastPageNumber) + "00
	 * @param aFirstPageNumber the first wiki-page to search for elements to import is aWikiNamespace + aFirstPageNumber + "00_to_" + (aFirstPageNumber+1) + "00
	 * @param aWikiNamespace namespace under wich to look for the pages to import
	 */
	public AutoImportRoads(final String aWikiNamespace, final int aFirstPageNumber, final int aLastPageNumber) {
		super(aWikiNamespace, aFirstPageNumber, aLastPageNumber);
	}

	public static void main(final String[] args) {
	
		try {
//			new AutoImportRoads("TMC/TMC_Import_Germany/Roads/", 70, 605);
			// 70-393 already done
			new AutoImportRoads("TMC/TMC_Import_Germany/Roads/", 494, 605);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Import an entry that is not imported yet.
	 * @param aRow the row of the wiki-table
	 * @param aChangeWriter where we write our changes to
	 * @return the new row of an imported entry or null if nothing was done
	 * @throws IOException1 if we cannot read our settings 
	 * @throws NumberFormatException  if we cannot read our settings 
	 * @throws ParseException if we cannot parse the bounding-box in the wiki-page
	 */
	protected String importRow(final String aRow, final XmlChangeWriter aChangeWriter) throws NumberFormatException, IOException, ParseException {
		//System.out.println("============ importing row:\n" + aRow + "\n===");
		String name = aRow.replace('\n', ' ').replaceFirst(" of type .*", "").replaceFirst(".*road ", "");
		String code = aRow.replace('\n', ' ').replaceFirst(".*TMCRoad_", "").replaceFirst(".osm.*", "");
		System.out.println("============================================");
		System.out.println("name: " + name);
		System.out.println("code: " + code);

//	    has it a name we can search for?
		if (name.trim().length() == 0) {
			System.out.println("road has no name\n============");
			return null;
		}
		// search for the name in multiple variations
		Relation found = null;
        int count = Integer.parseInt(getSettings().getProperty("tmcimport.national.refSeachRule.count", "0"));
        search: for (int i = -1; i < count; i++) {
			System.out.println("search #" + i + "/" + count + "  failed");
			String newRef = name;
			if (i >= 0) {
				String match   = getSettings().getProperty("tmcimport.national.refSeachRule." + i + ".search");
				String replace = getSettings().getProperty("tmcimport.national.refSeachRule." + i + ".replace");
				if (match == null) {
					throw new IllegalStateException("missing entry 'tmcimport.national.refSeachRule." + i + ".search' in tmcimport.properties");
				}
				if (replace == null) {
					throw new IllegalStateException("missing entry 'tmcimport.national.refSeachRule." + i + ".replace' in tmcimport.properties");
				}
				newRef = name.replaceAll(match, replace);
				if (newRef.equals(name) || newRef.trim().length() == 0) {
					System.out.println("search #" + i + "/" + count + "  does not change the name - replacement ignored");
					continue; // no replacement made
				}
				if (newRef.trim().length() < 2) {
					System.out.println("search #" + i + "/" + count + "  means searching for a single letter - replacement ignored");
					continue; // no replacement made
				}
			}
            System.out.println("searching for road " + code + " - as \"" + newRef + "\"");

            MemoryDataSet osmRoad = searchRelations(newRef);
    		if (osmRoad != null && osmRoad.getRelationsCount() > 0) {
    			Iterator<Relation> rels = osmRoad.getRelations(Bounds.WORLD);
    			while (rels.hasNext()) {
    				Relation rel = rels.next();
    				rel = getDownloader().getRelationByID(rel.getId());
    				Collection<Tag> tags = rel.getTags();
    				/**
    				 * searching for:
    				 * route=road
                     * type=route
    				 */
    				if (WayHelper.getTag(tags, "type") == null || !WayHelper.getTag(tags, "type").equalsIgnoreCase("route")) {
    		            System.out.println("not a type=route");
    					continue;
    				}
    				if (WayHelper.getTag(tags, "route") == null || !WayHelper.getTag(tags, "route").equalsIgnoreCase("road")) {
    		            System.out.println("not a route=road");
    					continue;
    				}
    				// test bounding-box
    				if (!checkBoundingBox(aRow, rel, getDownloader())) {
    		            System.out.println("road does not match bounding-box");
    					continue;    					
    				}
//    				if (WayHelper.getTag(tags, "route") != null && WayHelper.getTag(tags, "route").equalsIgnoreCase("train")) {
//    		            System.out.println("ignoring train-route");
//    					continue;
//    				}
//    				if (WayHelper.getTag(tags, "route") != null && WayHelper.getTag(tags, "route").equalsIgnoreCase("rail")) {
//    		            System.out.println("ignoring train-route");
//    					continue;
//    				}
//    				if (WayHelper.getTag(tags, "route") != null && WayHelper.getTag(tags, "route").equalsIgnoreCase("hiking")) {
//    		            System.out.println("ignoring hiking-route");
//    					continue;
//    				}
//    				if (WayHelper.getTag(tags, "route") != null && WayHelper.getTag(tags, "route").equalsIgnoreCase("foot")) {
//    		            System.out.println("ignoring hiking-route");
//    					continue;
//    				}
//    				if (WayHelper.getTag(tags, "route") != null && WayHelper.getTag(tags, "route").equalsIgnoreCase("bus")) {
//    		            System.out.println("ignoring bus-route");
//    					continue;
//    				}
//    				if (WayHelper.getTag(tags, "route") != null && WayHelper.getTag(tags, "route").equalsIgnoreCase("bicycle")) {
//    		            System.out.println("ignoring bicycle-route");
//    					continue;
//    				}
//    				if (WayHelper.getTag(tags, "boundary") != null) {
//    		            System.out.println("ignoring boundary-relation");
//    					continue;
//    				}
//    				if (WayHelper.getTag(tags, "type") != null && WayHelper.getTag(tags, "type").equalsIgnoreCase("building")) {
//    		            System.out.println("ignoring building-relation");
//    					continue;
//    				}
//    				if (WayHelper.getTag(tags, "type") != null && WayHelper.getTag(tags, "type").equalsIgnoreCase("public_transport")) {
//    		            System.out.println("ignoring public_transport-relation");
//    					continue;
//    				}
    				found = rel;
    				break search;
				}
    			
    		}
        }

		if (found == null) {
			System.out.println("search for \"" + name + "" + "\" failed completely");
			return null;
		}
		// apply the tmc location-code
		Collection<Tag> tags = found.getTags();
		if (WayHelper.getTag(tags, "TMC:cid_58:tabcd_1:LCLversion") == null) {
			tags.add(new Tag("TMC:cid_58:tabcd_1:LCLversion", "8.00"));
		} else if (!WayHelper.getTag(tags, "TMC:cid_58:tabcd_1:LCLversion").equals("8.00")) {
			throw new IllegalStateException("relation already has a different LCLversion");
		}
		if (WayHelper.getTag(tags, "TMC:cid_58:tabcd_1:LocationCode") == null) {
			tags.add(new Tag("TMC:cid_58:tabcd_1:LocationCode", code));
		} else if (!WayHelper.getTag(tags, "TMC:cid_58:tabcd_1:LocationCode").equals(code)) {
			throw new IllegalStateException("relation already has a different LocationCode");
		}
		// show tags
		for (Tag tag : tags) {
			System.out.println(tag.getKey() + "=" + tag.getValue());
		}

		List<RelationMember> members = found.getMembers();
		for (RelationMember relationMember : members) {
			if (relationMember.getMemberRole() == null) {
				System.err.println("relation has a member with a null role");
			}
		}
		//upload
		ChangeContainer changeContainer = new ChangeContainer(new RelationContainer(found), ChangeAction.Modify);
		aChangeWriter.process(changeContainer);
		
		// generate the table-row reflecting that this has been imported
		System.out.println("============");

		return "\n| " + code + "\n"
		+ "| [https://wolschon.kleinbetrieb.biz/tmclcl/TMCRoad_" + code + ".osm road " + name + "]\n"
		+ "| {{relation|" + found.getId() + "}}\n"
		+ "| {{BGColor|lime|DONE}}\n"
		+ "| [[User:TMCImporter|automatic TMC-Importer]]\n";
	}
}
