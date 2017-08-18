package netpingmon;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;

public class NetPingWidget extends JPanel {
    private JLabel deviceName;
    private JPanel rootPanel;
    private JLabel ipAddress;
    private JPanel ipStatePanel;
    private JLabel checking;
    private JPanel linesPanel;
    private JLabel ipAddressLabel;
    private IOLineWidget line1;
    private IOLineWidget line2;
    private IOLineWidget line3;
    private IOLineWidget line4;

    //<current netPing settings>========================================================================================
    //примненные настройки
    private String snmpCommunity = "SWITCH";
    private String snmpPort = "161";
    private DisplayMessage connectedMessage = new DisplayMessage();
    private DisplayMessage disconnectedMessage = new DisplayMessage();
    //</current netPing settings>=======================================================================================

    //<to apply netPing settings>=======================================================================================
    //новые непримёненные настройки, применятся после вызова applySettings()
    private String snmpCommunityApply = snmpCommunity;
    private String snmpPortApply = snmpPort;
    private String deviceNameApply = deviceName.getText();
    private String ipAddressApply = ipAddress.getText();
    //</to apply netPing settings>======================================================================================

    private MainWindow mainWindow;
    private Boolean connected = null;
    private AutoChecking autoChecking;

    NetPingWidget(MainWindow mainWindowIn, String ipAddressIn){
        mainWindow = mainWindowIn;

        deviceName.setText("");
        ipAddress.setText(ipAddressIn);

        ipAddressApply = ipAddress.getText();
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
            boolean reached = false;

            checking.setText("проверка...");

            try{
                InetAddress address = InetAddress.getByName(ipAddressApply);

                int numChecks = 0;

                while(numChecks<mainWindow.getRetries() && !reached){
                    reached = address.isReachable(mainWindow.getTimeOut()*1000);
                    numChecks++;
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }

            if(reached){
                setConnected();
            }else{
                setDisconnected();
            }
        }, mainWindowIn.getCheckingDelay());
    }

    //<get>=============================================================================================================
    String getDeviceName(){
        return deviceName.getText();
    }
    String getIpAddress(){
        return ipAddress.getText();
    }
    String getSnmpCommunity(){
        return snmpCommunity;
    }
    String getSnmpPort(){
        return snmpPort;
    }
    DisplayMessage getConnectedMessage(){
        return connectedMessage;
    }
    DisplayMessage getDisconnectedMessage(){
        return disconnectedMessage;
    }
    MainWindow getMainWindow(){
        return mainWindow;
    }
    IOLineWidget getLine(String lineNumberIn){
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

    String getNotAppliedDeviceName(){
        return deviceNameApply;
    }
    String getNotAppliedSnmpPort(){
        return snmpPortApply;
    }
    String getNotAppliedIpAddress(){
        return ipAddressApply;
    }
    String getNotAppliedSnmpCommunity(){
        return snmpCommunityApply;
    }
    //</get>============================================================================================================

    //<set>=============================================================================================================
    //настройки не применятся пока не будет вызвана applySettings()
    void setDeviceName(String deviceNameIn){
        deviceNameApply = deviceNameIn;
    }
    void setIpAddress(String ipAddressIn){
        ipAddressApply = ipAddressIn;
    }
    void setSnmpCommunity(String snmpCommunityIn){
        snmpCommunityApply = snmpCommunityIn;
    }
    void setSnmpPort(String snmpPortIn){
        snmpPortApply = snmpPortIn;
    }

    void setLineActive(String lineNumberIn, boolean activeIn){
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
    void setActive(boolean activeIn){
        if(!activeIn){
            stopChecking();
            line1.setActive(false);
            line2.setActive(false);
            line3.setActive(false);
            line4.setActive(false);
        }
    }

    //изменение состояния связи с NetPing
    private void setConnected(){
        if(connected == null){
            connected = false; //ставим наоборт чтобы сработал следующий if
        }

        applyDisplayMessage(connectedMessage);

        if(!connected){
            connected = true;

            String ip = ipAddress.getText();
            String name = deviceName.getText();

            mainWindow.getLogger().info("связь восстановлена с " + ip + " " + name);
            mainWindow.getTrayIcon().displayMessage(mainWindow.getAppName(), "связь восстановлена с \n"+name, TrayIcon.MessageType.INFO);
        }
    }
    private void setDisconnected(){
        if(connected == null){
            connected = true; //ставим наоборт чтобы сработал следующий if
        }

        applyDisplayMessage(disconnectedMessage);

        if(connected){
            connected = false;

            String ip = ipAddress.getText();
            String name = deviceName.getText();

            mainWindow.getLogger().info("потеряна связь с " + ip + " " + name);
            mainWindow.getTrayIcon().displayMessage(mainWindow.getAppName(), "потеряна связь с \n"+name, TrayIcon.MessageType.INFO);
        }
    }
    //</set>============================================================================================================

    void startChecking(){
        autoChecking.start();
    }
    void stopChecking(){
        autoChecking.start();
    }

    private void applyDisplayMessage(DisplayMessage displayMessageIn){
        deviceName.setForeground(displayMessageIn.getTextColor());
        ipAddress.setForeground(displayMessageIn.getTextColor());
        checking.setForeground(displayMessageIn.getTextColor());
        ipAddressLabel.setForeground(displayMessageIn.getTextColor());

        checking.setText(displayMessageIn.getMessageText());

        rootPanel.setBackground(displayMessageIn.getBackgroundColor());
        ipStatePanel.setBackground(displayMessageIn.getBackgroundColor());
        linesPanel.setBackground(displayMessageIn.getBackgroundColor());
        line1.setBackground(displayMessageIn.getBackgroundColor());
        line2.setBackground(displayMessageIn.getBackgroundColor());
        line3.setBackground(displayMessageIn.getBackgroundColor());
        line4.setBackground(displayMessageIn.getBackgroundColor());
    }

    //применяет настройки для виджета (вместе с внутренними виджетами)
    void applySettings(){
        deviceName.setText(deviceNameApply);
        ipAddress.setText(ipAddressApply);
        snmpPort = snmpPortApply;
        snmpCommunity = snmpCommunityApply;

        connectedMessage.applySettings();
        disconnectedMessage.applySettings();

        if(connected != null){
            if(connected){
                applyDisplayMessage(connectedMessage);
            }else{
                applyDisplayMessage(disconnectedMessage);
            }
        }

        line1.applySettings();
        line2.applySettings();
        line3.applySettings();
        line4.applySettings();
    }

    //сбрасывает не применненые настройки (вместе с внутренними виджетами)
    void discardSettings(){
        deviceNameApply = deviceName.getText();
        ipAddressApply = ipAddress.getText();
        snmpPortApply = snmpPort;
        snmpCommunityApply = snmpCommunity;

        connectedMessage.discardSettings();
        disconnectedMessage.discardSettings();

        line1.discardSettings();
        line2.discardSettings();
        line3.discardSettings();
        line4.discardSettings();
    }
}
