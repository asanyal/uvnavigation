/**
 * This file is part of OSMNavigation by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others) by contacting him directly.
 *
 *  OSMNavigation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OSMNavigation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OSMNavigation.  If not, see <http://www.gnu.org/licenses/>.
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

import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import org.openstreetmap.osm.ConfigurationSection;
import org.openstreetmap.osm.ConfigurationSetting;
import org.openstreetmap.osm.Settings;
import org.openstreetmap.osm.Plugins.IPlugin;
import org.openstreetmap.travelingsalesman.navigation.OsmNavigationConfigSection;
import org.openstreetmap.travelingsalesman.painting.odr.IMapFeaturesSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.Locale;

import com.sun.speech.freetts.jsapi.FreeTTSSynthesizer;
import com.sun.speech.freetts.jsapi.FreeTTSSynthesizerModeDesc;


/**
 * Voice-synthesizer that uses the Java Speech API and
 * defaults to FreeTTS if no other implementation is around.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class FreeTTSVoiceSynth implements org.openstreetmap.travelingsalesman.routing.speech.IVoiceSynth {


    /**
     * The mode we are using.
     * See FreeTTS-documentation for details.
     */
    private SynthesizerModeDesc mySynthesizerModeDesc;

    /**
     * What we do speech with.
     */
    protected static Synthesizer mySynthesizer;

    /**
     * Our logger.
     */
    private static final Logger LOG
    = Logger.getLogger(FreeTTSVoiceSynth.class.getName());

    /**
     * @return the setting of the voice.
     */
    public final ConfigurationSection getSettings() {
        // ensure the default is loaded
        Settings.getInstance().get("freetts.voice", "kevin16");
        ConfigurationSection retval = new ConfigurationSection("FreeTTS");
        retval.addSetting(new ConfigurationSetting("freetts.voice",
                "Voice",
                ConfigurationSetting.TYPES.STRING,
                "FreeTTS",
                "The voice to use."));
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void speak(final String aSentence) {
        checkSpeechProperties();
        final String voiceName = Settings.getInstance().get("freetts.voice", "kevin16");

        try {
            if (mySynthesizer == null) {
                if (mySynthesizerModeDesc == null) {
                    mySynthesizerModeDesc = new SynthesizerModeDesc(
                            null,          // engine name
                            "general",     // mode name
                            Locale.US,     // locale
                            null,          // running
                            null);
                }
                mySynthesizer = Central.createSynthesizer(mySynthesizerModeDesc);
            }
            /* Just an informational message to guide users that didn't
             * set up their speech.properties file. 
             */
            if (mySynthesizer == null) {
                System.err.println("No Synthesizer found, using hardcoded default");
                //com.sun.speech.freetts.jsapi.FreeTTSEngineCentral central = new com.sun.speech.freetts.jsapi.FreeTTSEngineCentral();
                
                mySynthesizer =  new FreeTTSSynthesizer(
                        new FreeTTSSynthesizerModeDesc(
                                mySynthesizerModeDesc.getEngineName(),
                                mySynthesizerModeDesc.getModeName(),
                                mySynthesizerModeDesc.getLocale()));
            }

            /* Get the synthesizer ready to speak
             */
            mySynthesizer.allocate();
            mySynthesizer.resume();

            /* Choose the voice.
             */
            mySynthesizerModeDesc = (SynthesizerModeDesc) mySynthesizer.getEngineModeDesc();
            Voice[] voices = mySynthesizerModeDesc.getVoices();
            Voice voice = null;
            Voice anyOtherVoice = null;
            for (int i = 0; i < voices.length; i++) {
                if (voices[i].getName().equals(voiceName)) {
                    voice = voices[i];
                    break;
                }
                anyOtherVoice = voices[i];
            }
            if (voice == null) {
                System.err.println("Synthesizer has no voice named '"
                        + voiceName + "'.");
                voice = anyOtherVoice;
            }
            if (voice == null) {
                System.err.println("Synthesizer has no voice at all.");
                return;
            }
            mySynthesizer.getSynthesizerProperties().setVoice(voice);

            /* The the synthesizer to speak and wait for it to
             * complete.
             */
            mySynthesizer.speakPlainText(aSentence, null);
            mySynthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);

            /* Clean up and leave.
             */
            //mySynthesizer.deallocate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(final String[] args) {
        System.out.println("creating FreeTTSVoiceSynth");
        FreeTTSVoiceSynth test = new FreeTTSVoiceSynth();   
        System.out.println("speaking");
        test.speak("Hello World");
        System.out.println("done");     
    }
    /**
     * Check if ~/speech.properties exists
     */
    private void checkSpeechProperties() {
        File file = new File(new File(System.getProperty("user.home")), "speech.properties");
        if (!file.exists()) {
            try {
                InputStream in = getClass().getClassLoader().getResourceAsStream("speech.properties");
                FileOutputStream out = new FileOutputStream(file);
                byte[] buffer = new byte[Byte.MAX_VALUE];
                int reat = 1;
                while (reat >= 0) {
                    reat = in.read(buffer);
                    if (reat >= 0) {
                        out.write(buffer, 0, reat);
                    }
                }
                out.close();
                in.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        if (mySynthesizer != null) {
            mySynthesizer.deallocate();
            mySynthesizer = null;
        }
        super.finalize();
    }

}
