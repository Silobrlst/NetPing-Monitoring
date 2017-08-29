package netpingmon;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.Option;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
    private IoLineWidget line1;
    private IoLineWidget line2;
    private IoLineWidget line3;
    private IoLineWidget line4;

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

    private JPopupMenu popup = new JPopupMenu();

    private AddEditNetPingDialog addEditNetPingDialog;

    private GridLayout gridLayout = new GridLayout();

    private static final String messageToolTipText = "<html>Состояние связи с NetPing:<Br>";

    private boolean snmpInitialized = false;
    private boolean active = DefaultSettings.active;

    NetPingWidget(MainWindow mainWindowIn, String ipAddressIn){
        mainWindow = mainWindowIn;

        addEditNetPingDialog = new AddEditNetPingDialog(mainWindow);

        deviceName.setText("");
        ipAddress.setText(ipAddressIn);

        ipAddressApply = ipAddress.getText();
        deviceNameApply = deviceName.getText();

        line1 = new IoLineWidget(this, "1");
        line2 = new IoLineWidget(this, "2");
        line3 = new IoLineWidget(this, "3");
        line4 = new IoLineWidget(this, "4");

        linesPanel.setLayout(gridLayout);
        linesPanel.add(line1);
        linesPanel.add(line2);
        linesPanel.add(line3);
        linesPanel.add(line4);

        connectedMessage.setBackgroundColor(DefaultSettings.backgroundColorConnected);
        connectedMessage.setTextColor(DefaultSettings.textColorConnected);
        connectedMessage.applySettings();

        disconnectedMessage.setBackgroundColor(DefaultSettings.backgroundColorDisconnected);
        disconnectedMessage.setTextColor(DefaultSettings.textColorDisconnected);
        disconnectedMessage.applySettings();

        this.add(rootPanel);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        deviceName.addMouseListener(mouseAdapter);
        ipAddress.addMouseListener(mouseAdapter);
        checking.addMouseListener(mouseAdapter);
        ipAddressLabel.addMouseListener(mouseAdapter);
        //</подсветка при навдении курсора>=============================================================================

        MouseListener popupListener = new PopupListener();
        rootPanel.addMouseListener(popupListener);
        deviceName.addMouseListener(popupListener);
        ipAddress.addMouseListener(popupListener);
        checking.addMouseListener(popupListener);
        ipAddressLabel.addMouseListener(popupListener);

        JMenuItem checkItem = new JMenuItem("Проверить");
        JMenuItem editItem = new JMenuItem("Изменить");
        popup.add(checkItem);
        popup.add(editItem);

        Runnable checkFunction = () -> {
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
        };

        autoChecking = new AutoChecking(checkFunction, mainWindowIn.getCheckingDelay());

        NetPingWidget context = this;
        checkItem.addActionListener(e -> new Thread(checkFunction).start());
        editItem.addActionListener(e -> {
            rootPanel.setBorder(etchedBorder);
            addEditNetPingDialog.setEditing(this);
            if(addEditNetPingDialog.open() == JOptionPane.OK_OPTION){
                mainWindow.saveOnlyNetPing(context);
            }
            rootPanel.setBorder(originalBorder);
            context.repaint();
        });
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
    String getGridType(){
        if(gridLayout.getColumns() == 1 && gridLayout.getRows() == 4){
            return "1x4";
        }else if(gridLayout.getColumns() == 2 && gridLayout.getRows() == 2){
            return "2x2";
        }else if(gridLayout.getColumns() == 4 && gridLayout.getRows() == 1){
            return "4x1";
        }

        return "1x4";
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
    IoLineWidget getLine(String lineNumberIn){
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

    String getLoggingName(){
        return getIpAddress() + " \"" + getDeviceName() + "\"";
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
    void setCheckingDelay(int delayIn){
        autoChecking.setDelay(delayIn);

        line1.setCheckingDelay(delayIn);
        line2.setCheckingDelay(delayIn);
        line3.setCheckingDelay(delayIn);
        line4.setCheckingDelay(delayIn);
    }
    void setGridType(String typeIn){
        if(!typeIn.equals("1x4") && !typeIn.equals("2x2") && !typeIn.equals("4x1")){
            typeIn = "1x4";
        }

        switch(typeIn){
            case "2x2":
                gridLayout.setColumns(2);
                gridLayout.setRows(2);
                break;
            case "4x1":
                gridLayout.setColumns(4);
                gridLayout.setRows(1);
                break;
            case "1x4":
            default:
                gridLayout.setColumns(1);
                gridLayout.setRows(4);
                break;
        }

        revalidate();
        repaint();
    }

    void setLineActive(String lineNumberIn, boolean activeIn){
        IoLineWidget ioLineWidget = this.getLine(lineNumberIn);
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
        if(activeIn && !active){
            if(snmpInitialized){
                this.setVisible(true);

                autoChecking.start();

                mainWindow.getLogger().info(getLoggingName() + ": мониторинг устройства NetPing запущен");
            }
        }else if(!activeIn && active){
            this.setVisible(false);

            autoChecking.stop();

            mainWindow.getLogger().info(getLoggingName() + ": мониторинг устройства NetPing остановлен");
        }

        active = activeIn;
    }

    //изменение состояния связи с NetPing
    private void setConnected(){
        if(connected == null){
            connected = false; //ставим наоборт чтобы сработал следующий if
        }

        applyDisplayMessage(connectedMessage);

        if(!connected){
            connected = true;

            mainWindow.getLogger().info("установлена связь с " + getLoggingName());
            mainWindow.getTrayIcon().displayMessage(mainWindow.getAppName(), "установлена связь с \n"+getLoggingName(), TrayIcon.MessageType.INFO);
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

            mainWindow.getLogger().info("потеряна связь с " + getLoggingName());
            mainWindow.getTrayIcon().displayMessage(mainWindow.getAppName(), "потеряна связь с \n"+getLoggingName(), TrayIcon.MessageType.INFO);
        }
    }
    //</set>============================================================================================================

    boolean isActive(){
        return active;
    }

    //копирует все настройки (кроме ip-адрес) этого netPing'а в netPingWidgetIn
    void copyTo(NetPingWidget netPingWidgetIn){
        netPingWidgetIn.setDeviceName(getDeviceName());
        netPingWidgetIn.setSnmpCommunity(getSnmpCommunity());
        netPingWidgetIn.setSnmpPort(getSnmpPort());
        netPingWidgetIn.setGridType(getGridType());

        connectedMessage.copyTo(netPingWidgetIn.getConnectedMessage());
        disconnectedMessage.copyTo(netPingWidgetIn.getDisconnectedMessage());

        line1.copyTo(netPingWidgetIn.getLine("1"));
        line2.copyTo(netPingWidgetIn.getLine("2"));
        line3.copyTo(netPingWidgetIn.getLine("3"));
        line4.copyTo(netPingWidgetIn.getLine("4"));

        netPingWidgetIn.applySettings();
    }

    void snmpInitialized(){
        snmpInitialized = true;

        if(active){
            autoChecking.start();
        }

        line1.snmpInitialized();
        line2.snmpInitialized();
        line3.snmpInitialized();
        line4.snmpInitialized();
    }

    private void applyDisplayMessage(DisplayMessage displayMessageIn){
        deviceName.setForeground(displayMessageIn.getTextColor());
        ipAddress.setForeground(displayMessageIn.getTextColor());
        checking.setForeground(displayMessageIn.getTextColor());
        ipAddressLabel.setForeground(displayMessageIn.getTextColor());

        checking.setText(displayMessageIn.getMessageText());
        checking.setToolTipText(messageToolTipText + displayMessageIn.getMessageText() + "</html>");

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

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        addEditNetPingDialog.updateStyle();

        line1.updateStyle();
        line2.updateStyle();
        line3.updateStyle();
        line4.updateStyle();
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
