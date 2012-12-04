package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.tools;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.data.WayHelper;
import org.openstreetmap.osm.data.coordinates.Bounds;
import org.openstreetmap.osm.io.DataSetSink;
import org.openstreetmap.osm.io.URLDownloader;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeWriter;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.AbstractAutomaticImporter;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.Wikiutils.Wikipage;

/**
 * Import german "Kreise" (admin level 6) automatically.
 * 1. get missing ones from wiki (the wiki contains a table with all entries and the informaton if they have already been imported)
 * 2. search using osmxapi
 * 3. upload import
 * 4. upload wiki
 * This is a helper-program that only applies to Germany.
 * Thus a lot of things are hardcoded.
 */
public class AutoImportAreas extends AbstractAutomaticImporter {


	public static void main(final String[] args) {

		try {
//import of areas is done			AutoImportAreas importer = new AutoImportAreas("TMC/TMC_Import_Germany/Areas/", 0, 600);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param aLastPageNumber the last wiki-page to search for elements to import is aWikiNamespace + (aLastPageNumber - 1) + "00_to_" + (aLastPageNumber) + "00
	 * @param aFirstPageNumber the first wiki-page to search for elements to import is aWikiNamespace + aFirstPageNumber + "00_to_" + (aFirstPageNumber+1) + "00
	 * @param aWikiNamespace namespace under wich to look for the pages to import
	 */
	public AutoImportAreas(final String aWikiNamespace, final int aFirstPageNumber, final int aLastPageNumber) {
		super(aWikiNamespace, aFirstPageNumber, aLastPageNumber);
	}

	/**
	 * Import an entry that is not imported yet.
	 * @param aRow the row of the wiki-table
	 * @param aChangeWriter where we write our changes to
	 * @return the new row of an imported entry or null if nothing was done
	 * @throws ParseException if we cannot parse the bounding-box in the wiki-page
	 * @throws IOException1 if we cannot read our settings 
	 * @throws NumberFormatException  if we cannot read our settings 
	 */
	protected String importRow(final String aRow, final XmlChangeWriter aChangeWriter) throws IOException, ParseException {
		//System.out.println("============ importing row:\n" + aRow + "\n===");
		String name = aRow.replace('\n', ' ').replaceFirst(" of type .*", "").replaceFirst(".*admin area ", "").trim();
		String code = aRow.replace('\n', ' ').replaceFirst(".*TMCAdminArea_", "").replaceFirst(".osm.*", "").trim();
		System.out.println("name: \"" + name + "\"");
		System.out.println("code: " + code);
		// TODO Auto-generated method stub
		// is this a "Kreis"?
		// TODO: boundary=administrative + admin_level=8 == "Order 4 area"(Stadt/Gemeinde)
		// TODO: boundary=administrative + admin_level=6 == "Order 3 area"(Kreis)
		if (aRow.toLowerCase().indexOf("order 3 area") != -1) {
			System.out.println("code " + code + " is an order 3 area(Kreis)\n============");
			return importOrder3Area(aRow, name, code, aChangeWriter);
		}
		if (aRow.toLowerCase().indexOf("order 4 area") != -1) {
			System.out.println("code " + code + " is an order 4 area(Stadt/Gemeinde)\n============");
			String retval = importOrder4Area(aRow, name, code, aChangeWriter);
			if (retval == null) {
				retval = importOrder4AreaWays(aRow, name, code, aChangeWriter);
			}
			return retval;
		}
		System.out.println("code is " + code + " not an order 3 or order 4 area\n============");
		return null;
	}
	private String importOrder3Area(final String aRow, final String aName, final String aCode, final XmlChangeWriter aChangeWriter) throws IOException, ParseException {
		// search for the name in 2 variations
		MemoryDataSet mem = searchRelations(aName + ", Stadt");
		if (mem == null || mem.getRelationsCount() == 0) {
			System.out.println("search #1 for \"" + aName + ", Stadt" + "\" failed");
			mem = searchRelations(aName);
		}
		if (mem == null || mem.getRelationsCount() == 0) {
			System.out.println("search #2 for \"" + aName + "\" failed");
			System.out.println("not found\n============");
			return null;
		}
		Iterator<Relation> relations = mem.getRelations(Bounds.WORLD);
		while (relations.hasNext()) {
			Relation relation = relations.next();
			if (WayHelper.getTag(relation.getTags(), "admin_level") == null) {
				System.out.println("no admin_level\n============");
				continue;
			}
			if (!WayHelper.getTag(relation.getTags(), "admin_level").equals("6")) {
				System.out.println("wrong admin_level\n============");
				continue;
			}
			//		if (WayHelper.getTag(relation.getTags(), "type") == null) {
			//			System.out.println("no type \n============");
			//		continue;
			//		}
			//		if (!WayHelper.getTag(relation.getTags(), "type").equals("multipolygon")) {
			//			System.out.println("wrong type \n============");
			//		continue;
			//		}
			if (!checkBoundingBox(aRow, relation, mem)) {
				System.out.println("relation does not match bounding-box");
				continue;    					
			}

			// apply the tmc location-code
			Collection<Tag> tags = relation.getTags();
			tags.add(new Tag("TMC:cid_58:tabcd_1:LCLversion ", "8.00"));
			tags.add(new Tag("TMC:cid_58:tabcd_1:LocationCode", aCode));
			tags.add(new Tag("TMC:cid_58:tabcd_1:Class", "Area"));

			// show tags
			for (Tag tag : tags) {
				System.out.println(tag.getKey() + "=" + tag.getValue());
			}

			List<RelationMember> members = relation.getMembers();
			for (RelationMember relationMember : members) {
				if (relationMember.getMemberRole() == null) {
					System.err.println("relation has a member with a null role");
				}
			}

			//upload
			ChangeContainer changeContainer = new ChangeContainer(new RelationContainer(relation), ChangeAction.Modify);
			aChangeWriter.process(changeContainer);

			// generate the table-row reflecting that this has been imported
			System.out.println("============DONE importing this area");

			return "\n| " + aCode + "\n"
			+ "| [https://wolschon.kleinbetrieb.biz/tmclcl/TMCAdminArea_" + aCode + ".osm area " + aName + "]\n"
			+ "| {{relation|" + relation.getId() + "}}\n"
			+ "| {{BGColor|lime|DONE}}\n"
			+ "| [[User:TMCImporter|automatic TMC-Importer]]\n";
		}
		return null;
	}

	private String importOrder4Area(final String aRow, final String aName, final String aCode, final XmlChangeWriter aChangeWriter) throws IOException, ParseException {
		// search for the name in 2 variations
		MemoryDataSet mem = searchRelations(aName);
		if (mem == null || mem.getRelationsCount() == 0) {
			System.out.println("search(relation) #1 for \"" + aName + "" + "\" failed");
			//			mem = searchRelations(aName);
			//		}
			//		if (mem == null || mem.getRelationsCount() == 0) {
			//			System.out.println("search(relation) #2 for \"" + aName + "\" failed");
			System.out.println("relation not found\n============");
			return null;
		}
		Iterator<Relation> relations = mem.getRelations(Bounds.WORLD);
		while (relations.hasNext()) {
			Relation relation = relations.next();
			if (WayHelper.getTag(relation.getTags(), "admin_level") == null) {
				if (WayHelper.getTag(relation.getTags(), "place") == null) {
					System.out.println("no admin_level and no place-tag\n============");
					continue;
				} else {
					if (!WayHelper.getTag(relation.getTags(), "place").equals("village")
							&& !WayHelper.getTag(relation.getTags(), "place").equals("town")
							&& !WayHelper.getTag(relation.getTags(), "place").equals("city")) {
						System.out.println("no admin_level and wrong place="
								+ WayHelper.getTag(relation.getTags(), "place") + "\n============");
						continue;
					}
				}
				continue;
			} else {
				if (!WayHelper.getTag(relation.getTags(), "admin_level").equals("8")) {
					System.out.println("wrong admin_level\n============");
					continue;
				}
			}
			//		if (WayHelper.getTag(relation.getTags(), "type") == null) {
			//			System.out.println("no type \n============");
			//			return null;			
			//		}
			//		if (!WayHelper.getTag(relation.getTags(), "type").equals("multipolygon")) {
			//			System.out.println("wrong type \n============");
			//			return null;			
			//		}
			if (!checkBoundingBox(aRow, relation, mem)) {
				System.out.println("relation does not match bounding-box");
				continue;    					
			}

			// apply the tmc location-code
			Collection<Tag> tags = relation.getTags();
			tags.add(new Tag("TMC:cid_58:tabcd_1:LCLversion ", "8.00"));
			tags.add(new Tag("TMC:cid_58:tabcd_1:LocationCode", aCode));
			tags.add(new Tag("TMC:cid_58:tabcd_1:Class", "Area"));

			// show tags
			for (Tag tag : tags) {
				System.out.println(tag.getKey() + "=" + tag.getValue());
			}

			List<RelationMember> members = relation.getMembers();
			for (RelationMember relationMember : members) {
				if (relationMember.getMemberRole() == null) {
					System.err.println("relation has a member with a null role");
				}
			}

			//upload
			ChangeContainer changeContainer = new ChangeContainer(new RelationContainer(relation), ChangeAction.Modify);
			aChangeWriter.process(changeContainer);

			// generate the table-row reflecting that this has been imported
			System.out.println("============DONE importing this area");

			return "\n| " + aCode + "\n"
			+ "| [https://wolschon.kleinbetrieb.biz/tmclcl/TMCAdminArea_" + aCode + ".osm area " + aName + "]\n"
			+ "| {{relation|" + relation.getId() + "}}\n"
			+ "| {{BGColor|lime|DONE}}\n"
			+ "| [[User:TMCImporter|automatic TMC-Importer]]\n";
		}
		return null;

	}


	private String importOrder4AreaWays(final String aRow, final String aName, final String aCode, final XmlChangeWriter aChangeWriter) throws IOException, ParseException {
		// search for the name in 2 variations
		MemoryDataSet mem = searchWays(aName);
		if (mem == null || mem.getWaysCount() == 0) {
			System.out.println("search(way) #1 for \"" + aName + "" + "\" failed");
			//			mem = searchWays(aName);
			//		}
			//		if (mem == null || mem.getWaysCount() == 0) {
			//			System.out.println("search(way) #2 for \"" + aName + "\" failed");
			System.out.println("way not found\n============");
			return null;
		}
		Iterator<Way> ways = mem.getWays(Bounds.WORLD);
		while (ways.hasNext()) {
			Way way = ways.next();
			if (WayHelper.getTag(way.getTags(), "admin_level") == null) {
				if (WayHelper.getTag(way.getTags(), "place") == null) {
					System.out.println("no admin_level and no place-tag\n============");
					continue;
				} else {
					if (!WayHelper.getTag(way.getTags(), "place").equals("village")
							&& !WayHelper.getTag(way.getTags(), "place").equals("town")
							&& !WayHelper.getTag(way.getTags(), "place").equals("city")) {
						System.out.println("no admin_level and wrong place="
								+ WayHelper.getTag(way.getTags(), "place") + "\n============");
						continue;
					}
				}
				continue;
			} else {
				if (!WayHelper.getTag(way.getTags(), "admin_level").equals("8")) {
					System.out.println("wrong admin_level\n============");
					continue;
				}
			}
			//		if (WayHelper.getTag(relation.getTags(), "type") == null) {
			//			System.out.println("no type \n============");
			//			continue;
			//		}
			//		if (!WayHelper.getTag(relation.getTags(), "type").equals("multipolygon")) {
			//			System.out.println("wrong type \n============");
			//			continue;
			//		}
			if (!checkBoundingBox(aRow, way, mem)) {
				System.out.println("way does not match bounding-box");
				continue;    					
			}
			// apply the tmc location-code
			Collection<Tag> tags = way.getTags();
			tags.add(new Tag("TMC:cid_58:tabcd_1:LCLversion ", "8"));
			tags.add(new Tag("TMC:cid_58:tabcd_1:LocationCode", aCode));
			tags.add(new Tag("TMC:cid_58:tabcd_1:Class", "Area"));

			// show tags
			for (Tag tag : tags) {
				System.out.println(tag.getKey() + "=" + tag.getValue());
			}

			//upload
			ChangeContainer changeContainer = new ChangeContainer(new WayContainer(way), ChangeAction.Modify);
			aChangeWriter.process(changeContainer);
			// generate the table-row reflecting that this has been imported
			System.out.println("============DONE importing this area");

			return "\n| " + aCode + "\n"
			+ "| [https://wolschon.kleinbetrieb.biz/tmclcl/TMCAdminArea_" + aCode + ".osm area " + aName + "]\n"
			+ "| {{way|" + way.getId() + "}}\n"
			+ "| {{BGColor|lime|DONE}}\n"
			+ "| [[User:TMCImporter|automatic TMC-Importer]]\n";
		}
		return null;

	}


}
