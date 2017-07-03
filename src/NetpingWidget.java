import org.apache.logging.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class NetpingWidget extends JPanel {
    private JLabel deviceName;
    private JLabel status;
    private JPanel rootPanel;
    private JLabel ipAddress;
    private JPanel ipStatePanel;
    private JLabel checking;

    private SettingsLoader settingsLoader;
    private OID io1OID;
    private VariableBinding io1Variable;
    private Snmp snmp;
    private Logger logger;
    private TrayIcon trayIcon;
    private String appName;
    private int openedValue;
    private int closedValue;

    private NetpingStateEnum state;

    private Thread thread;
    private Runnable checkTask;
    private java.util.Timer timer;
    private boolean autoCheckEnable;


    NetpingWidget(String ipAddressIn, String deviceNameIn, MainWindow mainWindowIn){
        deviceName.setText(deviceNameIn);
        ipAddress.setText(ipAddressIn);
        snmp = mainWindowIn.getSnmp();
        logger = mainWindowIn.getLogger();
        trayIcon = mainWindowIn.getTrayIcon();
        appName = mainWindowIn.getAppName();

        settingsLoader = mainWindowIn.getSettingsLoader();
        io1OID = new OID(settingsLoader.getSnmpSettings().getIo1OID);
        io1Variable = new VariableBinding(io1OID);
        openedValue = settingsLoader.getOpenedValue();
        closedValue = settingsLoader.getClosedValue();

        this.add(rootPanel);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);

        checkTask = () -> {
            SnmpSettings snmpSettings = settingsLoader.getSnmpSettings();

            PDU pdu = new PDU();
            pdu.add(io1Variable);
            pdu.setType(PDU.GET);

            // Create Target Address object
            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(new OctetString(snmpSettings.community));
            comtarget.setVersion(SnmpConstants.version1);
            comtarget.setAddress(new UdpAddress(ipAddress.getText() + "/" + snmpSettings.snmpPort));
            comtarget.setRetries(settingsLoader.getSnmpGetRetries());
            comtarget.setTimeout(settingsLoader.getSnmpGetTimeout());

            checking.setText("проверка...");

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
                            setState(NetpingStateEnum.Error);
                        }
                    } else {
                        //System.out.println("Error: Response PDU is null");
                        setState(NetpingStateEnum.Disconneted);
                    }
                } else {
                    //System.out.println("Error: Agent Timeout... ");
                    setState(NetpingStateEnum.Disconneted);
                }

                checking.setText("");

                if(autoCheckEnable){
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            thread = new Thread(checkTask);
                            thread.start();
                        }
                    }, settingsLoader.getCheckDelay()*1000);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };

        thread = new Thread(checkTask);

        timer = new java.util.Timer();

        autoCheckEnable = false;

        autoCheckStart();
        //thread.start();
    }

    public void setDeviceName(String deviceNameIn){
        deviceName.setText(deviceNameIn);
    }

    public String getDeviceName(){
        return deviceName.getText();
    }

    public void setState(NetpingStateEnum stateIn){
        String ip = ipAddress.getText();
        String name = deviceName.getText();

        NetpingStateEnum oldState = getState();

        if (stateIn != oldState) {
            switch (stateIn) {
                case Opened:
                    status.setText("открыт");
                    setColor(new Color(255, 250, 0));

                    if (oldState == NetpingStateEnum.Disconneted) {
                        logger.info("связь восстановлена, шкаф открыт " + ip + " " + name);
                        trayIcon.displayMessage(appName, "связь восстановлена, шкаф открыт\n"+name, TrayIcon.MessageType.INFO);
                    } else {
                        logger.info("открыт шкаф " + ip + " " + name);
                        trayIcon.displayMessage(appName, "открыт шкаф\n"+name, TrayIcon.MessageType.INFO);
                    }
                    break;
                case Closed:
                    status.setText("закрыт");
                    setColor(new Color(100, 255, 100));

                    if (oldState == NetpingStateEnum.Disconneted) {
                        logger.info("связь восстановлена, шкаф закрыт " + ip + " " + name);
                        trayIcon.displayMessage(appName, "связь восстановлена, шкаф закрыт\n"+name, TrayIcon.MessageType.INFO);
                    } else {
                        logger.info("закрыт шкаф " + ip + " " + name);
                        trayIcon.displayMessage(appName, "закрыт шкаф\n" + name, TrayIcon.MessageType.INFO);
                    }
                    break;
                case Disconneted:
                    status.setText("нет связи");
                    setColor(new Color(255, 100, 100));

                    logger.info("нет связи с " + ip + " " + name);
                    trayIcon.displayMessage(appName, "нет связи с "+name, TrayIcon.MessageType.INFO);
                    break;
                case Error:
                    status.setText("ошибка");
                    setColor(new Color(255, 127, 0));
                    break;
            }
        }

        state = stateIn;
    }

    public NetpingStateEnum getState(){
        return state;
    }

    private void setColor(Color colorIn){
        rootPanel.setBackground(colorIn);
        ipStatePanel.setBackground(colorIn);
    }


    public void autoCheckStart(){
        autoCheckEnable = true;
        thread = new Thread(checkTask);
        thread.start();
    }

    public void autoCheckStop(){
        autoCheckEnable = false;
    }
}
