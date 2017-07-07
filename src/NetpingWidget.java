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
    //private JLabel checking;
    private JPanel linesPanel;

    private SettingsLoader settingsLoader;
    private OID io1OID;
    private Snmp snmp;
    private Logger logger;
    private TrayIcon trayIcon;
    private String appName;

    private boolean connnected;

    private NetPingSettings netPingSettings;

    private AutoChecking autoChecking;


    NetpingWidget(String ipAddressIn, String deviceNameIn, MainWindow mainWindowIn, NetPingSettings netPingSettingsIn){
        connnected = true;

        netPingSettings = netPingSettingsIn;

        deviceName.setText(deviceNameIn);
        ipAddress.setText(ipAddressIn);
        snmp = mainWindowIn.getSnmp();
        logger = mainWindowIn.getLogger();
        trayIcon = mainWindowIn.getTrayIcon();
        appName = mainWindowIn.getAppName();

        settingsLoader = mainWindowIn.getSettingsLoader();
        io1OID = new OID(settingsLoader.getSnmpSettings().getIo1OID);

        this.add(rootPanel);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
    }


    public void setDeviceName(String deviceNameIn){
        deviceName.setText(deviceNameIn);
    }

    public String getDeviceName(){
        return deviceName.getText();
    }


    public void setConnected(){
        String ip = ipAddress.getText();
        String name = deviceName.getText();

        status.setText(netPingSettings.connectedMessage.messageText);
        rootPanel.setBackground(netPingSettings.connectedMessage.backgroundColor);
        ipStatePanel.setBackground(netPingSettings.connectedMessage.backgroundColor);

        if(!connnected){
            logger.info("связь восстановлена с " + ip + " " + name);
            trayIcon.displayMessage(appName, "связь восстановлена с \n"+name, TrayIcon.MessageType.INFO);
        }
    }

    public void setDisconnected(){
        String ip = ipAddress.getText();
        String name = deviceName.getText();

        status.setText(netPingSettings.connectedMessage.messageText);
        rootPanel.setBackground(netPingSettings.connectedMessage.backgroundColor);
        ipStatePanel.setBackground(netPingSettings.connectedMessage.backgroundColor);

        if(connnected){
            logger.info("потеряна связь с " + ip + " " + name);
            trayIcon.displayMessage(appName, "потеряна связь с \n"+name, TrayIcon.MessageType.INFO);
        }
    }


    public AutoChecking getAutoCheking(){
        return autoChecking;
    }

    public NetPingSettings getNetPingSettings(){
        return netPingSettings;
    }
}
