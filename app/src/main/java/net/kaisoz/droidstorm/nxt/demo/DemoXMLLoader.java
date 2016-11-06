package net.kaisoz.droidstorm.nxt.demo;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads a demo from an XML.
 * If the XML file structure is correct, it will be loaded as DemoAction arraylist and passed back to the calling activity
 * Otherwise, null will be returned, which means that the XML file is not correct
 *
 * @author Tomás Tormo Franco
 */
public class DemoXMLLoader extends DefaultHandler {

    private static final String ACTION_TAG = "nxtaction";
    private static final String POWER_ATTR = "mPower";
    private static final String TURNRATIO_ATTTR = "turnratio";
    private static final String DELAY_ATTR = "delay";
    private static final String TACHOLIMIT_ATTR = "tacholimit";

    private ArrayList<DemoAction> demoActions;

    public DemoXMLLoader() {
        super();
        demoActions = new ArrayList<DemoAction>();
    }

    @Override
    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
        if (localName.equals(ACTION_TAG)) {
            DemoAction newAction = new DemoAction();

            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equals(POWER_ATTR)) {
                    newAction.setPower(Integer.valueOf(attributes.getValue(i)).intValue());
                } else if (attributes.getQName(i).equals(TURNRATIO_ATTTR)) {
                    newAction.setTurnRatio(Integer.valueOf(attributes.getValue(i)).intValue());
                } else if (attributes.getQName(i).equals(TACHOLIMIT_ATTR)) {
                    newAction.setTachoLimit(Long.valueOf(attributes.getValue(i)).intValue());
                } else if (attributes.getQName(i).equals(DELAY_ATTR)) {
                    newAction.setDelay(Long.valueOf(attributes.getValue(i)).intValue());
                }
            }
            demoActions.add(newAction);
        }
    }


    /**
     * Returns al read actions
     *
     * @return arraylist with al read demo actions
     */
    public ArrayList<DemoAction> getActions() {
        return this.demoActions;
    }

}  