/*
**    Chromis POS  - Open Source Point of Sale
**
**    This file is part of Chromis POS Version Chromis V1.5.4
**
**    Copyright (c) 2015-2023 Chromis & previous Openbravo POS related works   
**
**    https://www.chromis.co.uk
**   
**    Chromis POS is free software: you can redistribute it and/or modify
**    it under the terms of the GNU General Public License as published by
**    the Free Software Foundation, either version 3 of the License, or
**    (at your option) any later version.
**
**    Chromis POS is distributed in the hope that it will be useful,
**    but WITHOUT ANY WARRANTY; without even the implied warranty of
**    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
**    GNU General Public License for more details.
**
**    You should have received a copy of the GNU General Public License
**    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>
**
 */
package uk.chromis.commons.utils;

import java.util.UUID;
import java.util.prefs.Preferences;
import uk.chromis.data.loader.PreparedSentence;
import uk.chromis.data.loader.SentenceFind;
import uk.chromis.data.loader.SerializerReadString;
import uk.chromis.data.loader.SerializerWriteString;
import uk.chromis.data.loader.Session;
import uk.chromis.data.loader.SessionFactory;

public class TerminalInfo {

    private static final String TERMINAL = "$1308823401";
    private static final Preferences preferences;
    private static String tuid;
    protected static Session session = SessionFactory.getSession();
    protected static SentenceFind terminalActiveSession;

    static {
        preferences = initPreferences();
        tuid = preferences.get(TERMINAL, null);
        if (tuid == null) {
            tuid = UUID.randomUUID().toString();
            preferences.put(TERMINAL, tuid);
        }
    }

    private static Preferences initPreferences() {
        // Use user-level preferences to avoid permission issues on HKLM
        return Preferences.userRoot().node("chromis");
    }

    public TerminalInfo() {

        terminalActiveSession = new PreparedSentence(session,
                "select terminal_location from terminals where terminal_key = ? ",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE);

    }

    public static String getTerminalLocation() {
        return preferences.get("location", "MainStore");

//        try {
//            return (String) terminalActiveSession.find(tuid);
//        } catch (BasicException ex) {
//            System.out.println("Error get terminal location");
//        }
//        return null;
    }

    public static String getTerminalID() {
        return tuid;
    }

    public static void setTerminalName(String posName) {
        preferences.put("posname", posName);
    }

    public static String getTerminalName() {
        return preferences.get("posname", "Unknown");
    }

    public static void enableWebCam() {
        preferences.putBoolean("webcam", true);
    }

    public static void setWebCam(Boolean value) {
        preferences.putBoolean("webcam", value);
    }

    public static Boolean isWebCamActive() {
        return preferences.getBoolean("webcam", false);
    }

    public static void setLocation(String location) {
        preferences.put("location", location);
    }

    public static String getLocation() {
        return preferences.get("location", "MainStore");
    }

    public static void setScales(String scales) {
        preferences.put("scales", scales);
    }

    public static String getScales() {
        return preferences.get("scales", "Not defined");
    }

    public static void setScalesSet2(String scales) {
        preferences.put("scalesset2", scales);
    }

    public static String getScalesSet2() {
        return preferences.get("scalesset2", "Not defined");
    }

    public static void setScanner(String scanner) {
        preferences.put("scanner", scanner);
    }

    public static String getScanner() {
        return preferences.get("scanner", "Not defined");
    }

    public static void setDisplay(String display) {
        preferences.put("display", display);
    }

    public static String getDisplay() {
        return preferences.get("display", "Not defined");
    }

    public static void enableCustomerDisplay(Boolean display) {
        preferences.putBoolean("customerdisplay", display);
    }

    public static Boolean hasCustomerDisplay() {
        return preferences.getBoolean("customerdisplay", false);
    }

    public static void setReceiptPrinter(String printer) {
        preferences.put("receiptprinter", printer);
    }

    public static String getReceiptPrinter() {
        return preferences.get("receiptprinter", "Not defined");
    }

    public static void setKitchenPrinter(String printer) {
        preferences.put("kitchenprinter", printer);
    }

    public static String getKitchenPrinter() {
        return preferences.get("kitchenprinter", "Not defined");
    }

    public static void setKitchenPrinter2(String printer) {
        preferences.put("kitchenprinter2", printer);
    }

    public static String getKitchenPrinter2() {
        return preferences.get("kitchenprinter2", "Not defined");
    }

    public static void setKitchenPrinter3(String printer) {
        preferences.put("kitchenprinter3", printer);
    }

    public static String getKitchenPrinter3() {
        return preferences.get("kitchenprinter3", "Not defined");
    }

    public static void setBarPrinter(String printer) {
        preferences.put("barprinter", printer);
    }

    public static String getBarPrinter() {
        return preferences.get("barprinter", "Not defined");
    }

    public static void setBarPrinter2(String printer) {
        preferences.put("barprinter2", printer);
    }

    public static String getBarPrinter2() {
        return preferences.get("barprinter2", "Not defined");
    }

    public static void setRemotePrinter(String printer) {
        preferences.put("remoteprinter", printer);
    }

    public static String getRemotePrinter() {
        return preferences.get("remoteprinter", "Not defined");
    }

    public static void setRemotePrinter2(String printer) {
        preferences.put("remoteprinter2", printer);
    }

    public static String getRemotePrinter2() {
        return preferences.get("remoteprinter2", "Not defined");
    }

    public static void setRemotePrinter3(String printer) {
        preferences.put("remoteprinter3", printer);
    }

    public static String getRemotePrinter3() {
        return preferences.get("remoteprinter3", "Not defined");
    }

    public static void setRemotePrinter4(String printer) {
        preferences.put("remoteprinter4", printer);
    }

    public static String getRemotePrinter4() {
        return preferences.get("remoteprinter4", "Not defined");
    }

    public static void setRemotePrinter5(String printer) {
        preferences.put("remoteprinter5", printer);
    }

    public static String getRemotePrinter5() {
        return preferences.get("remoteprinter5", "Not defined");
    }

    public static void setPosType(String posType) {
        preferences.put("postype", posType);
    }

    public static String getPosType() {
        return preferences.get("postype", "standard");
    }

    public static void enableTimer() {
        preferences.putBoolean("timer", true);
    }

    public static void setTimer(Boolean value) {
        preferences.putBoolean("timer", value);
    }

    public static Boolean isTimerActive() {
        return preferences.getBoolean("timer", false);
    }

    public static void setTimerPeriod(String period) {
        preferences.put("timerperiod", period);
    }

    public static String getTimerPeriod() {
        return preferences.get("timerperiod", "0");
    }

    public static String getReceiptPrefix() {
        return preferences.get("prefix", "");
    }

    public static void setReceiptPrefix(String prefix) {
        preferences.put("prefix", prefix);
    }

}
