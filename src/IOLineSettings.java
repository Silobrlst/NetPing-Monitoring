import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

public class IOLineSettings {
    public OID trapReceiveOID;
    public VariableBinding snmpGetVariable;
    public DisplayMessageSettings value1Message;
    public DisplayMessageSettings value0Message;

    IOLineSettings(OID trapReceiveOIDIn, VariableBinding snmpGetVariableIn){
        trapReceiveOID = trapReceiveOIDIn;
        snmpGetVariable = snmpGetVariableIn;
        value1Message = new DisplayMessageSettings();
        value0Message = new DisplayMessageSettings();
    }
}
