package net.kaisoz.droidstorm.nxt.demo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

/**
 * Loads a demo from an XML.
 * If the XML file structure is correct, it will be loaded as DemoAction arraylist and passed back to the calling activity
 * Otherwise, null will be returned, which means that the XML file is not correct
 *
 * @author Tomás Tormo Franco
 */

public class DemoXMLManager {

    private InputStream mIn;
    private static final String ACTION_TAG = "nxtaction";
    private static final String POWER_ATTR = "power";
    private static final String TURNRATIO_ATTTR = "turnratio";
    private static final String DELAY_ATTR = "delay";
    private static final String TACHOLIMIT_ATTR = "tacholimit";


    /**
     * Sets the InputStream which will be used to read the XML file
     *
     * @param in
     */
    public void setInputStream(InputStream in) {
        this.mIn = in;
    }

    /**
     * Reads all the actions from XML Actions file and returns a list
     *
     * @return
     */
    public List<DemoAction> parse() {

        XmlPullParser parser = Xml.newPullParser();
        ArrayList<DemoAction> demoActions = null;

        if (mIn == null)
            throw new IllegalArgumentException("No InputStream set");

        try {
            // auto-detect the encoding from the stream
            parser.setInput(mIn, null);
            int eventType = parser.getEventType();
            DemoAction newAction = null;
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        demoActions = new ArrayList<DemoAction>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase(ACTION_TAG)) {
                            newAction = new DemoAction();
                            int attrCount = parser.getAttributeCount();
                            for (int i = 0; i < attrCount; i++) {
                                if (parser.getAttributeName(i).equals(POWER_ATTR)) {
                                    newAction.setPower(Integer.valueOf(parser.getAttributeValue(i)).intValue());
                                } else if (parser.getAttributeName(i).equals(TURNRATIO_ATTTR)) {
                                    newAction.setTurnRatio(Integer.valueOf(parser.getAttributeValue(i)).intValue());
                                } else if (parser.getAttributeName(i).equals(DELAY_ATTR)) {
                                    newAction.setDelay(Long.valueOf(parser.getAttributeValue(i)).intValue());
                                } else if (parser.getAttributeName(i).equals(TACHOLIMIT_ATTR)) {
                                    newAction.setTachoLimit(Long.valueOf(parser.getAttributeValue(i)).intValue());
                                }
                            }
                            demoActions.add(newAction);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return demoActions;
    }
}
