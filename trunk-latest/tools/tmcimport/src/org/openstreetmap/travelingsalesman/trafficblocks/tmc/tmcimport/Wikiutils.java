package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Wikiutils {

    /**
     * Read this many byte at once from a stream.
     */
    private static final int BUFFERSIZE = 255;
    /**
     * All cookies we collected.
     */
    private LinkedList<String> myCookies;
    /**
     * the URL without "index.php", e.g. "http://wiki.openstreetmap.org/"
     */
    private String myWikiBaseURL;

    /**
     * Login to the OpenStreetMap-Wiki and remember the session-cookie.
     * @param aUserName the user to log in as
     * @param aPassword the password to use
     * @param aWikiBaseURL the URL without "index.php", e.g. "http://wiki.openstreetmap.org/"
     * @throws IOException if we cannot login
     */
    public Wikiutils(final String aUserName, final String aPassword, final String aWikiBaseURL) throws IOException {
        this.myWikiBaseURL = aWikiBaseURL;
        URL loginURL = new URL(aWikiBaseURL
                + "index.php?title=Special:UserLogin&action=submitlogin&type=login"
                );

        // login
        HttpURLConnection uc = (HttpURLConnection) loginURL.openConnection();
      //basic auth        String userPassword = aUserName + ":" + aPassword;
      //basic auth        String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
      //basic auth        uc.setRequestProperty("Authorization", "Basic " + encoding);

        //send http-post parameters
        uc.setDoOutput(true);
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        uc.setRequestProperty("Cookie", "wiki_session=00000000000000000000000000000000");
        uc.setRequestMethod("POST");

        OutputStreamWriter out = new OutputStreamWriter(
                                  uc.getOutputStream(), "UTF8");
        out.write("wpName=" + URLEncoder.encode(aUserName, "UTF-8"));
        out.write("&");
        out.write("wpPassword=" + URLEncoder.encode(aPassword, "UTF-8"));
        out.write("&wpRemember=1&wpLoginattempt");
        out.close();

        String headerName  = null;
        this.myCookies = new LinkedList<String>();
        for (int i = 1; (headerName = uc.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = uc.getHeaderField(i);
                System.err.println("full cookie: \"" + cookie + "\"");
                cookie = cookie.substring(0, cookie.indexOf(";"));
                this.myCookies.add(cookie);
                System.err.println("cookie: \"" + cookie + "\"");
                //            String cookieName = cookie.substring(0, cookie.indexOf("="));
                //            String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
            }
        }

        String html = readTextFromStream((InputStream) uc.getInputStream());
//        System.out.println(html);
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {

        try {

        	File testdir = new File("D:\\workspace\\tmcimport\\generated\\2009-11-03_19_40_32_0032");
        	File[] files = testdir.listFiles(new FilenameFilter() {

				/**
				 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
				 */
				@Override
				public boolean accept(final File aDir, final String aName) {
					return aName.startsWith("Roads.wiki.") && !aName.equals("Roads.wiki.txt");
				}    		
        	});
        	Properties mySettings = new Properties();
            mySettings.load(Wikiutils.class.getClassLoader()
                     .getResourceAsStream("org/openstreetmap/travelingsalesman/trafficblocks/tmc/tmcimport/tmcimport.properties"));


//            String unchangedRowPattern = "\\|.*none.*TODO.*your name here.*";
        	for (int i = 0; i < files.length; i++) {
        		File testfile = files[i];
        		String wikipage = mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Roads/" + files[i].getName().substring("Roads.wiki.".length(), files[i].getName().length() - ".txt".length());
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

    /**
     * @param aWikipage a page-name in urlencoding
     * @return the content of that page in the wiki.
     * @throws IOException see {@link #getPageFromWiki(URL)}
     * @throws MalformedURLException if we cannot build a valid URL for that page
     */
    public Wikipage getPageFromWiki(final String aWikipage) throws IOException {
        try {
			return getPageFromWiki(new URL(this.myWikiBaseURL + "/index.php?title=" + aWikipage + "&action=edit"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

    /**
     * @param aPageName where to upload to
     * @param aWikipage the content to upload
     * @throws IOException
     */
    public void uploadPageToWiki(final String aPageName, final Wikipage aWikipage) throws IOException {
        System.out.println("uploading wikipage...");
        String resultPage = tryPageUpload(aPageName, aWikipage, "");
        //System.out.println(resultPage);
        //TODO: if the resultPage contains a captcha...

        if (resultPage.indexOf("Summary preview") > -1 && resultPage.indexOf("wpCaptchaWord") < 0) {
            System.out.println("====================================");
            System.out.println(resultPage);
            parseHiddenFields(resultPage, aWikipage);
        	System.out.println("forced preview - submitting again");
            resultPage = tryPageUpload(aPageName, aWikipage, "");        	
        }
        if (resultPage.indexOf("Someone else has changed this page since you started editing it") > -1 && resultPage.indexOf("wpCaptchaWord") < 0) {
            System.out.println("====================================");
            System.out.println(resultPage);
            parseHiddenFields(resultPage, aWikipage);
        	System.out.println("forced preview due to conflict- submitting again");
            resultPage = tryPageUpload(aPageName, aWikipage, "");        	
        }
        
        if (resultPage.indexOf("wpCaptchaWord") >= 0) {
            System.out.println("====================================");
            System.out.println(resultPage);
            parseHiddenFields(resultPage, aWikipage);
            Matcher matcher = Pattern.compile("wpCaptchaWord.>([ 0-9+-]*)<.label>").matcher(resultPage);
            if (!matcher.find()) {
                throw new IllegalStateException("There is a captcha but I cannot find it");
            }
            String captcha = matcher.group(1);
            System.out.println("captcha = " + captcha);
            String wpCaptchaWord = null;
            Matcher addition = Pattern.compile("([0-9]*) \\+ ([0-9]*)").matcher(captcha);
            if (!addition.find()) {
                Matcher subtraction = Pattern.compile("([0-9]*) - ([0-9]*)").matcher(captcha);
                if (!subtraction.find()) {
                    throw new IllegalStateException("unknown captcha -type");
                } else {
                    wpCaptchaWord = "" + (Integer.parseInt(subtraction.group(1)) - Integer.parseInt(subtraction.group(2)));
                }
            } else {
                wpCaptchaWord = "" + (Integer.parseInt(addition.group(1)) + Integer.parseInt(addition.group(2)));
            }
            System.out.println("wpCaptchaWord = " + wpCaptchaWord);
            // <label for="wpCaptchaWord">99 + 2</label> = <input name="wpCaptchaWord" id="wpCaptchaWord" tabindex="1" />
            resultPage = tryPageUpload(aPageName, aWikipage, "wpCaptchaWord=" + wpCaptchaWord);
            //System.out.println(resultPage);
        }
        System.out.println("====================================");
    }

    /**
     * @see #uploadPageToWiki(String, Wikipage)
     */
    private String tryPageUpload(final String aPageName,
            final Wikipage aWikipage,
            final String additionalParameters) throws MalformedURLException,
            IOException, UnsupportedEncodingException, ProtocolException {
        URL submitURL = new URL("http://wiki.openstreetmap.org/index.php?title="
                + aPageName + "&action=submit");
        Map<String, String> hiddenFields = aWikipage.getHiddenFields();

        HttpURLConnection uc = (HttpURLConnection) submitURL.openConnection();
        uc.setDoOutput(true);
        StringBuilder allCookies = new StringBuilder();
        for (String cookie : this.myCookies) {
            if (allCookies.length() > 0) {
                allCookies.append("; ");
            }
            System.err.println("sending cookie: " + cookie);
            allCookies.append(cookie);
        }
        String wikitext = aWikipage.getWikitext();
        StringBuilder allParameters = new StringBuilder("wpTextbox1=");
        allParameters.append(URLEncoder.encode(wikitext, "UTF-8"));
        System.err.println("sending parameter: wpTextbox1");
        allParameters.append("&wpSummary=automated_upload&wpSave=" + URLEncoder.encode("Save page", "UTF-8"));
        System.err.println("sending parameter: wpSummary");
        allParameters.append("&action=submit");
        System.err.println("sending parameter: action=submit");
        allParameters.append("&title" + aPageName);
        System.err.println("sending parameter: title=" + aPageName);
        if (additionalParameters != null && additionalParameters.length() > 0) {
            allParameters.append("&" + additionalParameters);
            System.err.println("sending additional parameter: " + additionalParameters);
        }
        for (String parameter : hiddenFields.keySet()) {
            allParameters.append("&");
            allParameters.append(parameter)
            .append('=')
            .append(URLEncoder.encode(hiddenFields.get(parameter), "UTF-8"));
            System.err.println("sending parameter: " + parameter + "="
            + hiddenFields.get(parameter));
        }
        uc.setRequestProperty("Cookie", allCookies.toString());
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        uc.setRequestMethod("POST");
        uc.connect();

        OutputStreamWriter out = new OutputStreamWriter(
                                  uc.getOutputStream(), "UTF8");
        out.write(allParameters.toString());
        out.close();


        InputStream readStream = uc.getInputStream();
        String resultPage = readTextFromStream(readStream);
        return resultPage;
    }

    private Wikipage getPageFromWiki(final URL aWikipage) throws IOException {

        String html = readTextFromURL(aWikipage);
        int startWikitext  = html.indexOf("<textarea");
        startWikitext = html.indexOf('>', startWikitext);
        int endWikitext = html.indexOf("</textarea>", startWikitext);
        String extract = html.substring(startWikitext + 1, endWikitext);
        extract = extract.replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">");
		Wikipage wikipage = new Wikipage(extract);

        /*
         * <input type='hidden' value="" name="wpSection" />
<input type='hidden' value="20091020052536" name="wpStarttime" />

<input type='hidden' value="20091019002223" name="wpEdittime" />

<input type='hidden' value="" name="wpScrolltop" id="wpScrolltop" />
<input type='hidden' value="89940b9d2a588c1bb3551bad7780cc81+\" name="wpEditToken" />
         */

        parseHiddenFields(html, wikipage);
        System.err.println("searching for hidden fields ended");
        return wikipage;
    }

	/**
	 * @param html
	 * @param wikipage
	 */
	private void parseHiddenFields(String html, Wikipage wikipage) {

        System.err.println("hidden: - trying pattern 1");

		Pattern hiddenPattern = Pattern.compile("input type='hidden' value=\\\"([^\\\"]*)\\\" name=\\\"([^\\\"]*)\\\" ");
//        Pattern hiddenPattern = Pattern.compile("input type=.hidden. value=.(.*). name=.(.*). ");
        Matcher hiddenMatcher = hiddenPattern.matcher(html);
        while (hiddenMatcher.find()) {
            //TODO: add as property to Wikipage
        	System.err.println("hidden: value=" + hiddenMatcher.group(1) + " name=" + hiddenMatcher.group(2));
        	wikipage.addHiddenField(hiddenMatcher.group(2), hiddenMatcher.group(1));
        }

        System.err.println("hidden: - trying pattern 2");

        hiddenPattern = Pattern.compile("input type=\\\"hidden\\\" name=\\\"([^\\\"]*)\\\" id=\\\"([^\\\"]*)\\\" value=\\\"([^\\\"]*)\\\" ");
//        hiddenPattern = Pattern.compile("<input type=\\\"hidden\\\" name=\\\"(wpCaptchaId)\\\" id=\\\"wpCaptchaId\\\" value=\\\"([0-9]*)\\\"");
        hiddenMatcher = hiddenPattern.matcher(html);
        while (hiddenMatcher.find()) {
        	//TODO: add as property to Wikipage
          System.err.println("hidden(type 2): value=" + hiddenMatcher.group(3) + " name=" + hiddenMatcher.group(1));
          wikipage.addHiddenField(hiddenMatcher.group(1), hiddenMatcher.group(3));
      }
	}

    /**
     * @param aWikipage
     * @return
     * @throws IOException
     */
    private String readTextFromURL(final URL aWikipage) throws IOException {
        URLConnection uc = aWikipage.openConnection();
        uc.setDoOutput(true);

        // send session-cookie
        StringBuilder allCookies = new StringBuilder();
        for (String cookie : this.myCookies) {
            if (allCookies.length() > 0) {
                allCookies.append("; ");
            }
            allCookies.append(cookie);
        }
        uc.setRequestProperty("Cookie", allCookies.toString());
        uc.connect();

        InputStream readStream = uc.getInputStream();

        // read session-cookie
        String headerName = null;
        for (int i = 1; (headerName = uc.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = uc.getHeaderField(i);
                System.err.println("full cookie: \"" + cookie + "\"");
                cookie = cookie.substring(0, cookie.indexOf(";"));
                this.myCookies.add(cookie);
                System.err.println("cookie: \"" + cookie + "\"");
                //            String cookieName = cookie.substring(0, cookie.indexOf("="));
                //            String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
            }
        }

        return readTextFromStream(readStream);
    }

    /**
     * @param readStream
     * @return
     * @throws IOException
     */
    public String readTextFromStream(final InputStream readStream)
            throws IOException {
        Reader reader = new InputStreamReader(readStream, "UTF8");
        StringBuilder inputChars = new StringBuilder();
        char[] buffer = new char[BUFFERSIZE];
        int reat = -1;
        while ((reat = reader.read(buffer)) >= 0) {
            inputChars.append(buffer, 0, reat);
        }
        String html = inputChars.toString();
        return html;
    }

    /**
     * Represents a wiki-page with a single table in it.
     * @author Marcus@Wolschon.biz
     *
     */
    public static class Wikipage {
        private String myPrefix;
        private List<String> myRows;
        private Map<String, String> myHiddenFields = new HashMap<String, String>();
        /**
         * @return the hiddenFields
         */
        public Map<String, String> getHiddenFields() {
            return myHiddenFields;
        }
        /**
         * @param aHiddenFields the hiddenFields to set
         */
        public void setHiddenFields(Map<String, String> aHiddenFields) {
            myHiddenFields = aHiddenFields;
        }
        /**
         * @return the rows
         */
        protected List<String> getRows() {
            return myRows;
        }
        public void addHiddenField(String aName, String aValue) {
            this.myHiddenFields.put(aName, aValue);
        }
        public String getWikitext() {
            StringBuilder sb = new StringBuilder(this.myPrefix);
            boolean first = true;
            for (String row : this.getRows()) {
                if (first) {
                    first = false;
                } else {
                    sb.append("|-\n");
                }
                sb.append(row);
            }
            sb.append("|}\n");
            sb.append(this.myPostfix);
            return sb.toString();
        }
        private String myPostfix;
        /**
         * Parse a given wikitext into a Wikipage.
         * @param aWikiText the wikitext to parse.
         */
        public Wikipage(final String aWikiText) {
            String[] split = aWikiText.split("(?m)(^\\|-)|(^\\|})");
            myPrefix = split[0] + "\n|-";
            if (split.length == 1) {
            	System.err.println("no table-rows in page:\n" + aWikiText);
            	throw new IllegalArgumentException("no table in given wiki-page");
            }
            //System.out.println("prefix=" + myPrefix);
            myPostfix = split[split.length - 1];
            //System.out.println("postfix=" + myPostfix);
            myRows = new ArrayList<String>(split.length - 2);
            for (int i = 1; i < split.length - 1; i++) {
                myRows.add(split[i]);
                //System.out.println("row=" + split[i]);
            }
        }
        /**
         * Test each row against the given pattern. If it matches, overwrite it with the
         * corresponding row of the given update.
         * @param aUpdates the new version to apply to unchanged rows
         * @throws IOException if we cannot read out settings
         */
        public void applyUpdates(final Wikipage aUpdates) throws IOException {
        	Properties mySettings = new Properties();
            mySettings.load(getClass().getClassLoader()
                     .getResourceAsStream("org/openstreetmap/travelingsalesman/trafficblocks/tmc/tmcimport/tools/tmcimport.properties"));

            applyUpdates(aUpdates, mySettings.getProperty("wiki.unchangeRowPattern"));
        }
        /**
         * Test each row against the given pattern. If it matches, overwrite it with the
         * corresponding row of the given update.
         * @param aUpdates the new version to apply to unchanged rows
         * @param aUnchangeRowPattern a regexp-pattern to identify unchanged rows
         */
        public void applyUpdates(final Wikipage aUpdates, final String aUnchangeRowPattern) {
        	Pattern pattern = Pattern.compile(aUnchangeRowPattern);
        	List<String> rows = getRows();
        	for (int i = 0; i < aUpdates.getRows().size(); i++) {
        		if (rows.size() <= i) {
    				System.out.println("adding row " + i);
        			rows.add(aUpdates.getRows().get(i));                    	
        		} else {
        			if (pattern.matcher(rows.get(i).replace('\n', ' ').trim()).matches()) {
        				System.out.println("updating row " + i);
        				rows.set(i, aUpdates.getRows().get(i));
        			} else {
        				System.out.println("NOT updating row " + i);
        				//                    System.err.println("NOT updating row " + i + "\n\tpattern=" + aUnchangeRowPattern
        				//                            + "\n\t" + rows.get(i) + "\n========================");
        			}
        		}
            }
        }
    }
}

