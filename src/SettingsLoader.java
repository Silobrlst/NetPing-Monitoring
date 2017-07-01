import org.json.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

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

    SettingsLoader(String configFileNameIn){
        configFileName = configFileNameIn;
        loadConfig();
    }

    private String loadDataFromfile(String fileNameIn){
        try{
            File file = new File(fileNameIn);
            file.createNewFile();

            byte[] encoded = Files.readAllBytes(Paths.get(fileNameIn));
            return new String(encoded, Charset.defaultCharset());
        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    private void saveDataToFile(String fileNameIn, String dataIn){
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

    public void loadConfig(){
        String data = loadDataFromfile(configFileName);

        JSONObject json;
        if(data.isEmpty()){
            json = new JSONObject();
        }else{
            json = new JSONObject(data);
        }

        boolean saveFile = false;

        if(!json.has("ipAddress")){
            json.put("ipAddress", "0.0.0.0");
            saveFile=true;
        }

        if(!json.has("snmpPort")){
            json.put("snmpPort", "161");
            saveFile=true;
        }

        if(!json.has("snmpTrapsPort")){
            json.put("snmpTrapsPort", "162");
            saveFile=true;
        }

        if(!json.has("trapOID")){
            json.put("trapOID", "1.3.6.1.4.1.25728.8900.2.2.0");
            saveFile=true;
        }

        if(!json.has("getIo1OID")){
            json.put("getIo1OID", "1.3.6.1.4.1.25728.8900.1.1.2.1");
            saveFile=true;
        }

        if(!json.has("community")){
            json.put("community", "SWITCH");
            saveFile=true;
        }

        if(!json.has("netpings")){
            json.put("netpings", new JSONObject());
            saveFile=true;
        }

        if(!json.has("checkTime")){
            json.put("checkTime", 5);
            saveFile=true;
        }

        config = json;

        if(saveFile){
            saveConfig();
        }
    }

    public void saveConfig(){
        saveDataToFile(configFileName, config.toString(5));
    }

    public void loadDefaultConfig(){
        JSONObject json = new JSONObject();
        json.put("ipAddress", "0.0.0.0");
        json.put("snmpPort", "161");
        json.put("snmpTrapsPort", "162");
        json.put("trapOID", "1.3.6.1.4.1.25728.8900.2.2.0");
        json.put("getIo1OID", "1.3.6.1.4.1.25728.8900.1.1.2.1");
        json.put("community", "SWITCH");
        json.put("netpings", new JSONObject());

        config = json;
    }

    public SnmpSettings getSnmpSettings(){
        SnmpSettings settings = new SnmpSettings();
        settings.community = config.getString("community");
        settings.getIo1OID = config.getString("getIo1OID");
        settings.ipAddress = config.getString("ipAddress");
        settings.snmpPort = config.getString("snmpPort");
        settings.snmpTrapsPort = config.getString("snmpTrapsPort");
        settings.trapOID = config.getString("trapOID");
        return settings;
    }

    public void setSnmpSettings(SnmpSettings snmpSettingsIn){
        if(snmpSettingsIn.community != null){
            config.put("community", snmpSettingsIn.community);
        }
        if(snmpSettingsIn.getIo1OID != null){
            config.put("getIo1OID", snmpSettingsIn.getIo1OID);
        }
        if(snmpSettingsIn.ipAddress != null){
            config.put("ipAddress", snmpSettingsIn.ipAddress);
        }
        if(snmpSettingsIn.snmpPort != null){
            config.put("snmpPort", snmpSettingsIn.snmpPort);
        }
        if(snmpSettingsIn.snmpTrapsPort != null){
            config.put("snmpTrapsPort", snmpSettingsIn.snmpTrapsPort);
        }
        if(snmpSettingsIn.trapOID != null){
            config.put("trapOID", snmpSettingsIn.trapOID);
        }
    }


    public Map<String, NetpingWidget> getNetpingWidgetsMap(){
        Map<String, NetpingWidget> netpings = new HashMap<>();

        JSONObject netpingsJSON = config.getJSONObject("netpings");
        for(String ip: netpingsJSON.keySet()){
            String devcieName = netpingsJSON.getString(ip);
            netpings.put(ip, new NetpingWidget(ip, devcieName));
        }

        return netpings;
    }

    public Map<String, String> getNetpingIpNameMap(){
        Map<String, String> map = new HashMap<>();

        JSONObject netpingsJSON = config.getJSONObject("netpings");
        for(String ip: netpingsJSON.keySet()){
            map.put(ip, netpingsJSON.getString(ip));
        }

        return map;
    }

    public void setNetping(String ipAddressIn, String nameIn){
        JSONObject netpings = config.getJSONObject("netpings");
        netpings.put(ipAddressIn, nameIn);
    }

    public void deleteNetping(String ipAddressIn){
        JSONObject netpings = config.getJSONObject("netpings");
        netpings.remove(ipAddressIn);
        config.remove(ipAddressIn);
    }

    public boolean isNetpingExists(String ipAddressIn){
        return config.getJSONObject("netpings").has(ipAddressIn);
    }


    public int getCheckTime(){
        return config.getInt("checkTime");
    }

    public void setCheckTime(int checkTimeIn){
        config.put("checkTime", checkTimeIn);
    }
}
