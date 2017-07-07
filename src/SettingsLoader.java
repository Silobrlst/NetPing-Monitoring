import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SettingsLoader {
    private String configFileName;
    private JSONObject config;

    private Map<String, Object> defaultParametersMap;
    private static final String ipAddress = "ipAddress";
    private static final String snmpPort = "snmpPort";
    private static final String snmpTrapsPort = "snmpTrapsPort";
    private static final String trapOID = "trapOID";
    private static final String getIo1OID = "getIo1OID";
    private static final String community = "community";
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


    SettingsLoader(String configFileNameIn) {
        defaultParametersMap = new HashMap<>();
        configFileName = configFileNameIn;

        defaultParametersMap.put(ipAddress, "0.0.0.0");
        defaultParametersMap.put(snmpPort, "161");
        defaultParametersMap.put(snmpTrapsPort, "162");
        defaultParametersMap.put(trapOID, "1.3.6.1.4.1.25728.8900.2.2.0");
        defaultParametersMap.put(getIo1OID, "1.3.6.1.4.1.25728.8900.1.1.2.1");
        defaultParametersMap.put(community, "SWITCH");
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
        settings.community = config.getString(community);
        settings.getIo1OID = config.getString(getIo1OID);
        settings.ipAddress = config.getString(ipAddress);
        settings.snmpPort = config.getString(snmpPort);
        settings.snmpTrapsPort = config.getString(snmpTrapsPort);
        settings.trapOID = config.getString(trapOID);
        return settings;
    }

    public void setSnmpSettings(SnmpSettings snmpSettingsIn) {
        if (snmpSettingsIn.community != null) {
            config.put(community, snmpSettingsIn.community);
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


    public Map<String, String> getNetpingIpNameMap() {
        Map<String, String> map = new HashMap<>();

        JSONObject netpingsJSON = config.getJSONObject(netpings);
        for (String ip : netpingsJSON.keySet()) {
            map.put(ip, netpingsJSON.getString(ip));
        }

        return map;
    }

    public void setNetping(String ipAddressIn, String nameIn) {
        JSONObject netpingsJSON = config.getJSONObject(netpings);
        netpingsJSON.put(ipAddressIn, nameIn);
    }

    public void deleteNetping(String ipAddressIn) {
        JSONObject netpingsJSON = config.getJSONObject(netpings);
        netpingsJSON.remove(ipAddressIn);
        config.remove(ipAddressIn);
    }

    public boolean isNetpingExists(String ipAddressIn) {
        return config.getJSONObject(netpings).has(ipAddressIn);
    }


    public int getCheckDelay() {
        return config.getInt(checkDelay);
    }

    public void setCheckTime(int checkTimeIn) {
        config.put(checkDelay, checkTimeIn);
    }


    public void setTrayIconVisible(boolean visibleIn) {
        config.put(trayIcon, visibleIn);
    }

    public boolean isTrayIcon() {
        return config.getBoolean(trayIcon);
    }


    public void setStyle(String styleNameIn) {
        config.put(style, styleNameIn);
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

    public int getClosedValue() {
        return config.getInt(closedValue);
    }

    public int getOpenedValue() {
        return config.getInt(openedValue);
    }
}
