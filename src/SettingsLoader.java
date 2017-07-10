import org.json.JSONObject;
import org.snmp4j.smi.OID;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsLoader {
    private String configFileName;
    private JSONObject config;
    private JSONObject netPingsJSON;

    private Map<String, Object> defaultParametersMap;
    private static final String ipAddress = "ipAddress";
    private static final String snmpPort = "snmpPort";
    private static final String snmpTrapsPort = "snmpTrapsPort";
    private static final String getIo1OID = "getIo1OID";
    private static final String snmpCommunity = "snmpCommunity";
    private static final String checkDelay = "checkDelay";
    private static final String trayIcon = "trayIcon";
    private static final String style = "style";
    private static final String gridRows = "gridRows";
    private static final String gridCollumns = "gridCollumns";
    private static final String snmpGetRetries = "snmpGetRetries";
    private static final String snmpGetTimeout = "snmpGetTimeout";
    private static final String netpings = "netpings";
    private static final String closedValue = "closedValue";
    private static final String openedValue = "openedValue";
    private static final String name = "name";
    private static final String getOID = "getOID";
    private static final String trapOID = "trapOID";
    private static final String messageText = "messageText";
    private static final String textColor = "textColor";
    private static final String backgroundColor = "backgroundColor";


    SettingsLoader(String configFileNameIn) {
        defaultParametersMap = new HashMap<>();
        configFileName = configFileNameIn;

        defaultParametersMap.put(ipAddress, "0.0.0.0");
        defaultParametersMap.put(snmpPort, "161");
        defaultParametersMap.put(snmpTrapsPort, "162");
        defaultParametersMap.put(trapOID, "1.3.6.1.4.1.25728.8900.2.2.0");
        defaultParametersMap.put(getIo1OID, "1.3.6.1.4.1.25728.8900.1.1.2.1");
        defaultParametersMap.put(snmpCommunity, "SWITCH");
        defaultParametersMap.put(checkDelay, 5);
        defaultParametersMap.put(trayIcon, false);
        defaultParametersMap.put(style, "Metal");
        defaultParametersMap.put(gridRows, 6);
        defaultParametersMap.put(gridCollumns, 4);
        defaultParametersMap.put(snmpGetRetries, 2);
        defaultParametersMap.put(snmpGetTimeout, 2000);
        defaultParametersMap.put(closedValue, 1);
        defaultParametersMap.put(openedValue, 0);

        loadConfig();
    }

    private String loadDataFromfile(String fileNameIn) {
        try {
            File file = new File(fileNameIn);
            file.createNewFile();

            byte[] encoded = Files.readAllBytes(Paths.get(fileNameIn));
            return new String(encoded, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void saveDataToFile(String fileNameIn, String dataIn) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(fileNameIn);
            bw = new BufferedWriter(fw);
            bw.write(dataIn);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadConfig() {
        String data = loadDataFromfile(configFileName);

        JSONObject json;
        if (data.isEmpty()) {
            json = new JSONObject();
        } else {
            json = new JSONObject(data);
        }

        boolean saveFile = false;

        if (!json.has(netpings)) {
            json.put(netpings, new JSONObject());
            saveFile = true;
        }

        for (String parameter : defaultParametersMap.keySet()) {
            if (!json.has(parameter)) {
                json.put(parameter, defaultParametersMap.get(parameter));
                saveFile = true;
            }
        }

        config = json;
        netPingsJSON = config.getJSONObject(netpings);

        if (saveFile) {
            saveConfig();
        }
    }

    public void saveConfig() {
        saveDataToFile(configFileName, config.toString(5));
    }

    public void loadDefaultConfig() {
        JSONObject json = new JSONObject();

        for (String parameter : defaultParametersMap.keySet()) {
            json.put(parameter, defaultParametersMap.get(parameter));
        }

        config = json;
    }

    public SnmpSettings getSnmpSettings() {
        SnmpSettings settings = new SnmpSettings();
        settings.community = config.getString(snmpCommunity);
        settings.getIo1OID = config.getString(getIo1OID);
        settings.ipAddress = config.getString(ipAddress);
        settings.snmpPort = config.getString(snmpPort);
        settings.snmpTrapsPort = config.getString(snmpTrapsPort);
        settings.trapOID = config.getString(trapOID);
        return settings;
    }

    public void setSnmpSettings(SnmpSettings snmpSettingsIn) {
        if (snmpSettingsIn.community != null) {
            config.put(snmpCommunity, snmpSettingsIn.community);
        }
        if (snmpSettingsIn.getIo1OID != null) {
            config.put(getIo1OID, snmpSettingsIn.getIo1OID);
        }
        if (snmpSettingsIn.ipAddress != null) {
            config.put(ipAddress, snmpSettingsIn.ipAddress);
        }
        if (snmpSettingsIn.snmpPort != null) {
            config.put(snmpPort, snmpSettingsIn.snmpPort);
        }
        if (snmpSettingsIn.snmpTrapsPort != null) {
            config.put(snmpTrapsPort, snmpSettingsIn.snmpTrapsPort);
        }
        if (snmpSettingsIn.trapOID != null) {
            config.put(trapOID, snmpSettingsIn.trapOID);
        }
    }


    public List<String> getNetPingIpAddresses(){
        ArrayList<String> ipAddresses = new ArrayList<>();

        for (String ip : netPingsJSON.keySet()) {
            ipAddresses.add(ip);
        }

        return ipAddresses;
    }


    public void removeNetping(String ipAddressIn) {
        JSONObject netpingsJSON = config.getJSONObject(netpings);
        netpingsJSON.remove(ipAddressIn);
        config.remove(ipAddressIn);
    }


    public void loadIOLine(NetPingWidget netPingWidgetIn, String lineNumberIn){
        JSONObject netPingJSON;

        if(netPingsJSON.has(netPingWidgetIn.getIpAddress())){
            netPingJSON = netPingsJSON.getJSONObject(netPingWidgetIn.getIpAddress());

            JSONObject lineJSON = netPingJSON.getJSONObject(lineNumberIn);
            IOLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
            ioLineWidget.setName(lineJSON.getString(name));
            ioLineWidget.setSnmpGetOID(lineJSON.getString(getOID));
            ioLineWidget.setTrapReceiveOID(lineJSON.getString(trapOID));
        }
    }

    public NetPingWidget loadNetPing(MainWindow mainWindowIn, String ipAddressIn){
        JSONObject netPingJSON;

        if(netPingsJSON.has(ipAddressIn)){
            netPingJSON = netPingsJSON.getJSONObject(ipAddressIn);

            NetPingWidget netPingWidget = new NetPingWidget(mainWindowIn, ipAddressIn);
            netPingWidget.setDeviceName(netPingJSON.getString(name));
            netPingWidget.setSnmpCommunity(netPingJSON.getString(snmpCommunity));
            netPingWidget.setSnmpPort(netPingJSON.getString(snmpPort));

            loadIOLine(netPingWidget, "1");
            loadIOLine(netPingWidget, "2");
            loadIOLine(netPingWidget, "3");
            loadIOLine(netPingWidget, "4");
        }

        return null;
    }

    public NetPingWidget newNetPing(MainWindow mainWindowIn, String ipAddressIn){
        NetPingWidget netPingWidget = new NetPingWidget(mainWindowIn, ipAddressIn);

        //<init lines>=======================
        setNetPingIOLine(netPingWidget, "1");
        setNetPingIOLine(netPingWidget, "2");
        setNetPingIOLine(netPingWidget, "3");
        setNetPingIOLine(netPingWidget, "4");
        //</init lines>======================

        setNetPing(ipAddressIn, netPingWidget);

        return netPingWidget;
    }


    public boolean isTrayIcon() {
        return config.getBoolean(trayIcon);
    }

    public boolean isNetpingExists(String ipAddressIn) {
        return config.getJSONObject(netpings).has(ipAddressIn);
    }


    //<netPing settings set>===============
    public void setNetPingIpAddress(String oldIpAddressIn, String newIpAddressIn) {
        if(netPingsJSON.has(oldIpAddressIn)){
            JSONObject netpingJSON = netPingsJSON.getJSONObject(oldIpAddressIn);
            netPingsJSON.remove(oldIpAddressIn);
            netPingsJSON.put(newIpAddressIn, netpingJSON);
        }
    }
    public void setNetPing(String ipAddressIn, NetPingWidget netPingWidgetIn) {
        JSONObject netpingJSON;

        if(netPingsJSON.has(ipAddressIn)){
            netpingJSON = netPingsJSON.getJSONObject(ipAddressIn);
        }else{
            netpingJSON = new JSONObject();
            netPingsJSON.put(ipAddressIn, netpingJSON);
        }

        netpingJSON.put(name, netPingWidgetIn.getDeviceName());
        netpingJSON.put(snmpCommunity, netPingWidgetIn.getDeviceName());
        netpingJSON.put(snmpPort, netPingWidgetIn.getDeviceName());
    }
    public void setNetPingIOLine(NetPingWidget netPingWidgetIn, String lineNumberIn){
        JSONObject netPingJSON = netPingsJSON.getJSONObject(netPingWidgetIn.getIpAddress());
        JSONObject lineJSON = netPingJSON.getJSONObject(lineNumberIn);

        IOLineWidget ioLineWidget = netPingWidgetIn.getLine(lineNumberIn);
        lineJSON.put(name, ioLineWidget.getLineName());
        lineJSON.put(getOID, ioLineWidget.getSnmpGetOID());
        lineJSON.put(trapOID, ioLineWidget.getTrapReceiveOID());

        setNetPingIOLineDisplayMessageSettings(netPingWidgetIn.getIpAddress(), lineNumberIn, ioLineWidget.getValue0Message());
        setNetPingIOLineDisplayMessageSettings(netPingWidgetIn.getIpAddress(), lineNumberIn, ioLineWidget.getValue1Message());
    }
    public void setNetPingIOLineDisplayMessageSettings(String ipAddressIn, String lineNumberIn, DisplayMessageSettings displayMessageSettingsIn){
        JSONObject netpingJSON = netPingsJSON.getJSONObject(ipAddressIn);
        JSONObject lineJSON = netpingJSON.getJSONObject(lineNumberIn);
        lineJSON.put(messageText, displayMessageSettingsIn.messageText);
        lineJSON.put(textColor, displayMessageSettingsIn.textColor.getRGB());
        lineJSON.put(backgroundColor, displayMessageSettingsIn.backgroundColor.getRGB());
    }
    //</netPing settings set>==============

    //<application settings set>===========
    public void setCheckTime(int checkTimeIn) {
        config.put(checkDelay, checkTimeIn);
    }
    public void setTrayIconVisible(boolean visibleIn) {
        config.put(trayIcon, visibleIn);
    }
    public void setStyle(String styleNameIn) {
        config.put(style, styleNameIn);
    }
    //</application settings set>==========

    //<application settings get>===========
    public int getCheckingDelay() {
        return config.getInt(checkDelay);
    }
    public String getStyle() {
        return config.getString(style);
    }
    public int getGridRows() {
        return config.getInt(gridRows);
    }
    public int getGridCollumns() {
        return config.getInt(gridCollumns);
    }
    public int getSnmpGetRetries() {
        return config.getInt(snmpGetRetries);
    }
    public int getSnmpGetTimeout() {
        return config.getInt(snmpGetTimeout);
    }
    //</application settings get>==========
}

