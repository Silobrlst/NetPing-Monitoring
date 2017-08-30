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
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class IoLineWidget extends JPanel {
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
    private boolean active = DefaultSettings.active;
    private boolean snmpInitialized = false;

    private JPopupMenu popup = new JPopupMenu();

    private EditIoLineDialog editIoLineDialog = new EditIoLineDialog(null);

    private static final String messageToolTipText = "<html>Состояние линии:<Br>";

    IoLineWidget(NetPingWidget netPingWidgetIn, String lineNumberIn){
        netPingWidget = netPingWidgetIn;
        lineNumber = lineNumberIn;
        value0Message = new DisplayMessage();
        value1Message = new DisplayMessage();

        switch(lineNumberIn){
            case "1":
                snmpGetOIDApply = DefaultSettings.snmpGetLine1Oid;
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

        //<подсветка при навдении курсора>==============================================================================
        EtchedBorder etchedBorder = new EtchedBorder();
        Border originalBorder = rootPanel.getBorder();

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                rootPanel.setBorder(etchedBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                rootPanel.setBorder(originalBorder);
            }
        };

        rootPanel.addMouseListener(mouseAdapter);
        messageText.addMouseListener(mouseAdapter);
        lineName.addMouseListener(mouseAdapter);
        //</подсветка при навдении курсора>=============================================================================

        trapReceiveOIDApply = "1.3.6.1.4.1.25728.8900.2.2.0";
        lineNameApply = "";

        snmpGetOID = new OID(snmpGetOIDApply);
        trapReceiveOID = new OID(trapReceiveOIDApply);
        snmpGetVariable = new VariableBinding(snmpGetOID);

        defaultBackgroundColor = this.getBackground();

        currentState = -1;

        Runnable checkFunction = () -> {
            if(trapReceiveOID == null || snmpGetOID == null || netPingWidget.getMainWindow().getSnmp() == null || !netPingWidget.isActive()){
                return;
            }

            PDU pdu = new PDU();
            pdu.add(snmpGetVariable);
            pdu.setType(PDU.GET);

            // Create Target Address object
            CommunityTarget comTarget = new CommunityTarget();
            comTarget.setCommunity(new OctetString(netPingWidget.getSnmpCommunity()));
            comTarget.setVersion(SnmpConstants.version1);
            comTarget.setAddress(new UdpAddress(netPingWidget.getIpAddress() + "/" + netPingWidget.getSnmpPort()));
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
                            setError("Error: Request Failed");
                        }
                    } else {
                        //System.out.println("Error: Response PDU is null");
                        setError("Error: Response PDU is null");
                    }
                } else {
                    //System.out.println("Error: Agent Timeout... ");
                    setError("Error: Agent Timeout... ");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };

        autoChecking = new AutoChecking(checkFunction, netPingWidget.getMainWindow().getCheckingDelay());

        //<контекстное меню>============================================================================================
        MouseListener popupListener = new PopupListener();
        rootPanel.addMouseListener(popupListener);
        messageText.addMouseListener(popupListener);
        lineName.addMouseListener(popupListener);

        JMenuItem checkItem = new JMenuItem("Проверить");
        JMenuItem editItem = new JMenuItem("Изменить");
        popup.add(checkItem);
        popup.add(editItem);

        IoLineWidget context = this;
        checkItem.addActionListener(e -> new Thread(checkFunction).start());
        editItem.addActionListener(e -> {
            rootPanel.setBorder(etchedBorder);
            if(editIoLineDialog.open(context, getLineNumber()) == JOptionPane.OK_OPTION){
                netPingWidget.getMainWindow().saveOnlyIoLine(netPingWidget, context);
            }
            rootPanel.setBorder(originalBorder);
            context.repaint();
        });
        //</контекстное меню>===========================================================================================

        this.add(rootPanel);
    }

    boolean isActive(){
        return active;
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

    private String getLoggingName(){
        return netPingWidget.getLoggingName() + ", линия " + getLineNumber() + " \"" + lineName.getText() + "\"";
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
    void setCheckingDelay(int delayIn){
        autoChecking.setDelay(delayIn);
    }

    void setActive(boolean activeIn){
        if(activeIn && !active){
            if(snmpInitialized) {
                autoChecking.start();
                netPingWidget.getMainWindow().getLogger().info(getLoggingName() + ": мониторинг линии запущен");
            }
        }else if(!activeIn && active){
            autoChecking.stop();
            netPingWidget.getMainWindow().getLogger().info(getLoggingName() + ": мониторинг линии остановлен");
        }

        active = activeIn;
    }

    //изменение состояния линии
    void set0(){
        applyDisplayMessage(value0Message);
        currentState = 0;

        netPingWidget.getMainWindow().getLogger().info(changedStateLog("0 \"" + value0Message.getMessageText() + "\""));
        netPingWidget.getMainWindow().getTrayIcon().displayMessage("изменение состояния линии", getLoggingName() + "\n\"" + value0Message.getMessageText() + "\"", TrayIcon.MessageType.INFO);
    }
    void set1(){
        applyDisplayMessage(value1Message);
        currentState = 1;

        netPingWidget.getMainWindow().getLogger().info(changedStateLog("1 \"" + value1Message.getMessageText() + "\""));
        netPingWidget.getMainWindow().getTrayIcon().displayMessage("изменение состояния линии", getLoggingName() + "\n\"" + value1Message.getMessageText() + "\"", TrayIcon.MessageType.INFO);
    }
    void setError(String commentIn){
        rootPanel.setBackground(defaultBackgroundColor);
        messageText.setText("Ошибка");
        messageText.setToolTipText(messageToolTipText + commentIn + "</html>");
        messageText.setForeground(Color.BLACK);
        lineName.setForeground(Color.BLACK);

//        netPingWidget.getMainWindow().getLogger().info(changedStateLog("\"ошибка\""));
//        netPingWidget.getMainWindow().getTrayIcon().displayMessage("изменение состояния линии", getLoggingName() + "\n\"ошибка\"", TrayIcon.MessageType.INFO);
    }
    //</set>============================================================================================================

    private String changedStateLog(String stateIn){
        return getLoggingName() + ": изменение состояния на " + stateIn;
    }

    void copyTo(IoLineWidget ioLineWidgetIn){
        ioLineWidgetIn.setLineName(getLineName());
        ioLineWidgetIn.setSnmpGetOID(getSnmpGetOID().toString());
        ioLineWidgetIn.setTrapReceiveOID(getTrapReceiveOID().toString());

        getValue0Message().copyTo(ioLineWidgetIn.getValue0Message());
        getValue1Message().copyTo(ioLineWidgetIn.getValue1Message());
    }

    void snmpInitialized(){
        snmpInitialized = true;

        if(active){
            autoChecking.start();
        }
    }

    private void applyDisplayMessage(DisplayMessage displayMessageIn){
        rootPanel.setBackground(displayMessageIn.getBackgroundColor());
        messageText.setText(displayMessageIn.getMessageText());
        messageText.setToolTipText(messageToolTipText + displayMessageIn.getMessageText() + "</html>");
        messageText.setForeground(displayMessageIn.getTextColor());
        lineName.setForeground(displayMessageIn.getTextColor());
    }

    void applySettings(){
        lineName.setText(lineNameApply);
        snmpGetOID = new OID(snmpGetOIDApply);
        snmpGetVariable = new VariableBinding(snmpGetOID);

        trapReceiveOID = new OID(trapReceiveOIDApply);

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

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        editIoLineDialog.updateStyle();
    }

    private class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }
}
