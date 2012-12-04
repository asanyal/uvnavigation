/**
 * PluginMain.java
 * created: 01.03.2008 01:04:12
 *
 ***********************************
 * Editing this file:
 *  -For consistent code-quality this file should be checked with the
 *   checkstyle-ruleset enclosed in this project.
 *  -After the design of this file has settled it should get it's own
 *   JUnit-Test that shall be executed regularly. It is best to write
 *   the test-case BEFORE writing this class and to run it on every build
 *   as a regression-test.
 */
package org.openstreetmap.travelingsalesman.freetts;
//automatically created logger for debug and error -output
import java.util.logging.Logger;



/**
 * (c) 2008 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * PluginMain.java<br/>
 * created: 01.03.2008 01:04:12
 *<br/><br/>
 * <b>Entry-point for the plugin!</b>
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class PluginMain extends org.java.plugin.Plugin {

	/**
	 * Automatically created logger for debug and error-output.
	 */
	private static final Logger LOG = Logger.getLogger(PluginMain.class
			.getName());


	/**
	 * Just an overridden ToString to return this classe's name
	 * and hashCode.
	 * @return className and hashCode
	 */
	@Override
    public String toString() {
		return "PluginMain@" + hashCode();
	}

	@Override
	protected void doStart() throws Exception {
		// ignored
	}

	@Override
	protected void doStop() throws Exception {
		if (FreeTTSVoiceSynth.mySynthesizer != null) {
			FreeTTSVoiceSynth.mySynthesizer.deallocate();
			FreeTTSVoiceSynth.mySynthesizer = null;
		}
	}
}


