import org.snmp4j.Snmp;

public class SnmpSettings {
    String snmpPort;
    String snmpTrapsPort;
    String community;
    String ipAddress;
    String trapOID;
    String getIo1OID;

    SnmpSettings(){
        snmpPort = null;
        snmpTrapsPort = null;
        community = null;
        ipAddress = null;
        trapOID = null;
        getIo1OID = null;
    }
}
