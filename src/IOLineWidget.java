import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class IOLineWidget extends JPanel {
    private JLabel lineName;
    private JLabel messageText;
    private JPanel rootPanel;

    //<current line settings>=========
    //примненные настройки
    private OID trapReceiveOID;
    private OID snmpGetOID;
    private VariableBinding snmpGetVariable;
    private DisplayMessageSettings value1Message;
    private DisplayMessageSettings value0Message;
    //</current line settings>========

    //<to apply line settings>========
    //настройки которые применятся после вызова applySettings()
    private String trapReceiveOIDApply;
    private String snmpGetOIDApply;
    private String lineNameApply;
    private DisplayMessageSettings value1MessageApply;
    private DisplayMessageSettings value0MessageApply;
    //</to apply line settings>=======

    private String lineNumber;
    private Color defaultBackgroundColor;
    private AutoChecking autoChecking;
    private NetPingWidget netPingWidget;
    private int currentState;
    private boolean active;

    IOLineWidget(NetPingWidget netPingWidgetIn, String lineNumberIn){
        netPingWidget = netPingWidgetIn;
        lineNumber = lineNumberIn;
        value1Message = new DisplayMessageSettings();
        value0Message = new DisplayMessageSettings();

        value1MessageApply = value1Message;
        value0MessageApply = value0Message;

        switch(lineNumberIn){
            case "1":
                snmpGetOIDApply = "1.3.6.1.4.1.25728.8900.1.1.2.1";
                break;
            case "2":
                snmpGetOIDApply = "1.3.6.1.4.1.25728.8900.1.1.2.2";
                break;
            case "3":
                snmpGetOIDApply = "1.3.6.1.4.1.25728.8900.1.1.2.3";
                break;
            case "4":
                snmpGetOIDApply = "1.3.6.1.4.1.25728.8900.1.1.2.4";
                break;
            default:
                snmpGetOIDApply = "1.3.6.1.4.1.25728.8900.1.1.2.1";
                break;
        }


        trapReceiveOIDApply = "1.3.6.1.4.1.25728.8900.2.2.0";
        lineNameApply = "";

        snmpGetOID = new OID(snmpGetOIDApply);
        trapReceiveOID = new OID(trapReceiveOIDApply);
        snmpGetVariable = new VariableBinding(snmpGetOID);

        defaultBackgroundColor = this.getBackground();

        messageText.setText("неизвестно");

        currentState = -1;
        autoChecking = new AutoChecking(() -> {
            if(trapReceiveOID == null || snmpGetOID == null){
                return;
            }

            PDU pdu = new PDU();
            pdu.add(snmpGetVariable);
            pdu.setType(PDU.GET);

            // Create Target Address object
            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(new OctetString(netPingWidget.getSnmpCommunity()));
            comtarget.setVersion(SnmpConstants.version1);
            comtarget.setAddress(new UdpAddress(netPingWidget.getIpAddress() + "/" + netPingWidget.getDeviceName()));
            comtarget.setRetries(netPingWidget.getMainWindow().getRetries());
            comtarget.setTimeout(netPingWidget.getMainWindow().getTimeOut());

            messageText.setText("проверка...");

            try {
                ResponseEvent response = netPingWidget.getMainWindow().getSnmp().get(pdu, comtarget);

                // Process Agent Response
                if (response != null) {
                    PDU responsePDU = response.getResponse();

                    if (responsePDU != null) {
                        int errorStatus = responsePDU.getErrorStatus();
                        int errorIndex = responsePDU.getErrorIndex();
                        String errorStatusText = responsePDU.getErrorStatusText();

                        if (errorStatus == PDU.noError) {
                            if (responsePDU.getVariable(snmpGetOID) != null) {
                                int value = responsePDU.getVariable(snmpGetOID).toInt();

                                if (value == 1) {
                                    set1();
                                } else if(value == 0){
                                    set0();
                                }
                            }
                        } else {
                            System.out.println("Error: Request Failed");
                            System.out.println("Error Status = " + errorStatus);
                            System.out.println("Error Index = " + errorIndex);
                            System.out.println("Error Status Text = " + errorStatusText);
                            setError();
                        }
                    } else {
                        //System.out.println("Error: Response PDU is null");
                        setError();
                    }
                } else {
                    //System.out.println("Error: Agent Timeout... ");
                    setError();
                }

                messageText.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }, netPingWidget.getMainWindow().getCheckingDelay());

        this.add(rootPanel);
    }

    //<get>===============================
    public String getLineName(){
        return lineName.getText();
    }
    public DisplayMessageSettings getValue0Message(){
        return value0Message;
    }
    public DisplayMessageSettings getValue1Message(){
        return value1Message;
    }
    public OID getTrapReceiveOID(){
        return trapReceiveOID;
    }
    public OID getSnmpGetOID(){
        return snmpGetOID;
    }
    public AutoChecking getAutoChecking(){
        return autoChecking;
    }

    public boolean getActive(){
        return active;
    }

    public String getNotAppliedTrapReceiveOID(){
        return trapReceiveOIDApply;
    }
    public String getNotAppliedSnmpGetOID(){
        return snmpGetOIDApply;
    }
    public String getNotAppliedLineName(){
        return lineNameApply;
    }
    public DisplayMessageSettings getNotAppliedValue0Message(){
        return value0MessageApply;
    }
    public DisplayMessageSettings getNotAppliedValue1Message(){
        return value1MessageApply;
    }
    public String getLineNumber(){
        return lineNumber;
    }
    //</get>==============================

    //<set>===============================
    //настройки не применятся пока не будет вызвана applySettings()
    public void setLineName(String lineNameIn){
        lineNameApply = lineNameIn;
    }
    public void setSnmpGetOID(String snmpGetOIDIn){
        snmpGetOIDApply = snmpGetOIDIn;
    }
    public void setTrapReceiveOID(String trapReceiveOIDIn){
        trapReceiveOIDApply = trapReceiveOIDIn;
    }
    public void setValue1Message(DisplayMessageSettings value1MessageIn){
        value1MessageApply = value1MessageIn;
    }
    public void setValue0Message(DisplayMessageSettings value0MessageIn){
        value0MessageApply = value0MessageIn;
    }

    public void setActive(boolean activeIn){
        active = activeIn;
    }

    //изменение состояния линии
    public void set1(){
        rootPanel.setBackground(value1Message.backgroundColor);
        messageText.setText(value1Message.messageText);
        messageText.setForeground(value1Message.textColor);
        lineName.setForeground(value1Message.textColor);
        currentState = 1;
    }
    public void set0(){
        rootPanel.setBackground(value0Message.backgroundColor);
        messageText.setText(value0Message.messageText);
        messageText.setForeground(value0Message.textColor);
        lineName.setForeground(value0Message.textColor);
        currentState = 0;
    }
    public void setError(){
        rootPanel.setBackground(defaultBackgroundColor);
        messageText.setText("Ошибка");
        messageText.setForeground(Color.BLACK);
        lineName.setForeground(Color.BLACK);
    }
    //</set>==============================

    public void applySettings(){
        lineName.setText(lineNameApply);
        snmpGetOID = new OID(snmpGetOIDApply);
        trapReceiveOID = new OID(trapReceiveOIDApply);
        snmpGetVariable = new VariableBinding(trapReceiveOID);

        value1Message = value1MessageApply;
        value0Message = value0MessageApply;

        if(currentState == 1){
            set1();
        }else if(currentState == 0){
            set0();
        }

    }
    public void discardSettings(){
        lineNameApply = lineName.getText();

        if(snmpGetOID == null){
            snmpGetOIDApply = "";
        }else{
            snmpGetOIDApply = snmpGetOID.toString();
        }

        if(trapReceiveOID == null){
            trapReceiveOIDApply = "";
        }else{
            trapReceiveOIDApply = trapReceiveOID.toString();
        }

        value1MessageApply = value1Message;
        value0MessageApply = value0Message;
    }
}
