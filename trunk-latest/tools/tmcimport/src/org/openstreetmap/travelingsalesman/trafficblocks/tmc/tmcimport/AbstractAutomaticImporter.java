package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.osm.data.DownloadingDataSet;
import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.data.coordinates.Bounds;
import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osm.data.visitors.BoundingXYVisitor;
import org.openstreetmap.osm.data.visitors.Visitor;
import org.openstreetmap.osm.io.DataSetSink;
import org.openstreetmap.osm.io.URLDownloader;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeWriter;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.Wikiutils.Wikipage;

public abstract class AbstractAutomaticImporter {

	private static final String UNCHANGEDROW = "\\|.*none.*TODO.*your name here.*";
	private Wikiutils myWikiUtils;

	/**
	 * Download whatever we need.
	 */
	private DownloadingDataSet myDownloader = new DownloadingDataSet();
	/**
	 * @return the downloader
	 */
	protected DownloadingDataSet getDownloader() {
		return myDownloader;
	}
	/**
	 * @param aLastPageNumber the last wiki-page to search for elements to import is aWikiNamespace + (aLastPageNumber - 1) + "00_to_" + (aLastPageNumber) + "00
	 * @param aFirstPageNumber the first wiki-page to search for elements to import is aWikiNamespace + aFirstPageNumber + "00_to_" + (aFirstPageNumber+1) + "00
	 * @param aWikiNamespace namespace under wich to look for the pages to import
	 */
	public AbstractAutomaticImporter(final String aWikiNamespace, final int aFirstPageNumber, final int aLastPageNumber) {
		try {
			for (int i=aFirstPageNumber; i<aLastPageNumber; i++) {
				HashMap<String, String> taskArgs = new HashMap<String, String>();
				taskArgs.put("file", "changes_" + i + "00_to_" + (i+1) + "00.osc");
				File changeFile = new File("changes_" + i + "00_to_" + (i+1) + "00.osc");
				XmlChangeWriter changeWriter = new XmlChangeWriter(changeFile, CompressionMethod.None);

				String wikipage = aWikiNamespace + i + "00_to_" + (i+1) + "00";

				System.out.println("checking wiki-page: " + wikipage);
				Wikipage page = getWikiUtils().getPageFromWiki(wikipage);
				if (page == null) {
					continue;
				}
				List<String> rows = page.getRows();
				System.out.println("#rows in this page=" + rows.size());
				boolean changesMade = false;
				for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
					String row = rows.get(rowIndex);
					if (!row.replace('\n', ' ').trim().matches(getUnchangedRowPattern())) {
						System.out.println("rows " + rowIndex + " is already done by someone else");
						continue; // this row is already imported
					}
					try {
						String newRow = importRow(row, changeWriter);
						if (newRow != null) {
							System.out.println("change to wiki collected");
							rows.set(rowIndex, newRow);
							changesMade = true;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (changesMade) {
					System.out.println("uploading changes to OpenStreetMap");
					changeWriter.complete();
					DownloadingDataSet uploadOSM = new DownloadingDataSet();
		            LinkedList<Tag> comments = new LinkedList<Tag>();
		            comments.add(new Tag("created_by", "LibOSM - DownloadingDataSet V1.0"));
		            comments.add(new Tag("comment", "automatic import of TMC LocationCodes"));
		            comments.add(new Tag("user", "Marcus Wolschon"));
		            comments.add(new Tag("osc filename", changeFile.getName()));
					String user = getSettings().getProperty("wiki.user");
					int changeset = uploadOSM.openChangeset(comments, user, getSettings().getProperty("wiki.password"));
					uploadOSM.uploadChange(changeset, changeFile, user, getSettings().getProperty("wiki.password"));
					uploadOSM.closeChangeset(changeset, user, getSettings().getProperty("wiki.password"));
					System.out.println("uploading changes to wiki");
					getWikiUtils().uploadPageToWiki(wikipage, page);
				} else {
					System.out.println("no imports done for this wiki-page");
					changeWriter.complete();
					changeFile.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * @return the wikiUtils
	 * @throws IOException if our settings cannot be reat
	 */
	protected Wikiutils getWikiUtils() throws IOException {
		if (myWikiUtils == null) {
			myWikiUtils = new Wikiutils(getSettings().getProperty("wiki.user"), getSettings().getProperty("wiki.password"), getSettings().getProperty("wiki.baseurl"));
		}
		return myWikiUtils;
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
	protected abstract String importRow(final String aRow, final XmlChangeWriter aChangeWriter) throws NumberFormatException, IOException, ParseException;
	/**
	 * @return a regex-pattern to match table-rows that have not been done yet.
	 * @throws IOException if we cannot read our config
	 */
	protected String getUnchangedRowPattern() throws IOException {
		Properties mySettings = getSettings();
	
	    return mySettings.getProperty("wiki.unchangeRowPattern", UNCHANGEDROW);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	protected Properties getSettings() throws IOException {
		Properties mySettings = new Properties();
	    mySettings.load(getClass().getClassLoader()
	             .getResourceAsStream("org/openstreetmap/travelingsalesman/trafficblocks/tmc/tmcimport/tools/tmcimport.properties"));
		return mySettings;
	}

	/**
	 * Recursively let the visitor visit all members of the given entity.
	 * Download what is not in the given map.
	 * @param aVisitor the visitos to show around
	 * @param aMap the map to look up things before resorting to a download
	 * @param aRelation the member to visit
	 */
	private void recurse(final Visitor aVisitor, final IDataSet aMap, final Relation aRelation) {
		aVisitor.visit(aRelation);
		List<RelationMember> members = aRelation.getMembers();
		for (RelationMember relationMember : members) {
			if (relationMember.getMemberType() == EntityType.Way) {
				Way way = aMap.getWaysByID(relationMember.getMemberId());
				if (way == null) {
					way = myDownloader.getWaysByID(relationMember.getMemberId());
				}
				if (way != null) {
					recurse(aVisitor, aMap, way);
				}
			} else if (relationMember.getMemberType() == EntityType.Node) {
				Node node = aMap.getNodeByID(relationMember.getMemberId());
				if (node == null) {
					try {
						node = myDownloader.getNodeByID(relationMember.getMemberId());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (node != null) {
					aVisitor.visit(node);
				}
			} else if (relationMember.getMemberType() == EntityType.Relation) {
				// ignored
			}
		}
		
	}

	/**
	 * Recursively let the visitor visit all members of the given entity.
	 * Download what is not in the given map.
	 * @param aVisitor the visitos to show around
	 * @param aMap the map to look up things before resorting to a download
	 * @param aWay the member to visit
	 */
	private void recurse(final Visitor aVisitor, final IDataSet aMap, final Way aWay) {
		aVisitor.visit(aWay);
		List<WayNode> wayNodes = aWay.getWayNodes();
		for (WayNode wayNode : wayNodes) {
			Node node = aMap.getNodeByID(wayNode.getNodeId());
			if (node == null) {
				try {
					node = myDownloader.getNodeByID(wayNode.getNodeId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (node != null) {
				aVisitor.visit(node);
			}
		}		
	}

	/**
	 * Check that all relevant members of the given relation
	 * are aproximately in the bounding-box given in the table-row
	 * provided.
	 * @param aRow a wiki-table-row
	 * @param aMap the map containing the relation and it´s members
	 * @param aFound a relation to test
	 * @return true if everything is okay
	 * @throws ParseException if we cannot parse the bounding-box
	 */
	protected boolean checkBoundingBox(final String aRow, final Entity aFound, final IDataSet aMap) throws ParseException {
		//Bounds
		BoundingXYVisitor visitor = new BoundingXYVisitor(aMap);
		if (aFound instanceof Relation) {
			Relation rel = (Relation) aFound;
			recurse(visitor, aMap, rel);
		} else if (aFound instanceof Way) {
			recurse(visitor, aMap, (Way) aFound);
		} else if (aFound instanceof Node) {
			visitor.visit((Node) aFound);
		} else {
			throw new IllegalArgumentException("OSM-Object is no relation, way or node but a: "
					+ aFound.getClass());			
		}
		Bounds bounds = visitor.getBounds();
		if (bounds == null) {
			throw new IllegalArgumentException("OSM-Object found has no bounds");
		}
		
		Matcher matcher = Pattern.compile("left=([0-9]*\\.[0-9]*).*right=([0-9]*\\.[0-9]*).*top=([0-9]*\\.[0-9]*).*bottom=([0-9]*\\.[0-9]*)").matcher(aRow.replace('\n', ' '));
		if (!matcher.find()) {
			throw new IllegalArgumentException("row does not contain a bounding-box.\n" + aRow);
		}
		
		Double left   = new BigDecimal(matcher.group(1)).doubleValue();   // minimum longitude
		Double right  = new BigDecimal(matcher.group(2)).doubleValue();  // maximum longitude
		Double top    = new BigDecimal(matcher.group(3)).doubleValue();    // maximum latitude
		Double bottom = new BigDecimal(matcher.group(4)).doubleValue(); // minimum latitude

		LatLon max = bounds.getMax(); //NullPointerException
		LatLon min = bounds.getMin();

		double hor = 2.6 * (max.getXCoord() - min.getXCoord());
		double vert = 2.6 * (max.getYCoord() - min.getYCoord());

		System.out.println("DEBUG: boundingBoxMatch:");
		System.out.println("\t" + min.getXCoord() + " > " + (bottom - vert) + "= " + (min.getXCoord() > bottom - vert));
		System.out.println("\t" + min.getYCoord() + " > " + (left - hor) + "= " + (min.getYCoord() > left - hor));
		System.out.println("\t" + max.getXCoord() + " < " + (top + vert) + "= " + (max.getXCoord() < top + vert));
		System.out.println("\t" + max.getYCoord() + " < " + (right + hor) + "= " + (max.getYCoord() < right + hor));
		//TODO: test this
		return min.getXCoord() > bottom - vert
		&& min.getYCoord() > left - hor
		&& max.getXCoord() < top + vert
		&& max.getYCoord() < right + hor;
	}

	/**
	 * Find a relation by name or ref -attribute using
	 * the OSMXAP.
	 * @param aName the name to look for
	 * @throws UnsupportedEncodingException if UTF8 is not supported 
	 */
	protected MemoryDataSet searchRelations(final String name) throws UnsupportedEncodingException {
		//String url = "http://www.informationfreeway.org/api/0.6/relation%5Bname%7Cref%7Cname:de%7Cshort_name:de="  + URLEncoder.encode(name, "UTF8") + "%5D";
		String url = "http://osmxapi.hypercube.telascience.org/api/0.6/relation[name|ref|name:de|short_name:de=" + URLEncoder.encode(name, "UTF8") + "]";
		System.out.println(url);

		//String url = "http://78.46.81.38/api/elems?relation&name=" + URLEncoder.encode(name);
		//http://78.46.81.38/api/elems?relation&ref=L%201100
		MemoryDataSet mem = new MemoryDataSet();			
		try {
			URLDownloader downloader = new URLDownloader(new URL(url));
			downloader.setSink(new DataSetSink(mem));
			downloader.run();
			//download members of relations and ways
			return mem;
		} catch (Exception e) {
			System.err.println("URL was: " + url);
			System.err.println("#nodes so far    : " + mem.getNodesCount());
			System.err.println("#ways so far     : " + mem.getWaysCount());
			System.err.println("#relations so far: " + mem.getRelationsCount());
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
			System.err.println("URL was: " + url);
			System.err.println("#nodes so far    : " + mem.getNodesCount());
			System.err.println("#ways so far     : " + mem.getWaysCount());
			System.err.println("#relations so far: " + mem.getRelationsCount());
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Find a way by name or ref -attribute using
	 * the OSMXAP.
	 * @param aName the name to look for
	 * @throws UnsupportedEncodingException if UTF8 is not supported
	 */
	protected MemoryDataSet searchWays(final String aName) throws UnsupportedEncodingException {
		String url = "http://osmxapi.hypercube.telascience.org/api/0.6/way[name|ref|name:de|short_name:de=" + URLEncoder.encode(aName, "UTF8") + "]";
		System.out.println(url);
		MemoryDataSet mem = new MemoryDataSet();			
		try {
			URLDownloader downloader = new URLDownloader(new URL(url));
			downloader.setSink(new DataSetSink(mem));
			downloader.run();
			return mem;
		} catch (Exception e) {
			System.err.println("URL was: " + url);
			System.err.println("#nodes so far    : " + mem.getNodesCount());
			System.err.println("#ways so far     : " + mem.getWaysCount());
			System.err.println("#relations so far: " + mem.getRelationsCount());
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
			System.err.println("URL was: " + url);
			System.err.println("#nodes so far    : " + mem.getNodesCount());
			System.err.println("#ways so far     : " + mem.getWaysCount());
			System.err.println("#relations so far: " + mem.getRelationsCount());
			e.printStackTrace();
			return null;
		}
	}


}