package com.ivy.sd.intermecprint;

import com.ivy.sd.png.util.Commons;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by hgode on 08.04.2014.
 */
public class PrintFileXML {
    ArrayList<PrintFileDetails> printFileDetails;
    final String TAG="PrintFileXML";
    public ArrayList<PrintFileDetails> getPrintFileDetails(){
        return  printFileDetails;
    }
    public PrintFileXML(InputStream in_s) {
        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            //InputStream in_s = activity.getApplicationContext().getAssets().open(fileXML);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);

            printFileDetails = parseXML(parser);
        } catch (XmlPullParserException e) {
            Commons.printException(TAG+ ",XmlPullParserException: " + e);

        } catch (IOException e) {

            Commons.printException(TAG+ ",IOException: " + e);

        }
        finally {
        }
    }

    private ArrayList<PrintFileDetails> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        printFileDetails=new ArrayList<PrintFileDetails>();
        int eventType = parser.getEventType();
        PrintFileDetails currentPrintFile = null;
        try {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        Commons.print("XmlParser,"+ "START_TAG: name='" + name + "'");
                        if (name.equals("fileentry")) {
                            currentPrintFile = new PrintFileDetails();
                            Commons.print(TAG+ ",new fileentry");
                        } else if (currentPrintFile != null) {
                            if (name.equals("shortname")) {
                                currentPrintFile.shortname = parser.nextText();
                            } else if (name.equals("description")) {
                                currentPrintFile.description = parser.nextText();
                            } else if (name.equals("help")) {
                                currentPrintFile.help = parser.nextText();
                            } else if (name.equals("filename")) {
                                currentPrintFile.filename = parser.nextText();
                                currentPrintFile.printLanguage = PrintLanguage.getLanguage(currentPrintFile.filename);
                                currentPrintFile.printerWidth = PrintLanguage.getPrintWidth(currentPrintFile.filename);
                                Commons.print(TAG+ ",filename ='"+currentPrintFile.filename+"'");
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("fileentry") && currentPrintFile != null) {
                            printFileDetails.add(currentPrintFile);
                            Commons.print(TAG+ ",fileentry END_TAG");
                        }
                }
                eventType = parser.next();
            }
        }catch(XmlPullParserException e){
            Commons.printException(TAG+ ",XmlPullParserException: "+e);
        }
        catch (Exception e){
            Commons.printException(TAG+ ",Exception: "+e);
        }
        return printFileDetails;
    }

    public PrintFileDetails getPrintFileDetails(String sFileName){
        PrintFileDetails printFileDetails1=new PrintFileDetails();
        for(PrintFileDetails pd : printFileDetails) {
            if (pd.filename.equals(sFileName)) {
                printFileDetails1 = pd;
                return printFileDetails1;
            }
        }
        return printFileDetails1;   
    }
}
