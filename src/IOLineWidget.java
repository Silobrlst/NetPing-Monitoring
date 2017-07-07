import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class IOLineWidget extends JPanel {
    private JLabel lineName;
    private JLabel messageText;
    private JPanel rootPanel;

    NetPingSettings netPingSettings;
    private IOLineSettings ioLineSettings;

    private Color defaultBackgroundColor;

    private AutoChecking autoChecking;

    IOLineWidget(String lineNameIn, NetPingSettings netPingSettingsIn, IOLineSettings ioLineSettingsIn){
        lineName.setText(lineNameIn);
        netPingSettings = netPingSettingsIn;
        ioLineSettings = ioLineSettingsIn;

        AutoChecking autoChecking = new AutoChecking(() -> {
            SnmpSettings snmpSettings = settingsLoader.getSnmpSettings();

            PDU pdu = new PDU();
            pdu.add(ioLineSettings.snmpGetVariable);
            pdu.setType(PDU.GET);

            // Create Target Address object
            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(new OctetString(netPingSettings.snmpCommunity));
            comtarget.setVersion(SnmpConstants.version1);
            comtarget.setAddress(new UdpAddress(ipAddress.getText() + "/" + snmpSettings.snmpPort));
            comtarget.setRetries(settingsLoader.getSnmpGetRetries());
            comtarget.setTimeout(settingsLoader.getSnmpGetTimeout());

            //checking.setText("проверка...");

            try {
                ResponseEvent response = snmp.get(pdu, comtarget);

                // Process Agent Response
                if (response != null) {
                    PDU responsePDU = response.getResponse();

                    if (responsePDU != null) {
                        int errorStatus = responsePDU.getErrorStatus();
                        int errorIndex = responsePDU.getErrorIndex();
                        String errorStatusText = responsePDU.getErrorStatusText();

                        if (errorStatus == PDU.noError) {
                            if (responsePDU.getVariable(io1OID) != null) {
                                int opened = responsePDU.getVariable(io1OID).toInt();

                                if (opened == openedValue) {
                                    setState(NetpingStateEnum.Closed);
                                } else if(opened == closedValue){
                                    setState(NetpingStateEnum.Opened);
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

                //checking.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }, settingsLoader.getCheckDelay());
    }

    void set1(){
        rootPanel.setBackground(ioLineSettings.value1Message.backgroundColor);
        messageText.setText(ioLineSettings.value1Message.messageText);
        messageText.setForeground(ioLineSettings.value1Message.textColor);
        lineName.setForeground(ioLineSettings.value1Message.textColor);
    }

    void set0(){
        rootPanel.setBackground(ioLineSettings.value0Message.backgroundColor);
        messageText.setText(ioLineSettings.value0Message.messageText);
        messageText.setForeground(ioLineSettings.value0Message.textColor);
        lineName.setForeground(ioLineSettings.value0Message.textColor);
    }

    void setError(){
        rootPanel.setBackground(defaultBackgroundColor);
        messageText.setText("Ошибка");
        messageText.setForeground(Color.BLACK);
        lineName.setForeground(Color.BLACK);
    }
}
