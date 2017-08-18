package netpingmon;

import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.util.*;

public class SettingsLoader {
    private static final String snmpPortJsonName = "snmpPort";
    private static final String snmpCommunityJsonName = "snmpCommunity";
    private static final String snmpTrapPortJsonName = "receiveTrapsPort";
    private static final String checkingDelayJsonName = "checkingDelay";
    private static final String trayIconJsonName = "trayIcon";
    private static final String styleJsonName = "style";
    private static final String gridRowsJsonName = "gridRows";
    private static final String gridColumnsJsonName = "gridColumns";
    private static final String retriesJsonName = "retries";
    private static final String timeOutJsonName = "timeOut";
    private static final String netPingsJsonName = "netPings";
    private static final String nameJsonName = "name";
    private static final String getOID = "getOID";
    private static final String trapOID = "trapOID";
    private static final String messageTextJsonName = "messageText";
    private static final String textColorJsonName = "textColor";
    private static final String backgroundColorJsonName = "backgroundColor";
    private static final String line1JsonName = "line1";
    private static final String line2JsonName = "line2";
    private static final String line3JsonName = "line3";
    private static final String line4JsonName = "line4";
    private static final String lineActiveJsonName = "active";
    private static final String value0MessageJsonName = "value0Message";
    private static final String value1MessageJsonName = "value1Message";
    private static final String connectedMessageJsonName = "connectedMessage";
    private static final String disconnectedMessageJsonName = "disconnectedMessage";

    private static final File settingsFile = new File("settings.json");

    private SettingsLoader() {
    }

    public static void main(String[] args) {
        JSONObject settingsJSON = JSONLoader.loadJSON(settingsFile);
        validateSettings(settingsJSON);

        Integer snmpTrapPort = settingsJSON.getInt(snmpTrapPortJsonName);
        Integer snmpPort = settingsJSON.getInt(snmpPortJsonName);

        MainWindow mainWindow = new MainWindow(new SettingsLoader(), snmpTrapPort, snmpPort);
        mainWindow.setStyle(settingsJSON.getString(styleJsonName));
        mainWindow.setTrayIconVisible(settingsJSON.getBoolean(trayIconJsonName));
        mainWindow.setSnmpCommunity(settingsJSON.getString(snmpCommunityJsonName));
        mainWindow.setCheckingDelay(settingsJSON.getInt(checkingDelayJsonName));
        mainWindow.setRetries(settingsJSON.getInt(retriesJsonName));
        mainWindow.setTimeOut(settingsJSON.getInt(timeOutJsonName));

        mainWindow.addNetPingWidgets(loadNetPings(mainWindow, settingsJSON.getJSONObject(netPingsJsonName)));
        mainWindow.startListen();
    }

    //<validate settings>===============================================================================================
    private static void validateDisplayMessage(JSONObject displayMessageJSONIn){
        if(!displayMessageJSONIn.has(textColorJsonName)){
            displayMessageJSONIn.put(textColorJsonName, 0xffffff);
        }
        if(!displayMessageJSONIn.has(backgroundColorJsonName)){
            displayMessageJSONIn.put(backgroundColorJsonName, 0);
        }
        if(!displayMessageJSONIn.has(messageTextJsonName)){
            displayMessageJSONIn.put(messageTextJsonName, "");
        }
    }
    private static void validateIOLine(JSONObject ioLineJSONIn){
        if(!ioLineJSONIn.has(nameJsonName)){
            ioLineJSONIn.put(nameJsonName, "");
        }
        if(!ioLineJSONIn.has(lineActiveJsonName)){
            ioLineJSONIn.put(lineActiveJsonName, true);
        }
        if(!ioLineJSONIn.has(value0MessageJsonName)){
            ioLineJSONIn.put(value0MessageJsonName, new JSONObject());
        }
        if(!ioLineJSONIn.has(value1MessageJsonName)){
            ioLineJSONIn.put(value1MessageJsonName, new JSONObject());
        }

        validateDisplayMessage(ioLineJSONIn.getJSONObject(value0MessageJsonName));
        validateDisplayMessage(ioLineJSONIn.getJSONObject(value1MessageJsonName));
    }
    private static void validateNetPing(JSONObject netPingJSONIn){
        if(!netPingJSONIn.has(nameJsonName)){
            netPingJSONIn.put(nameJsonName, "");
        }
        if(!netPingJSONIn.has(connectedMessageJsonName)){
            netPingJSONIn.put(connectedMessageJsonName, new JSONObject());
        }
        if(!netPingJSONIn.has(disconnectedMessageJsonName)){
            netPingJSONIn.put(disconnectedMessageJsonName, new JSONObject());
        }
        if(!netPingJSONIn.has(line1JsonName)){
            netPingJSONIn.put(line1JsonName, new JSONObject());
        }
        if(!netPingJSONIn.has(line2JsonName)){
            netPingJSONIn.put(line2JsonName, new JSONObject());
        }
        if(!netPingJSONIn.has(line3JsonName)){
            netPingJSONIn.put(line3JsonName, new JSONObject());
        }
        if(!netPingJSONIn.has(line4JsonName)){
            netPingJSONIn.put(line4JsonName, new JSONObject());
        }

        validateDisplayMessage(netPingJSONIn.getJSONObject(connectedMessageJsonName));
        validateDisplayMessage(netPingJSONIn.getJSONObject(disconnectedMessageJsonName));

        validateIOLine(netPingJSONIn.getJSONObject(line1JsonName));
        validateIOLine(netPingJSONIn.getJSONObject(line2JsonName));
        validateIOLine(netPingJSONIn.getJSONObject(line3JsonName));
        validateIOLine(netPingJSONIn.getJSONObject(line4JsonName));
    }
    private static void validateSettings(JSONObject settingsJSONIn){
        if(!settingsJSONIn.has(trayIconJsonName)){
            settingsJSONIn.put(trayIconJsonName, false);
        }
        if(!settingsJSONIn.has(styleJsonName)){
            settingsJSONIn.put(styleJsonName, "Metal");
        }
        if(!settingsJSONIn.has(gridColumnsJsonName)){
            settingsJSONIn.put(gridColumnsJsonName, "4");
        }
        if(!settingsJSONIn.has(gridRowsJsonName)){
            settingsJSONIn.put(gridRowsJsonName, "6");
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
        if(!settingsJSONIn.has(netPingsJsonName)){
            settingsJSONIn.put(netPingsJsonName, new JSONObject());
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

        JSONObject netPingsJSON = settingsJSONIn.getJSONObject(netPingsJsonName);
        for(String ip: netPingsJSON.keySet()){
            validateNetPing(netPingsJSON.getJSONObject(ip));
        }
    }
    //</validate settings>==============================================================================================

    //<load settings>===================================================================================================
    private static Collection<NetPingWidget> loadNetPings(MainWindow mainWindowIn, JSONObject netPingsJSONIn){
        ArrayList<NetPingWidget> netPingWidgets = new ArrayList<>();
        netPingsJSONIn.keySet().forEach(ip -> netPingWidgets.add(loadNetPing(mainWindowIn, ip, netPingsJSONIn.getJSONObject(ip))));

        return netPingWidgets;
    }
    private static void loadDisplayMessage(DisplayMessage displayMessageIn, JSONObject displayMessageJsonIn){
        displayMessageIn.setMessageText(displayMessageJsonIn.getString(messageTextJsonName));
        displayMessageIn.setTextColor(new Color(displayMessageJsonIn.getInt(textColorJsonName)));
        displayMessageIn.setBackgroundColor(new Color(displayMessageJsonIn.getInt(backgroundColorJsonName)));
        displayMessageIn.applySettings();
    }
    private static void loadIOLine(NetPingWidget netPingWidgetIn, String lineNumberIn, JSONObject ioLineJSONIn){
        IOLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        ioLineWidget.setLineName(ioLineJSONIn.getString(nameJsonName));
        ioLineWidget.setSnmpGetOID(ioLineJSONIn.getString(getOID));
        ioLineWidget.setTrapReceiveOID(ioLineJSONIn.getString(trapOID));

        loadDisplayMessage(ioLineWidget.getValue0Message(), ioLineJSONIn.getJSONObject(value0MessageJsonName));
        loadDisplayMessage(ioLineWidget.getValue1Message(), ioLineJSONIn.getJSONObject(value1MessageJsonName));

        ioLineWidget.applySettings();
        netPingWidgetIn.setLineActive(lineNumberIn, ioLineJSONIn.getBoolean(lineActiveJsonName));
    }
    private static NetPingWidget loadNetPing(MainWindow mainWindowIn, String ipAddressIn, JSONObject netPingJsonIn){
        validateNetPing(netPingJsonIn);

        NetPingWidget netPingWidget = new NetPingWidget(mainWindowIn, ipAddressIn);
        netPingWidget.setDeviceName(netPingJsonIn.getString(nameJsonName));
        netPingWidget.setSnmpCommunity(netPingJsonIn.getString(snmpCommunityJsonName));
        netPingWidget.setSnmpPort(netPingJsonIn.getString(snmpPortJsonName));

        loadDisplayMessage(netPingWidget.getConnectedMessage(), netPingJsonIn.getJSONObject(connectedMessageJsonName));
        loadDisplayMessage(netPingWidget.getDisconnectedMessage(), netPingJsonIn.getJSONObject(disconnectedMessageJsonName));

        loadIOLine(netPingWidget, "1", netPingJsonIn.getJSONObject(line1JsonName));
        loadIOLine(netPingWidget, "2", netPingJsonIn.getJSONObject(line2JsonName));
        loadIOLine(netPingWidget, "3", netPingJsonIn.getJSONObject(line3JsonName));
        loadIOLine(netPingWidget, "4", netPingJsonIn.getJSONObject(line4JsonName));

        netPingWidget.applySettings();

        return netPingWidget;
    }
    //</load settings>==================================================================================================

    //<save settings>===================================================================================================
    void saveSettings(SettingsDialog settingsDialogIn){
        JSONObject settingsJSON = new JSONObject();
        validateSettings(settingsJSON);

        settingsJSON.put(styleJsonName, settingsDialogIn.getStyle());
        settingsJSON.put(trayIconJsonName, settingsDialogIn.getTrayIconVisible());
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
        JSONObject netPingsJSON = settingsJSONIn.getJSONObject(netPingsJsonName);
        netPingWidgetsIn.forEach(netPingWidget -> saveNetPing(netPingWidget, netPingsJSON));
    }
    private void saveNetPing(NetPingWidget netPingWidgetIn, JSONObject netPingsJSONIn) {
        JSONObject netPingJSON = new JSONObject();
        validateNetPing(netPingJSON);

        netPingsJSONIn.put(netPingWidgetIn.getIpAddress(), netPingJSON);

        netPingJSON.put(nameJsonName, netPingWidgetIn.getDeviceName());
        netPingJSON.put(snmpCommunityJsonName, netPingWidgetIn.getSnmpCommunity());
        netPingJSON.put(snmpPortJsonName, netPingWidgetIn.getSnmpPort());

        JSONObject connectedMessageJson = new JSONObject();
        saveDisplayMessage(netPingWidgetIn.getConnectedMessage(), connectedMessageJson);
        netPingJSON.put(connectedMessageJsonName, connectedMessageJson);

        JSONObject disconnectedMessageJson = new JSONObject();
        saveDisplayMessage(netPingWidgetIn.getDisconnectedMessage(), disconnectedMessageJson);
        netPingJSON.put(disconnectedMessageJsonName, disconnectedMessageJson);

        saveNetPingIOLine(netPingWidgetIn, "1", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "2", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "3", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "4", netPingJSON);
    }
    private void saveNetPingIOLine(NetPingWidget netPingWidgetIn, String lineNumberIn, JSONObject netPingJSON){
        JSONObject lineJSON;

        switch(lineNumberIn){
            case "1":
                lineJSON = netPingJSON.getJSONObject(line1JsonName);
                break;
            case "2":
                lineJSON = netPingJSON.getJSONObject(line2JsonName);
                break;
            case "3":
                lineJSON = netPingJSON.getJSONObject(line3JsonName);
                break;
            case "4":
                lineJSON = netPingJSON.getJSONObject(line4JsonName);
                break;
            default:
                return;
        }

        IOLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        lineJSON.put(nameJsonName, ioLineWidget.getLineName());
        lineJSON.put(getOID, ioLineWidget.getSnmpGetOID());
        lineJSON.put(trapOID, ioLineWidget.getTrapReceiveOID());
        lineJSON.put(lineActiveJsonName, ioLineWidget.getActive());

        JSONObject value0MessageJSON = new JSONObject();
        saveDisplayMessage(netPingWidgetIn.getLine(lineNumberIn).getValue0Message(), value0MessageJSON);
        lineJSON.put(value0MessageJsonName, value0MessageJSON);

        JSONObject value1MessageJSON = new JSONObject();
        saveDisplayMessage(netPingWidgetIn.getLine(lineNumberIn).getValue1Message(), value1MessageJSON);
        lineJSON.put(value1MessageJsonName, value1MessageJSON);
    }
    private void saveDisplayMessage(DisplayMessage displayMessageIn, JSONObject displayMessageJsonIn){
        displayMessageJsonIn.put(messageTextJsonName, displayMessageIn.getMessageText());
        displayMessageJsonIn.put(textColorJsonName, displayMessageIn.getTextColor().getRGB());
        displayMessageJsonIn.put(backgroundColorJsonName, displayMessageIn.getBackgroundColor().getRGB());
    }
    //</save settings>==================================================================================================
}

