import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class NetPingWidget extends JPanel {
    private JLabel deviceName;
    private JLabel status;
    private JPanel rootPanel;
    private JLabel ipAddressLabel;
    private JPanel ipStatePanel;
    private JLabel checking;
    private IOLineWidget line1;
    private IOLineWidget line2;
    private IOLineWidget line3;
    private IOLineWidget line4;

    //<netping settings>==================
    private String snmpCommunity;
    private String snmpPort;

    private DisplayMessageSettings connectedMessage;
    private DisplayMessageSettings disconnectedMessage;
    //</netping settings>=================

    private MainWindow mainWindow;
    private boolean connnected;
    private AutoChecking autoChecking;

    NetPingWidget(MainWindow mainWindowIn, String ipAddressIn){
        connnected = true;

        mainWindow = mainWindowIn;

        deviceName.setText("");
        ipAddressLabel.setText(ipAddressIn);

        this.add(rootPanel);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);

        autoChecking = new AutoChecking(() -> {
            status.setText("ping");
        }, mainWindowIn.getDelay());
    }

    //<get>===============================
    public String getDeviceName(){
        return deviceName.getText();
    }
    public String getIpAddress(){
        return ipAddressLabel.getText();
    }
    public AutoChecking getAutoCheking(){
        return autoChecking;
    }
    public String getSnmpCommunity(){
        return snmpCommunity;
    }
    public String getSnmpPort(){
        return snmpPort;
    }
    public DisplayMessageSettings getConnectedMessage(){
        return connectedMessage;
    }
    public DisplayMessageSettings getDisconnectedMessage(){
        return disconnectedMessage;
    }
    public MainWindow getMainWindow(){
        return mainWindow;
    }
    public IOLineWidget getLine(String lineNumberIn){
        switch(lineNumberIn){
            case "1":
                return line1;
            case "2":
                return line2;
            case "3":
                return line3;
            case "4":
                return line4;
            default:
                return null;
        }
    }
    //</get>==============================

    //<set>===============================
    public void setDeviceName(String deviceNameIn){
        deviceName.setText(deviceNameIn);
    }
    public void setIpAddress(String deviceNameIn){
        ipAddressLabel.setText(deviceNameIn);
    }
    public void setConnected(){
        String ip = ipAddressLabel.getText();
        String name = deviceName.getText();

        status.setText(connectedMessage.messageText);
        rootPanel.setBackground(connectedMessage.backgroundColor);
        ipStatePanel.setBackground(connectedMessage.backgroundColor);

        if(!connnected){
            mainWindow.getLogger().info("связь восстановлена с " + ip + " " + name);
            mainWindow.getTrayIcon().displayMessage(mainWindow.getAppName(), "связь восстановлена с \n"+name, TrayIcon.MessageType.INFO);
        }
    }
    public void setDisconnected(){
        String ip = ipAddressLabel.getText();
        String name = deviceName.getText();

        status.setText(disconnectedMessage.messageText);
        rootPanel.setBackground(disconnectedMessage.backgroundColor);
        ipStatePanel.setBackground(disconnectedMessage.backgroundColor);

        if(connnected){
            mainWindow.getLogger().info("потеряна связь с " + ip + " " + name);
            mainWindow.getTrayIcon().displayMessage(mainWindow.getAppName(), "потеряна связь с \n"+name, TrayIcon.MessageType.INFO);
        }
    }
    public void setSnmpCommunity(String snmpCommunityIn){
        snmpCommunity = snmpCommunityIn;
    }
    public void setSnmpPort(String snmpPortIn){
        snmpPort = snmpPortIn;
    }
    public void setConnectedMessage(DisplayMessageSettings connectedMessageIn){
        connectedMessage = connectedMessageIn;
    }
    public void setDisconnectedMessage(DisplayMessageSettings disconnectedMessageIn){
        disconnectedMessage = disconnectedMessageIn;
    }

    public boolean isModified(IOLineWidget data) {
        return false;
    }
    //</set>==============================
}
