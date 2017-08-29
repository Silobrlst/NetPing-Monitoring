package netpingmon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.*;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.*;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;
import org.snmp4j.tools.console.SnmpRequest;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MainWindow extends JFrame implements CommandResponder {
    private JLabel appStatus;
    private JPanel netPingGrid;
    private JPanel rootPanel;
    private JButton settingsButton;
    private JButton logsButton;
    private GridLayout gridLayout = new GridLayout();

    private final String appName = "NetPing мониторинг";
    private Logger logger = LogManager.getFormatterLogger("MainWindow");
    private Snmp snmp = null;
    private TrayIcon trayIcon;
    private Map<String, NetPingWidget> ipMap = new HashMap<>();
    private boolean trayIconVisible = DefaultSettings.trayIcon;

    private Integer receiveTrapsPort;
    private Integer snmpPort;
    private String community = DefaultSettings.snmpCommunity;
    private Integer checkingDelay = DefaultSettings.checkingDelay;
    private Integer retries = DefaultSettings.retries;
    private Integer timeOut = DefaultSettings.timeOut;

    private SettingsDialog settingsDialog;
    private SettingsLoader settingsLoader;

    private GuiSaver guiSaver = new GuiSaver(this, "MainWindow");

    private LogsWindow logsWindow = new LogsWindow();

    private final Image appIcon = Toolkit.getDefaultToolkit().getImage("appIcon.png");

    private void initTrayIcon() {
        PopupMenu trayMenu = new PopupMenu();

        MenuItem item = new MenuItem("Показать");
        item.addActionListener(e -> this.setVisible(true));
        trayMenu.add(item);

        item = new MenuItem("Настройки");
        item.addActionListener(e -> settingsDialog.setVisible(true));
        trayMenu.add(item);

        item = new MenuItem("Выход");
        item.addActionListener(e -> System.exit(0));
        trayMenu.add(item);

        trayIcon = new TrayIcon(appIcon, appName, trayMenu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> this.setVisible(true));
    }

    private void init() {
        netPingGrid.setLayout(gridLayout);

        MainWindow mainWindowContext = this;
        settingsDialog = new SettingsDialog(this){
            @Override
            public void appliedAll() {
                mainWindowContext.setStyle(settingsDialog.getStyle());
                mainWindowContext.setTrayIconVisible(settingsDialog.getTrayIconVisible());
                mainWindowContext.setSnmpCommunity(settingsDialog.getSnmpCommunity());
                mainWindowContext.setCheckingDelay(settingsDialog.getCheckingDelay());
                mainWindowContext.setRetries(settingsDialog.getRetries());
                mainWindowContext.setTimeOut(settingsDialog.getTimeOut());
                mainWindowContext.setGridSize(settingsDialog.getGridColumns(), settingsDialog.getGridRows());

                settingsLoader.saveSettings(settingsDialog);
            }

            @Override
            public void addedNetPing(NetPingWidget netPingWidgetIn) {
                addNetPingWidget(netPingWidgetIn);
                netPingWidgetIn.repaint();
            }

            @Override
            public void removedNetPings(List<NetPingWidget> netPingWidgetsIn) {
                netPingWidgetsIn.forEach(netPingWidget -> {
                    removeNetPingWidget(netPingWidget);
                    netPingWidget.repaint();
                });
            }

            @Override
            public void changedNetPing(NetPingWidget netPingWidgetIn) {
                //обновляем ip-адреса в ipMap
                ipMap.clear();
                for(NetPingWidget netPingWidget: settingsDialog.getNetPingWidgets()){
                    ipMap.put(netPingWidget.getIpAddress(), netPingWidget);
                }

                for(NetPingWidget netPingWidget: ipMap.values()){
                    netPingWidget.repaint();
                }
            }
        };

        settingsButton.addActionListener(e -> settingsDialog.setVisible(true));

        //при завершении програмыы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("мониторинг остановлен")));
    }

    void startListen(){
        try {
            this.listen(new UdpAddress(receiveTrapsPort));
        } catch (IOException e) {
            System.err.println("Error in Listening for Trap");
            System.err.println("Exception Message = " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error in Listening for Trap", JOptionPane.ERROR_MESSAGE);
        }
    }


    MainWindow(SettingsLoader settingsLoaderIn, Integer receiveTrapsPortIn, Integer snmpPortIn) {
        settingsLoader = settingsLoaderIn;
        receiveTrapsPort = receiveTrapsPortIn;
        snmpPort = snmpPortIn;

        this.setTitle(appName);
        setIconImage(appIcon);
        appStatus.setText("Запуск...");

        initTrayIcon();

        logsButton.addActionListener(e -> logsWindow.open());

        this.getContentPane().add(rootPanel);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();

        guiSaver.saveWindowMaximized(true);
        guiSaver.load();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                guiSaver.save();
            }
        });

        this.init();
    }

    private void lineReceiveTrap(IoLineWidget ioLineWidgetIn, PDU pduIn){
        if (pduIn.getVariable(ioLineWidgetIn.getTrapReceiveOID()) != null) {
            int opened = pduIn.getVariable(ioLineWidgetIn.getTrapReceiveOID()).toInt();

            if (opened == 0) {
                ioLineWidgetIn.set0();
            } else {
                ioLineWidgetIn.set1();
            }
        }
    }
    private synchronized void listen(TransportIpAddress address) throws IOException {
        AbstractTransportMapping transport;
        if (address instanceof TcpAddress) {
            transport = new DefaultTcpTransportMapping((TcpAddress) address);
        } else {
            transport = new DefaultUdpTransportMapping((UdpAddress) address);
        }

        ThreadPool threadPool = ThreadPool.create("DispatcherPool", 10);
        MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

        // add message processing models
        mtDispatcher.addMessageProcessingModel(new MPv1());
        mtDispatcher.addMessageProcessingModel(new MPv2c());

        // add all security protocols
        SecurityProtocols.getInstance().addDefaultProtocols();
        SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

        //Create Target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));

        TransportMapping transportGet = new DefaultUdpTransportMapping(new UdpAddress(snmpPort));
        transportGet.listen();
        snmp = new Snmp(mtDispatcher, transportGet);
        snmp.addCommandResponder(this);

        transport.listen();
        String message = "прием SNMP-ловушек: " + address + ", SNMP: " + transportGet.getListenAddress().toString();
        logger.info("мониторинг запущен, прием ловушек: " + address + ", SNMP: " + transportGet.getListenAddress().toString());
        trayIcon.displayMessage(appName, "запущен", TrayIcon.MessageType.INFO);
        appStatus.setText(message);

        ipMap.values().forEach(netPingWidget -> netPingWidget.snmpInitialized());

        try {
            this.wait();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
        PDU pdu = cmdRespEvent.getPDU();
        if (pdu != null) {
            String ipPort = cmdRespEvent.getPeerAddress().toString();
            String ip = ipPort.split("/")[0];
            NetPingWidget netPingWidget = ipMap.get(ip);

            lineReceiveTrap(netPingWidget.getLine("1"), pdu);
            lineReceiveTrap(netPingWidget.getLine("2"), pdu);
            lineReceiveTrap(netPingWidget.getLine("3"), pdu);
            lineReceiveTrap(netPingWidget.getLine("4"), pdu);

            int pduType = pdu.getType();
            if ((pduType != PDU.TRAP) && (pduType != PDU.V1TRAP) && (pduType != PDU.REPORT)
                    && (pduType != PDU.RESPONSE)) {
                pdu.setErrorIndex(0);
                pdu.setErrorStatus(0);
                pdu.setType(PDU.RESPONSE);
                StatusInformation statusInformation = new StatusInformation();
                StateReference ref = cmdRespEvent.getStateReference();
                try {
                    System.out.println(cmdRespEvent.getPDU());
                    cmdRespEvent.getMessageDispatcher().returnResponsePdu(cmdRespEvent.getMessageProcessingModel(),
                            cmdRespEvent.getSecurityModel(), cmdRespEvent.getSecurityName(), cmdRespEvent.getSecurityLevel(),
                            pdu, cmdRespEvent.getMaxSizeResponsePDU(), ref, statusInformation);
                } catch (MessageException ex) {
                    System.err.println("Error while sending response: " + ex.getMessage());
                    LogFactory.getLogger(SnmpRequest.class).error(ex);
                }
            }
        }
    }

    boolean isNetPingExist(String ipAddressIn){
        return ipMap.containsKey(ipAddressIn);
    }

    //<set>=============================================================================================================
    void setCheckingDelay(Integer checkingDelayIn){
        checkingDelay = checkingDelayIn;

        for(NetPingWidget netPingWidget: ipMap.values()){
            netPingWidget.setCheckingDelay(checkingDelay);
        }
    }
    void setTimeOut(Integer timeOutIn){
        timeOut = timeOutIn;
    }
    void setRetries(Integer retriesIn){
        retries = retriesIn;
    }
    void setSnmpCommunity(String communityIn){
        community = communityIn;
    }
    void setTrayIconVisible(boolean visibleIn){
        SystemTray tray = SystemTray.getSystemTray();

        if(visibleIn){
            try {
                if(!trayIconVisible){
                    tray.add(trayIcon);
                    trayIconVisible = true;
                }
            } catch (AWTException e) {
                e.printStackTrace();
            }

            this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }else{
            tray.remove(trayIcon);
            trayIconVisible = false;
            this.setVisible(true);
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
    }
    void setStyle(String styleIn){
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (styleIn.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                    settingsDialog.updateStyle();
                    logsWindow.updateStyle();

                    for(NetPingWidget netPingWidget: ipMap.values()){
                        netPingWidget.updateStyle();
                    }

                    break;
                }
            }
        } catch (Exception ex) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
    }
    void setGridSize(int columnsIn, int rowsIn){
        gridLayout.setColumns(columnsIn);
        gridLayout.setRows(rowsIn);
    }
    //</set>============================================================================================================

    private void addNetPingWidget(NetPingWidget netPingWidgetIn){
        ipMap.put(netPingWidgetIn.getIpAddress(), netPingWidgetIn);
        netPingGrid.add(netPingWidgetIn);
        netPingGrid.revalidate();
        netPingGrid.repaint();
    }
    void addNetPingWidgets(Collection<NetPingWidget> netPingWidgetsIn){
        for(NetPingWidget netPingWidget: netPingWidgetsIn){
            ipMap.put(netPingWidget.getIpAddress(), netPingWidget);
            netPingGrid.add(netPingWidget);
        }

        netPingGrid.revalidate();
        netPingGrid.repaint();
    }
    private void removeNetPingWidget(NetPingWidget netPingWidgetIn){
        netPingWidgetIn.setActive(false);
        ipMap.remove(netPingWidgetIn.getIpAddress());
        netPingGrid.remove(netPingWidgetIn);
        netPingGrid.revalidate();
        netPingGrid.repaint();
    }

    //<get>=============================================================================================================
    Integer getSnmpPort(){
        return snmpPort;
    }
    String getSnmpCommunity(){
        return community;
    }
    Integer getReceiveTrapsPort(){
        return receiveTrapsPort;
    }
    Integer getTimeOut(){
        revalidate();
        repaint();
        return timeOut;
    }
    Integer getRetries(){
        return retries;
    }
    Integer getCheckingDelay(){
        return checkingDelay;
    }
    String getStyle(){
        return UIManager.getLookAndFeel().getName();
    }
    String getAppName(){
        return appName;
    }
    Snmp getSnmp(){
        return snmp;
    }
    Logger getLogger(){
        return logger;
    }
    TrayIcon getTrayIcon(){
        return trayIcon;
    }
    boolean getTrayIconVisible(){
        return trayIconVisible;
    }
    int getGridColumns(){
        return gridLayout.getColumns();
    }
    int getGridRows(){
        return gridLayout.getRows();
    }
    Collection<NetPingWidget> getNetPingWidgets(){
        return ipMap.values();
    }
    //</get>============================================================================================================

    void saveOnlyNetPing(NetPingWidget netPingWidgetIn){
        settingsLoader.saveOnlyNetPing(netPingWidgetIn);
    }
    void saveOnlyIoLine(NetPingWidget netPingWidgetIn, IoLineWidget ioLineWidgetIn){
        settingsLoader.saveOnlyIoLine(netPingWidgetIn, ioLineWidgetIn);
    }
}
