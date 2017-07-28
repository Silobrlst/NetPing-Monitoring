import javax.swing.*;
import java.awt.*;

public class NetPingWidget extends JPanel {
    private JLabel deviceName;
    private JLabel status;
    private JPanel rootPanel;
    private JLabel ipAddressLabel;
    private JPanel ipStatePanel;
    private JLabel checking;
    private JPanel linesPanel;
    private IOLineWidget line1;
    private IOLineWidget line2;
    private IOLineWidget line3;
    private IOLineWidget line4;

    //<current netPing settings>==========
    //примненные настройки
    private String snmpCommunity;
    private String snmpPort;
    private DisplayMessageSettings connectedMessage;
    private DisplayMessageSettings disconnectedMessage;
    //</current netPing settings>=========

    //<to apply netPing settings>=========
    //настройки которые применятся после вызова applySettings()
    private String snmpCommunityApply;
    private String snmpPortApply;
    private String deviceNameApply;
    private String ipAddressApply;
    //</to apply netPing settings>========

    private MainWindow mainWindow;
    private boolean connected;
    private AutoChecking autoChecking;

    NetPingWidget(MainWindow mainWindowIn, String ipAddressIn){
        mainWindow = mainWindowIn;

        connected = true;
        deviceName.setText("");
        ipAddressLabel.setText(ipAddressIn);

        ipAddressApply = ipAddressLabel.getText();
        deviceNameApply = deviceName.getText();

        line1 = new IOLineWidget(this, "1");
        line2 = new IOLineWidget(this, "2");
        line3 = new IOLineWidget(this, "3");
        line4 = new IOLineWidget(this, "4");

        linesPanel.setLayout(new GridLayout(4, 1));
        linesPanel.add(line1);
        linesPanel.add(line2);
        linesPanel.add(line3);
        linesPanel.add(line4);

        this.add(rootPanel);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);

        autoChecking = new AutoChecking(() -> {
            status.setText("ping");
        }, mainWindowIn.getCheckingDelay());
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

    public String getNotAppliedDeviceName(){
        return deviceNameApply;
    }
    public String getNotAppliedSnmpPort(){
        return snmpPortApply;
    }
    public String getNotAppliedIpAddress(){
        return ipAddressApply;
    }
    public String getNotAppliedSnmpCommunity(){
        return snmpCommunityApply;
    }
    //</get>==============================

    //<set>===============================
    //настройки не применятся пока не будет вызвана applySettings()
    public void setDeviceName(String deviceNameIn){
        deviceNameApply = deviceNameIn;
    }
    public void setIpAddress(String ipAddressIn){
        ipAddressApply = ipAddressIn;
    }
    public void setSnmpCommunity(String snmpCommunityIn){
        snmpCommunityApply = snmpCommunityIn;
    }
    public void setSnmpPort(String snmpPortIn){
        snmpPortApply = snmpPortIn;
    }
    public void setConnectedMessage(DisplayMessageSettings connectedMessageIn){
        connectedMessage = connectedMessageIn;
    }
    public void setDisconnectedMessage(DisplayMessageSettings disconnectedMessageIn){
        disconnectedMessage = disconnectedMessageIn;
    }

    public void setLineActive(String lineNumberIn, boolean activeIn){
        IOLineWidget ioLineWidget = this.getLine(lineNumberIn);
        ioLineWidget.setActive(activeIn);

        if(activeIn){
            linesPanel.add(ioLineWidget);
        }else{
            linesPanel.remove(ioLineWidget);
        }

        linesPanel.revalidate();
        linesPanel.repaint();
    }

    //изменение состояния связи с NetPing
    public void setConnected(){
        String ip = ipAddressLabel.getText();
        String name = deviceName.getText();

        status.setText(connectedMessage.messageText);
        rootPanel.setBackground(connectedMessage.backgroundColor);
        ipStatePanel.setBackground(connectedMessage.backgroundColor);

        if(!connected){
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

        if(connected){
            mainWindow.getLogger().info("потеряна связь с " + ip + " " + name);
            mainWindow.getTrayIcon().displayMessage(mainWindow.getAppName(), "потеряна связь с \n"+name, TrayIcon.MessageType.INFO);
        }
    }
    //</set>==============================

    //применяет настройки для виджета (вместе с внутренними виджетами)
    public void applySettings(){
        deviceName.setText(deviceNameApply);
        ipAddressLabel.setText(ipAddressApply);
        snmpPort = snmpPortApply;
        snmpCommunity = snmpCommunityApply;

        line1.applySettings();
        line2.applySettings();
        line3.applySettings();
        line4.applySettings();
    }

    //сбрасывает не применненые настройки (вместе с внутренними виджетами)
    public void discardSettings(){
        deviceNameApply = deviceName.getText();
        ipAddressApply = ipAddressLabel.getText();
        snmpPortApply = snmpPort;
        snmpCommunityApply = snmpCommunity;

        line1.discardSettings();
        line2.discardSettings();
        line3.discardSettings();
        line4.discardSettings();
    }
}
