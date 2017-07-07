import java.util.HashMap;
import java.util.Map;

public class NetPingSettings {
    //ioLineSettingsMap<LineName, LineSettings>
    public Map<String, IOLineSettings> ioLineSettingsMap;

    public String snmpCommunity;
    public String snmpPort;

    public DisplayMessageSettings connectedMessage;
    public DisplayMessageSettings disconnectedMessage;

    NetPingSettings(){
        ioLineSettingsMap = new HashMap<>();

        snmpCommunity = "public";
        snmpPort = "161";

        connectedMessage = new DisplayMessageSettings();
        disconnectedMessage = new DisplayMessageSettings();
    }
}
