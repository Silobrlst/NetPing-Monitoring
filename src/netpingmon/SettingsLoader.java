package netpingmon;

import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.util.*;

public class SettingsLoader {
    private static final String snmpPortJsonName = "snmpPort";
    private static final String snmpCommunityJsonName = "snmpCommunity";
    private static final String snmpTrapPortJsonName = "receiveTrapsPort";
    private static final String checkingDelayJsonName = "setCheckingDelay";
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
    private static final String activeJsonName = "active";
    private static final String value0MessageJsonName = "value0Message";
    private static final String value1MessageJsonName = "value1Message";
    private static final String connectedMessageJsonName = "connectedMessage";
    private static final String disconnectedMessageJsonName = "disconnectedMessage";
    private static final String linesGridTypeJsonName = "linesGridType";

    private static final File settingsFile = new File("settings.json");

    private SettingsLoader() {
    }

    public static void main(String[] args) {
        JSONObject settingsJSON = JsonLoader.loadJSON(settingsFile);
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
        mainWindow.setGridSize(settingsJSON.getInt(gridColumnsJsonName), settingsJSON.getInt(gridRowsJsonName));

        mainWindow.addNetPingWidgets(loadNetPings(mainWindow, settingsJSON.getJSONObject(netPingsJsonName)));
        mainWindow.startListen();
    }

    //<validate settings>===============================================================================================
    private static void validateJsonKey(JSONObject jsonIn, String nameIn, Object defaultIn){
        if (!jsonIn.has(nameIn)) {
            jsonIn.put(nameIn, defaultIn);
        }
    }
    private static void validateDisplayMessage(JSONObject displayMessageJSONIn){
        validateJsonKey(displayMessageJSONIn, textColorJsonName, 0xffffff);
        validateJsonKey(displayMessageJSONIn, backgroundColorJsonName, 0);
        validateJsonKey(displayMessageJSONIn, messageTextJsonName, "");
    }
    private static void validateIOLine(JSONObject ioLineJSONIn){
        validateJsonKey(ioLineJSONIn, nameJsonName, "");
        validateJsonKey(ioLineJSONIn, getOID, DefaultSettings.snmpGetLine1Oid);
        validateJsonKey(ioLineJSONIn, trapOID, DefaultSettings.snmpTrapPort);
        validateJsonKey(ioLineJSONIn, activeJsonName, true);
        validateJsonKey(ioLineJSONIn, value0MessageJsonName, new JSONObject());
        validateJsonKey(ioLineJSONIn, value1MessageJsonName, new JSONObject());

        validateDisplayMessage(ioLineJSONIn.getJSONObject(value0MessageJsonName));
        validateDisplayMessage(ioLineJSONIn.getJSONObject(value1MessageJsonName));
    }
    private static void validateNetPing(JSONObject netPingJSONIn){
        validateJsonKey(netPingJSONIn, nameJsonName, "");
        validateJsonKey(netPingJSONIn, snmpPortJsonName, DefaultSettings.snmpPort);
        validateJsonKey(netPingJSONIn, snmpCommunityJsonName, DefaultSettings.snmpCommunity);
        validateJsonKey(netPingJSONIn, connectedMessageJsonName, new JSONObject());
        validateJsonKey(netPingJSONIn, disconnectedMessageJsonName, new JSONObject());
        validateJsonKey(netPingJSONIn, linesGridTypeJsonName, DefaultSettings.linesGridType);
        validateJsonKey(netPingJSONIn, activeJsonName, DefaultSettings.active);
        validateJsonKey(netPingJSONIn, line1JsonName, new JSONObject());
        validateJsonKey(netPingJSONIn, line2JsonName, new JSONObject());
        validateJsonKey(netPingJSONIn, line3JsonName, new JSONObject());
        validateJsonKey(netPingJSONIn, line4JsonName, new JSONObject());

        validateDisplayMessage(netPingJSONIn.getJSONObject(connectedMessageJsonName));
        validateDisplayMessage(netPingJSONIn.getJSONObject(disconnectedMessageJsonName));

        validateIOLine(netPingJSONIn.getJSONObject(line1JsonName));
        validateIOLine(netPingJSONIn.getJSONObject(line2JsonName));
        validateIOLine(netPingJSONIn.getJSONObject(line3JsonName));
        validateIOLine(netPingJSONIn.getJSONObject(line4JsonName));
    }
    private static void validateSettings(JSONObject settingsJSONIn){
        validateJsonKey(settingsJSONIn, trayIconJsonName, DefaultSettings.trayIcon);
        validateJsonKey(settingsJSONIn, styleJsonName, DefaultSettings.style);
        validateJsonKey(settingsJSONIn, gridColumnsJsonName, DefaultSettings.gridNetPingsColumns);
        validateJsonKey(settingsJSONIn, gridRowsJsonName, DefaultSettings.gridNetPingsRows);
        validateJsonKey(settingsJSONIn, checkingDelayJsonName, DefaultSettings.checkingDelay);
        validateJsonKey(settingsJSONIn, timeOutJsonName, DefaultSettings.timeOut);
        validateJsonKey(settingsJSONIn, retriesJsonName, DefaultSettings.retries);
        validateJsonKey(settingsJSONIn, netPingsJsonName, new JSONObject());
        validateJsonKey(settingsJSONIn, snmpPortJsonName, DefaultSettings.snmpPort);
        validateJsonKey(settingsJSONIn, snmpTrapPortJsonName, DefaultSettings.snmpTrapPort);
        validateJsonKey(settingsJSONIn, snmpCommunityJsonName, DefaultSettings.snmpCommunity);

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
        IoLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        ioLineWidget.setLineName(ioLineJSONIn.getString(nameJsonName));
        ioLineWidget.setSnmpGetOID(ioLineJSONIn.getString(getOID));
        ioLineWidget.setTrapReceiveOID(ioLineJSONIn.getString(trapOID));

        loadDisplayMessage(ioLineWidget.getValue0Message(), ioLineJSONIn.getJSONObject(value0MessageJsonName));
        loadDisplayMessage(ioLineWidget.getValue1Message(), ioLineJSONIn.getJSONObject(value1MessageJsonName));

        ioLineWidget.applySettings();
        netPingWidgetIn.setLineActive(lineNumberIn, ioLineJSONIn.getBoolean(activeJsonName));
    }
    private static NetPingWidget loadNetPing(MainWindow mainWindowIn, String ipAddressIn, JSONObject netPingJsonIn){
        validateNetPing(netPingJsonIn);

        NetPingWidget netPingWidget = new NetPingWidget(mainWindowIn, ipAddressIn);
        netPingWidget.setDeviceName(netPingJsonIn.getString(nameJsonName));
        netPingWidget.setSnmpCommunity(netPingJsonIn.getString(snmpCommunityJsonName));
        netPingWidget.setSnmpPort(netPingJsonIn.getString(snmpPortJsonName));
        netPingWidget.setGridType(netPingJsonIn.getString(linesGridTypeJsonName));
        netPingWidget.setActive(netPingJsonIn.getBoolean(activeJsonName));

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
        settingsJSON.put(gridColumnsJsonName, settingsDialogIn.getGridColumns());
        settingsJSON.put(gridRowsJsonName, settingsDialogIn.getGridRows());
        saveNetPings(settingsDialogIn.getNetPingWidgets(), settingsJSON);

        JsonLoader.saveJSON(settingsFile, settingsJSON);
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
        netPingJSON.put(linesGridTypeJsonName, netPingWidgetIn.getGridType());
        netPingJSON.put(activeJsonName, netPingWidgetIn.isActive());

        JSONObject connectedMessageJson = new JSONObject();
        saveDisplayMessage(netPingWidgetIn.getConnectedMessage(), connectedMessageJson);
        netPingJSON.put(connectedMessageJsonName, connectedMessageJson);

        JSONObject disconnectedMessageJson = new JSONObject();
        saveDisplayMessage(netPingWidgetIn.getDisconnectedMessage(), disconnectedMessageJson);
        netPingJSON.put(disconnectedMessageJsonName, disconnectedMessageJson);

        saveIoLine(netPingWidgetIn, "1", netPingJSON);
        saveIoLine(netPingWidgetIn, "2", netPingJSON);
        saveIoLine(netPingWidgetIn, "3", netPingJSON);
        saveIoLine(netPingWidgetIn, "4", netPingJSON);
    }
    private void saveIoLine(NetPingWidget netPingWidgetIn, String lineNumberIn, JSONObject netPingJSON){
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

        IoLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        lineJSON.put(nameJsonName, ioLineWidget.getLineName());
        lineJSON.put(getOID, ioLineWidget.getSnmpGetOID());
        lineJSON.put(trapOID, ioLineWidget.getTrapReceiveOID());
        lineJSON.put(activeJsonName, ioLineWidget.isActive());

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

    void saveOnlyNetPing(NetPingWidget netPingWidgetIn){
        JSONObject settingsJson = JsonLoader.loadJSON(settingsFile);
        validateSettings(settingsJson);
        validateJsonKey(settingsJson.getJSONObject(netPingsJsonName), netPingWidgetIn.getIpAddress(), new JSONObject());
        saveNetPing(netPingWidgetIn, settingsJson.getJSONObject(netPingsJsonName));
        JsonLoader.saveJSON(settingsFile, settingsJson);
    }
    void saveOnlyIoLine(NetPingWidget netPingWidgetIn, IoLineWidget ioLineWidgetIn){
        JSONObject settingsJson = JsonLoader.loadJSON(settingsFile);
        validateSettings(settingsJson);

        JSONObject netPingJson = settingsJson.getJSONObject(netPingsJsonName).getJSONObject(netPingWidgetIn.getDeviceName());
        saveIoLine(netPingWidgetIn, ioLineWidgetIn.getLineNumber(), netPingJson);
        JsonLoader.saveJSON(settingsFile, settingsJson);
    }
    //</save settings>==================================================================================================
}

