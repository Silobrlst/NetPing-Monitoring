import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class SettingsLoader {
    private static final String ipAddress = "ipAddress";
    private static final String snmpPort = "snmpPort";
    private static final String snmpTrapsPort = "snmpTrapsPort";
    private static final String getIo1OID = "getIo1OID";
    private static final String snmpCommunity = "snmpCommunity";
    private static final String checkDelayJSONName = "checkDelay";
    private static final String trayIconJSONName = "trayIcon";
    private static final String styleJSONName = "style";
    private static final String gridRowsJSONName = "gridRows";
    private static final String gridColumnsJSONName = "gridCollumns";
    private static final String snmpGetRetries = "snmpGetRetries";
    private static final String snmpGetTimeout = "snmpGetTimeout";
    private static final String netPingsJSONName = "netPings";
    private static final String closedValue = "closedValue";
    private static final String openedValue = "openedValue";
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
    private static final String value0MessageJSONName = "value0Message";
    private static final String value1MessageJSONName = "value1Message";


    SettingsLoader() {
//        defaultParametersMap.put(ipAddress, "0.0.0.0");
//        defaultParametersMap.put(snmpPort, "161");
//        defaultParametersMap.put(snmpTrapsPort, "162");
//        defaultParametersMap.put(trapOID, "1.3.6.1.4.1.25728.8900.2.2.0");
//        defaultParametersMap.put(getIo1OID, "1.3.6.1.4.1.25728.8900.1.1.2.1");
//        defaultParametersMap.put(snmpCommunity, "SWITCH");
//        defaultParametersMap.put(checkDelayJSONName, 5);
//        defaultParametersMap.put(trayIconJSONName, false);
//        defaultParametersMap.put(styleJSONName, "Metal");
//        defaultParametersMap.put(gridRowsJSONName, 6);
//        defaultParametersMap.put(gridColumnsJSONName, 4);
//        defaultParametersMap.put(snmpGetRetries, 2);
//        defaultParametersMap.put(snmpGetTimeout, 2000);
//        defaultParametersMap.put(closedValue, 1);
//        defaultParametersMap.put(openedValue, 0);
    }


    //<validate settings>==================
    private static void validateDisplayMessage(JSONObject displayMessageJSONIn){
        if(displayMessageJSONIn.has(textColorJSONName)){
            displayMessageJSONIn.put(textColorJSONName, 0);
        }
        if(displayMessageJSONIn.has(backgroundColorJSONName)){
            displayMessageJSONIn.put(backgroundColorJSONName, 0);
        }
        if(displayMessageJSONIn.has(messageTextJSONName)){
            displayMessageJSONIn.put(messageTextJSONName, "");
        }
    }

    private static void validateIOLine(JSONObject ioLineJSONIn){
        if(ioLineJSONIn.has(nameJSONName)){
            ioLineJSONIn.put(nameJSONName, "");
        }
        if(ioLineJSONIn.has(value0MessageJSONName)){
            ioLineJSONIn.put(value0MessageJSONName, "");
        }
        if(ioLineJSONIn.has(value1MessageJSONName)){
            ioLineJSONIn.put(value1MessageJSONName, "");
        }

        validateDisplayMessage(ioLineJSONIn.getJSONObject(value0MessageJSONName));
        validateDisplayMessage(ioLineJSONIn.getJSONObject(value1MessageJSONName));
    }

    private static void validateNetPing(JSONObject netPingJSONIn){
        if(netPingJSONIn.has(nameJSONName)){
            netPingJSONIn.put(nameJSONName, "");
        }
        if(netPingJSONIn.has(line1JSONName)){
            netPingJSONIn.put(line1JSONName, new JSONObject());
        }
        if(netPingJSONIn.has(line2JSONName)){
            netPingJSONIn.put(line2JSONName, new JSONObject());
        }
        if(netPingJSONIn.has(line3JSONName)){
            netPingJSONIn.put(line3JSONName, new JSONObject());
        }
        if(netPingJSONIn.has(line4JSONName)){
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
        if(!settingsJSONIn.has(checkDelayJSONName)){
            settingsJSONIn.put(checkDelayJSONName, "60");
        }
        if(!settingsJSONIn.has(netPingsJSONName)){
            settingsJSONIn.put(netPingsJSONName, new JSONObject());
        }

        JSONObject netPingsJSON = settingsJSONIn.getJSONObject(netPingsJSONName);
        for(String ip: netPingsJSON.keySet()){
            validateNetPing(netPingsJSON.getJSONObject(ip));
        }
    }
    //</validate settings>=================


    public static Collection<NetPingWidget> loadNetPings(MainWindow mainWindowIn, JSONObject netPingsJSONIn){
        ArrayList<NetPingWidget> netPingWidgets = new ArrayList<>();
        netPingsJSONIn.keySet().forEach(ip -> netPingWidgets.add(loadNetPing(mainWindowIn, ip, netPingsJSONIn.getJSONObject(ip))));

        return netPingWidgets;
    }

    private static void loadIOLine(NetPingWidget netPingWidgetIn, String lineNumberIn, JSONObject ioLineJSONIn){
        IOLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        ioLineWidget.setName(ioLineJSONIn.getString(nameJSONName));
        ioLineWidget.setSnmpGetOID(ioLineJSONIn.getString(getOID));
        ioLineWidget.setTrapReceiveOID(ioLineJSONIn.getString(trapOID));
    }

    private static NetPingWidget loadNetPing(MainWindow mainWindowIn, String ipAddressIn, JSONObject netPingJSONIn){
        NetPingWidget netPingWidget = new NetPingWidget(mainWindowIn, ipAddressIn);
        netPingWidget.setDeviceName(netPingJSONIn.getString(nameJSONName));
        netPingWidget.setSnmpCommunity(netPingJSONIn.getString(snmpCommunity));
        netPingWidget.setSnmpPort(netPingJSONIn.getString(snmpPort));

        loadIOLine(netPingWidget, "1", netPingJSONIn.getJSONObject(line1JSONName));
        loadIOLine(netPingWidget, "2", netPingJSONIn.getJSONObject(line2JSONName));
        loadIOLine(netPingWidget, "3", netPingJSONIn.getJSONObject(line3JSONName));
        loadIOLine(netPingWidget, "4", netPingJSONIn.getJSONObject(line4JSONName));

        return netPingWidget;
    }


    public static void main(String[] args) {
        new SettingsLoader();

        JSONObject settingsJSON = JSONLoader.loadJSON(new File("settings.json"));
        validateSettings(settingsJSON);

        MainWindow mainWindow = new MainWindow();
        mainWindow.setStyle(settingsJSON.getString(styleJSONName));
        mainWindow.setTrayIconVisible(settingsJSON.getBoolean(trayIconJSONName));

        loadNetPings(mainWindow, settingsJSON.getJSONObject(netPingsJSONName));
    }

    //<netPing settings set>===============
    public void saveSettings(SettingsDialog settingsDialogIn){
        JSONObject settingsJSON = new JSONObject();
        settingsJSON.put(styleJSONName, settingsDialogIn.getStyle());
        settingsJSON.put(trayIconJSONName, settingsDialogIn.getTrayIconVisible());
        saveNetPings(settingsDialogIn.getNetPingWidgets(), settingsJSON);
    }
    public void saveNetPings(Collection<NetPingWidget> netPingWidgetsIn, JSONObject settingsJSONIn){
        JSONObject netPingsJSON = settingsJSONIn.getJSONObject(netPingsJSONName);
        netPingWidgetsIn.forEach(netPingWidget -> saveNetPing(netPingWidget, netPingsJSON));
    }
    public void saveNetPing(NetPingWidget netPingWidgetIn, JSONObject netPingsJSONIn) {
        JSONObject netPingJSON = new JSONObject();
        netPingsJSONIn.put(netPingWidgetIn.getIpAddress(), netPingJSON);

        netPingJSON.put(nameJSONName, netPingWidgetIn.getDeviceName());
        netPingJSON.put(snmpCommunity, netPingWidgetIn.getDeviceName());
        netPingJSON.put(snmpPort, netPingWidgetIn.getDeviceName());

        saveNetPingIOLine(netPingWidgetIn, "1", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "2", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "3", netPingJSON);
        saveNetPingIOLine(netPingWidgetIn, "4", netPingJSON);
    }
    private void saveNetPingIOLine(NetPingWidget netPingWidgetIn, String lineNumberIn, JSONObject netPingJSON){
        JSONObject lineJSON = netPingJSON.getJSONObject(lineNumberIn);

        IOLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        lineJSON.put(nameJSONName, ioLineWidget.getLineName());
        lineJSON.put(getOID, ioLineWidget.getSnmpGetOID());
        lineJSON.put(trapOID, ioLineWidget.getTrapReceiveOID());

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
    //</netPing settings set>==============
}

