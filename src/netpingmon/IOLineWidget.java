package netpingmon;

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

    //<current settings>================================================================================================
    //примненные настройки
    private OID trapReceiveOID;
    private OID snmpGetOID;
    private VariableBinding snmpGetVariable;
    //</current settings>===============================================================================================

    //<to apply settings>===============================================================================================
    //новые непримёненные настройки, применятся после вызова applySettings()
    private String trapReceiveOIDApply;
    private String snmpGetOIDApply;
    private String lineNameApply;
    //</to apply settings>==============================================================================================

    private DisplayMessage value0Message;
    private DisplayMessage value1Message;

    private String lineNumber;
    private Color defaultBackgroundColor;
    private AutoChecking autoChecking;
    private NetPingWidget netPingWidget;
    private int currentState;
    private boolean active;

    IOLineWidget(NetPingWidget netPingWidgetIn, String lineNumberIn){
        netPingWidget = netPingWidgetIn;
        lineNumber = lineNumberIn;
        value0Message = new DisplayMessage();
        value1Message = new DisplayMessage();

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

        currentState = -1;
        autoChecking = new AutoChecking(() -> {
            if(trapReceiveOID == null || snmpGetOID == null){
                return;
            }

            PDU pdu = new PDU();
            pdu.add(snmpGetVariable);
            pdu.setType(PDU.GET);

            // Create Target Address object
            CommunityTarget comTarget = new CommunityTarget();
            comTarget.setCommunity(new OctetString(netPingWidget.getSnmpCommunity()));
            comTarget.setVersion(SnmpConstants.version1);
            comTarget.setAddress(new UdpAddress(netPingWidget.getIpAddress() + "/" + netPingWidget.getDeviceName()));
            comTarget.setRetries(netPingWidget.getMainWindow().getRetries());
            comTarget.setTimeout(netPingWidget.getMainWindow().getTimeOut());

            messageText.setText("проверка...");

            try {
                ResponseEvent response = netPingWidget.getMainWindow().getSnmp().get(pdu, comTarget);

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

    //<get>=============================================================================================================
    String getLineName(){
        return lineName.getText();
    }
    DisplayMessage getValue0Message(){
        return value0Message;
    }
    DisplayMessage getValue1Message(){
        return value1Message;
    }
    OID getTrapReceiveOID(){
        return trapReceiveOID;
    }
    OID getSnmpGetOID(){
        return snmpGetOID;
    }

    boolean getActive(){
        return active;
    }

    String getNotAppliedTrapReceiveOID(){
        return trapReceiveOIDApply;
    }
    String getNotAppliedSnmpGetOID(){
        return snmpGetOIDApply;
    }
    String getNotAppliedLineName(){
        return lineNameApply;
    }
    String getLineNumber(){
        return lineNumber;
    }
    //</get>============================================================================================================

    //<set>=============================================================================================================
    //настройки не применятся пока не будет вызвана applySettings()
    void setLineName(String lineNameIn){
        lineNameApply = lineNameIn;
    }
    void setSnmpGetOID(String snmpGetOIDIn){
        snmpGetOIDApply = snmpGetOIDIn;
    }
    void setTrapReceiveOID(String trapReceiveOIDIn){
        trapReceiveOIDApply = trapReceiveOIDIn;
    }

    void setActive(boolean activeIn){
        active = activeIn;
    }

    void startChecking(){
        autoChecking.start();
    }
    void stopChecking(){
        autoChecking.stop();
    }

    //изменение состояния линии
    void set0(){
        applyDisplayMessage(value0Message);
        currentState = 0;
    }
    void set1(){
        applyDisplayMessage(value1Message);
        currentState = 1;
    }
    private void setError(){
        rootPanel.setBackground(defaultBackgroundColor);
        messageText.setText("Ошибка");
        messageText.setForeground(Color.BLACK);
        lineName.setForeground(Color.BLACK);
    }
    //</set>============================================================================================================

    private void applyDisplayMessage(DisplayMessage displayMessageIn){
        rootPanel.setBackground(displayMessageIn.getBackgroundColor());
        messageText.setText(displayMessageIn.getMessageText());
        messageText.setForeground(displayMessageIn.getTextColor());
        lineName.setForeground(displayMessageIn.getTextColor());
    }

    void applySettings(){
        lineName.setText(lineNameApply);
        snmpGetOID = new OID(snmpGetOIDApply);
        trapReceiveOID = new OID(trapReceiveOIDApply);
        snmpGetVariable = new VariableBinding(trapReceiveOID);

        value0Message.applySettings();
        value1Message.applySettings();

        if(currentState == 1){
            set1();
        }else if(currentState == 0){
            set0();
        }
    }
    void discardSettings(){
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

        value0Message.discardSettings();
        value1Message.discardSettings();
    }
}
