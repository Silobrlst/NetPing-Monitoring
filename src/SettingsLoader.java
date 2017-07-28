import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.util.*;

public class SettingsLoader {
    private static final String snmpPortJsonName = "snmpPort";
    private static final String snmpCommunityJsonName = "snmpCommunity";
    private static final String snmpTrapPortJsonName = "receiveTrapsPort";
    private static final String checkingDelayJsonName = "checkingDelay";
    private static final String trayIconJSONName = "trayIcon";
    private static final String styleJSONName = "style";
    private static final String gridRowsJSONName = "gridRows";
    private static final String gridColumnsJSONName = "gridColumns";
    private static final String retriesJsonName = "retries";
    private static final String timeOutJsonName = "timeOut";
    private static final String netPingsJSONName = "netPings";
    private static final String nameJSONName = "name";
    private static final String getOID = "getOID";
    private static final String trapOID = "trapOID";
    private static final String messageTextJSONName = "messageText";
    private static final String textColorJSONName = "textColor";
    private static final String backgroundColorJSONName = "backgroundColor";
    private static final String line1JSONName = "line1";
    private static final String line2JSONName = "line2";
    private static final String line3JSONName = "line3";
    private static final String line4JSONName = "line4";
    private static final String lineActiveJSONName = "active";
    private static final String value0MessageJSONName = "value0Message";
    private static final String value1MessageJSONName = "value1Message";

    private static final File settingsFile = new File("settings.json");

    private SettingsLoader() {
    }

    //<validate settings>==================
    private static void validateDisplayMessage(JSONObject displayMessageJSONIn){
        if(!displayMessageJSONIn.has(textColorJSONName)){
            displayMessageJSONIn.put(textColorJSONName, 0);
        }
        if(!displayMessageJSONIn.has(backgroundColorJSONName)){
            displayMessageJSONIn.put(backgroundColorJSONName, 0);
        }
        if(!displayMessageJSONIn.has(messageTextJSONName)){
            displayMessageJSONIn.put(messageTextJSONName, "");
        }
    }
    private static void validateIOLine(JSONObject ioLineJSONIn){
        if(!ioLineJSONIn.has(nameJSONName)){
            ioLineJSONIn.put(nameJSONName, "");
        }
        if(!ioLineJSONIn.has(lineActiveJSONName)){
            ioLineJSONIn.put(lineActiveJSONName, true);
        }
        if(!ioLineJSONIn.has(value0MessageJSONName)){
            ioLineJSONIn.put(value0MessageJSONName, new JSONObject());
        }
        if(!ioLineJSONIn.has(value1MessageJSONName)){
            ioLineJSONIn.put(value1MessageJSONName, new JSONObject());
        }

        validateDisplayMessage(ioLineJSONIn.getJSONObject(value0MessageJSONName));
        validateDisplayMessage(ioLineJSONIn.getJSONObject(value1MessageJSONName));
    }
    private static void validateNetPing(JSONObject netPingJSONIn){
        if(!netPingJSONIn.has(nameJSONName)){
            netPingJSONIn.put(nameJSONName, "");
        }
        if(!netPingJSONIn.has(line1JSONName)){
            netPingJSONIn.put(line1JSONName, new JSONObject());
        }
        if(!netPingJSONIn.has(line2JSONName)){
            netPingJSONIn.put(line2JSONName, new JSONObject());
        }
        if(!netPingJSONIn.has(line3JSONName)){
            netPingJSONIn.put(line3JSONName, new JSONObject());
        }
        if(!netPingJSONIn.has(line4JSONName)){
            netPingJSONIn.put(line4JSONName, new JSONObject());
        }

        validateIOLine(netPingJSONIn.getJSONObject(line1JSONName));
        validateIOLine(netPingJSONIn.getJSONObject(line2JSONName));
        validateIOLine(netPingJSONIn.getJSONObject(line3JSONName));
        validateIOLine(netPingJSONIn.getJSONObject(line4JSONName));
    }
    private static void validateSettings(JSONObject settingsJSONIn){
        if(!settingsJSONIn.has(trayIconJSONName)){
            settingsJSONIn.put(trayIconJSONName, false);
        }
        if(!settingsJSONIn.has(styleJSONName)){
            settingsJSONIn.put(styleJSONName, "Metal");
        }
        if(!settingsJSONIn.has(gridColumnsJSONName)){
            settingsJSONIn.put(gridColumnsJSONName, "4");
        }
        if(!settingsJSONIn.has(gridRowsJSONName)){
            settingsJSONIn.put(gridRowsJSONName, "6");
        }
        if(!settingsJSONIn.has(checkingDelayJsonName)){
            settingsJSONIn.put(checkingDelayJsonName, "60");
        }
        if(!settingsJSONIn.has(timeOutJsonName)){
            settingsJSONIn.put(timeOutJsonName, "3");
        }
        if(!settingsJSONIn.has(retriesJsonName)){
            settingsJSONIn.put(retriesJsonName, "4");
        }
        if(!settingsJSONIn.has(netPingsJSONName)){
            settingsJSONIn.put(netPingsJSONName, new JSONObject());
        }
        if(!settingsJSONIn.has(snmpPortJsonName)){
            settingsJSONIn.put(snmpPortJsonName, "64123");
        }
        if(!settingsJSONIn.has(snmpTrapPortJsonName)){
            settingsJSONIn.put(snmpTrapPortJsonName, "162");
        }
        if(!settingsJSONIn.has(snmpCommunityJsonName)){
            settingsJSONIn.put(snmpCommunityJsonName, "SWITCH");
        }

        JSONObject netPingsJSON = settingsJSONIn.getJSONObject(netPingsJSONName);
        for(String ip: netPingsJSON.keySet()){
            validateNetPing(netPingsJSON.getJSONObject(ip));
        }
    }
    //</validate settings>=================

    //<load settings>======================
    private static Collection<NetPingWidget> loadNetPings(MainWindow mainWindowIn, JSONObject netPingsJSONIn){
        ArrayList<NetPingWidget> netPingWidgets = new ArrayList<>();
        netPingsJSONIn.keySet().forEach(ip -> netPingWidgets.add(loadNetPing(mainWindowIn, ip, netPingsJSONIn.getJSONObject(ip))));

        return netPingWidgets;
    }
    private static DisplayMessageSettings loadDisplayMessageSettings(JSONObject displayMessageSettingsJsonIn){
        DisplayMessageSettings displayMessageSettings = new DisplayMessageSettings();
        displayMessageSettings.messageText = displayMessageSettingsJsonIn.getString(messageTextJSONName);
        displayMessageSettings.textColor = new Color(displayMessageSettingsJsonIn.getInt(textColorJSONName));
        displayMessageSettings.backgroundColor = new Color(displayMessageSettingsJsonIn.getInt(backgroundColorJSONName));
        return displayMessageSettings;
    }
    private static void loadIOLine(NetPingWidget netPingWidgetIn, String lineNumberIn, JSONObject ioLineJSONIn){
        IOLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        ioLineWidget.setLineName(ioLineJSONIn.getString(nameJSONName));
        ioLineWidget.setSnmpGetOID(ioLineJSONIn.getString(getOID));
        ioLineWidget.setTrapReceiveOID(ioLineJSONIn.getString(trapOID));
        netPingWidgetIn.setLineActive(lineNumberIn, ioLineJSONIn.getBoolean(lineActiveJSONName));
        ioLineWidget.setValue0Message(loadDisplayMessageSettings(ioLineJSONIn.getJSONObject(value0MessageJSONName)));
        ioLineWidget.setValue1Message(loadDisplayMessageSettings(ioLineJSONIn.getJSONObject(value1MessageJSONName)));
    }
    private static NetPingWidget loadNetPing(MainWindow mainWindowIn, String ipAddressIn, JSONObject netPingJSONIn){
        validateNetPing(netPingJSONIn);

        NetPingWidget netPingWidget = new NetPingWidget(mainWindowIn, ipAddressIn);
        netPingWidget.setDeviceName(netPingJSONIn.getString(nameJSONName));
        netPingWidget.setSnmpCommunity(netPingJSONIn.getString(snmpCommunityJsonName));
        netPingWidget.setSnmpPort(netPingJSONIn.getString(snmpPortJsonName));

        loadIOLine(netPingWidget, "1", netPingJSONIn.getJSONObject(line1JSONName));
        loadIOLine(netPingWidget, "2", netPingJSONIn.getJSONObject(line2JSONName));
        loadIOLine(netPingWidget, "3", netPingJSONIn.getJSONObject(line3JSONName));
        loadIOLine(netPingWidget, "4", netPingJSONIn.getJSONObject(line4JSONName));

        netPingWidget.applySettings();

        return netPingWidget;
    }
    //</load settings>=====================

    public static void main(String[] args) {
        JSONObject settingsJSON = JSONLoader.loadJSON(settingsFile);
        validateSettings(settingsJSON);

        Integer snmpTrapPort = settingsJSON.getInt(snmpTrapPortJsonName);
        Integer snmpPort = settingsJSON.getInt(snmpPortJsonName);

        MainWindow mainWindow = new MainWindow(new SettingsLoader(), snmpTrapPort, snmpPort);
        mainWindow.setStyle(settingsJSON.getString(styleJSONName));
        mainWindow.setTrayIconVisible(settingsJSON.getBoolean(trayIconJSONName));
        mainWindow.setSnmpCommunity(settingsJSON.getString(snmpCommunityJsonName));
        mainWindow.setCheckingDelay(settingsJSON.getInt(checkingDelayJsonName));
        mainWindow.setRetries(settingsJSON.getInt(retriesJsonName));
        mainWindow.setTimeOut(settingsJSON.getInt(timeOutJsonName));

        mainWindow.addNetPingWidgets(loadNetPings(mainWindow, settingsJSON.getJSONObject(netPingsJSONName)));
        mainWindow.startListen();
    }

    //<save settings>======================
    public void saveSettings(SettingsDialog settingsDialogIn){
        JSONObject settingsJSON = new JSONObject();
        validateSettings(settingsJSON);

        settingsJSON.put(styleJSONName, settingsDialogIn.getStyle());
        settingsJSON.put(trayIconJSONName, settingsDialogIn.getTrayIconVisible());
        settingsJSON.put(snmpPortJsonName, settingsDialogIn.getSnmpPort());
        settingsJSON.put(snmpCommunityJsonName, settingsDialogIn.getSnmpCommunity());
        settingsJSON.put(snmpTrapPortJsonName, settingsDialogIn.getSnmpTrapPort());
        settingsJSON.put(snmpPortJsonName, settingsDialogIn.getSnmpPort());
        settingsJSON.put(checkingDelayJsonName, settingsDialogIn.getCheckingDelay());
        settingsJSON.put(retriesJsonName, settingsDialogIn.getRetries());
        settingsJSON.put(timeOutJsonName, settingsDialogIn.getTimeOut());
        saveNetPings(settingsDialogIn.getNetPingWidgets(), settingsJSON);

        JSONLoader.saveJSON(settingsFile, settingsJSON);
    }
    private void saveNetPings(Collection<NetPingWidget> netPingWidgetsIn, JSONObject settingsJSONIn){
        JSONObject netPingsJSON = settingsJSONIn.getJSONObject(netPingsJSONName);
        netPingWidgetsIn.forEach(netPingWidget -> saveNetPing(netPingWidget, netPingsJSON));
    }
    private void saveNetPing(NetPingWidget netPingWidgetIn, JSONObject netPingsJSONIn) {
        JSONObject netPingJSON = new JSONObject();
        validateNetPing(netPingJSON);

        netPingsJSONIn.put(netPingWidgetIn.getIpAddress(), netPingJSON);

        netPingJSON.put(nameJSONName, netPingWidgetIn.getDeviceName());
        netPingJSON.put(snmpCommunityJsonName, netPingWidgetIn.getSnmpCommunity());
        netPingJSON.put(snmpPortJsonName, netPingWidgetIn.getSnmpPort());

        saveNetPingIOLine(netPingWidgetIn, "1", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "2", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "3", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "4", netPingJSON);
    }
    private void saveNetPingIOLine(NetPingWidget netPingWidgetIn, String lineNumberIn, JSONObject netPingJSON){
        JSONObject lineJSON;

        switch(lineNumberIn){
            case "1":
                lineJSON = netPingJSON.getJSONObject(line1JSONName);
                break;
            case "2":
                lineJSON = netPingJSON.getJSONObject(line2JSONName);
                break;
            case "3":
                lineJSON = netPingJSON.getJSONObject(line3JSONName);
                break;
            case "4":
                lineJSON = netPingJSON.getJSONObject(line4JSONName);
                break;
            default:
                return;
        }

        IOLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        lineJSON.put(nameJSONName, ioLineWidget.getLineName());
        lineJSON.put(getOID, ioLineWidget.getSnmpGetOID());
        lineJSON.put(trapOID, ioLineWidget.getTrapReceiveOID());
        lineJSON.put(lineActiveJSONName, ioLineWidget.getActive());

        JSONObject value0MessageJSON = new JSONObject();
        lineJSON.put(value0MessageJSONName, value0MessageJSON);

        JSONObject value1MessageJSON = new JSONObject();
        lineJSON.put(value1MessageJSONName, value1MessageJSON);

        saveNetPingIOLineDisplayMessageSettings(netPingWidgetIn.getLine(lineNumberIn).getValue0Message(), value0MessageJSON);
        saveNetPingIOLineDisplayMessageSettings(netPingWidgetIn.getLine(lineNumberIn).getValue1Message(), value1MessageJSON);
    }
    private void saveNetPingIOLineDisplayMessageSettings(DisplayMessageSettings displayMessageSettingsIn, JSONObject displayMessageJSONIn){
        displayMessageJSONIn.put(messageTextJSONName, displayMessageSettingsIn.messageText);
        displayMessageJSONIn.put(textColorJSONName, displayMessageSettingsIn.textColor.getRGB());
        displayMessageJSONIn.put(backgroundColorJSONName, displayMessageSettingsIn.backgroundColor.getRGB());
    }
    //</save settings>=====================
}

