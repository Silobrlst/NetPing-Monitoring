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
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MainWindow extends JFrame implements CommandResponder {
    private JLabel appStatus;
    private JPanel netPingGrid;
    private JPanel rootPanel;
    private JButton settingsButton;
    private JTextField checkingDelay;

    private boolean trayIconVisible;

    private SettingsWindow settingsWindow;

    private final String appName = "NetPing мониторинг";
    private Logger logger;
    private SettingsLoader settingsLoader;
    private Snmp snmp;
    private TrayIcon trayIcon;

    private Map<String, NetPingWidget> ipMap;


    private void initTrayIcon() {
        PopupMenu trayMenu = new PopupMenu();

        MenuItem item = new MenuItem("Показать");
        item.addActionListener(e -> this.setVisible(true));
        trayMenu.add(item);

        item = new MenuItem("Настройки");
        item.addActionListener(e -> settingsWindow.setVisible(true));
        trayMenu.add(item);

        item = new MenuItem("Выход");
        item.addActionListener(e -> System.exit(0));
        trayMenu.add(item);

        Image icon = Toolkit.getDefaultToolkit().getImage("icon.png");
        trayIcon = new TrayIcon(icon, "Netping мониторинг", trayMenu);
    }

    private void initLogger() {
        logger = LogManager.getFormatterLogger("MainWindow");
    }

    private void init() {
        initLogger();

        snmp = null;

        ipMap = new HashMap<>();

        settingsLoader = new SettingsLoader("config.json");
        SnmpSettings snmpSettings = settingsLoader.getSnmpSettings();
        String address = snmpSettings.ipAddress + "/" + snmpSettings.snmpTrapsPort;

        GridLayout gridLayout = new GridLayout(settingsLoader.getGridRows(), settingsLoader.getGridCollumns());
        netPingGrid.setLayout(gridLayout);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (settingsLoader.getStyle().equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                    this.pack();
                    break;
                }
            }
        } catch (Exception ex) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        trayIconVisible = false;
        setTrayIconVisible(settingsLoader.isTrayIcon());

        checkingDelay.setText(Integer.toString(settingsLoader.getCheckingDelay()));

        settingsWindow = new SettingsWindow(this, settingsLoader, () -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (settingsLoader.getStyle().equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        SwingUtilities.updateComponentTreeUI(this);
                        this.pack();
                        break;
                    }
                }
            } catch (Exception ex) {
                // If Nimbus is not available, you can set the GUI to another look and feel.
            }

            setTrayIconVisible(settingsLoader.isTrayIcon());

            List<String> ipAddresses = settingsLoader.getNetPingIpAddresses();

            //удаляем лишние
            for (String ip: ipMap.keySet()) {
                if (!ipAddresses.contains(ip)) {
                    deleteNetping(ip);
                }
            }

            //добавляем новые или изменяем существующие
            for (String ip : ipAddresses) {
                netPingGrid.add(settingsLoader.loadNetPing(this, ip));
                netPingGrid.revalidate();
                netPingGrid.repaint();
            }

            this.pack();
        });

        settingsButton.addActionListener(e -> settingsWindow.setVisible(true));

        //при завершении програмыы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("мониторинг остановлен")));

        try {
            this.listen(new UdpAddress(address));
        } catch (IOException e) {
            System.err.println("Error in Listening for Trap");
            System.err.println("Exception Message = " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error in Listening for Trap", JOptionPane.ERROR_MESSAGE);
        }
    }


    void updateStyle(){
        settingsWindow.updateStyle();
        this.pack();
    }


    private MainWindow() {
        this.setTitle(appName);

        initTrayIcon();

        checkingDelay.addActionListener(e -> {
            if (checkingDelay.getText().matches("^[1-9]\\d*")) {
                int delay = Integer.parseInt(checkingDelay.getText());

                settingsLoader.setCheckTime(delay);
                settingsLoader.saveConfig();
            } else {
                checkingDelay.setText(Integer.toString(settingsLoader.getCheckingDelay()));
            }
        });

        appStatus.setText("Запуск...");

        this.getContentPane().add(rootPanel);
        this.pack();
        this.setVisible(true);

        this.init();
    }

    public static void main(String[] args) {
        new MainWindow();
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
        target.setCommunity(new OctetString(settingsLoader.getSnmpSettings().community));

        TransportMapping transportGet = new DefaultUdpTransportMapping();
        transportGet.listen();
        snmp = new Snmp(mtDispatcher, transportGet);
        snmp.addCommandResponder(this);

        //<init netping widgets>==========================
        List<String> ipAddresses = settingsLoader.getNetPingIpAddresses();
        for (String ip : ipAddresses) {
            NetPingWidget netPingWidget = settingsLoader.loadNetPing(this, ip);
            ipMap.put(ip, netPingWidget);
            netPingGrid.add(netPingWidget);
            netPingGrid.revalidate();
            netPingGrid.repaint();
        }
        this.pack();
        //</init netping widgets>=========================

        transport.listen();
        String message = "прием SNMP-ловушек: " + address;
        logger.info("мониторинг запущен " + address);
        trayIcon.displayMessage(appName, "запущен", TrayIcon.MessageType.INFO);
        appStatus.setText(message);

        try {
            this.wait();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void lineReceiveTrap(IOLineWidget ioLineWidgetIn, PDU pduIn){
        if (pduIn.getVariable(ioLineWidgetIn.getTrapReceiveOID()) != null) {
            int opened = pduIn.getVariable(ioLineWidgetIn.getTrapReceiveOID()).toInt();

            if (opened == 0) {
                ioLineWidgetIn.set0();
            } else {
                ioLineWidgetIn.set1();
            }
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


    private void setTrayIconVisible(boolean visibleIn){
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


    private void deleteNetping(String ipAddressIn) {
        String name = ipMap.get(ipAddressIn).getDeviceName();
        netPingGrid.remove(ipMap.get(ipAddressIn));
        netPingGrid.revalidate();
        netPingGrid.repaint();
        this.pack();

        logger.info("удалён netping " + ipAddressIn + " " + name);
    }


    public String getAppName(){
        return appName;
    }

    public Snmp getSnmp(){
        return snmp;
    }

    public SettingsLoader getSettingsLoader(){
        return settingsLoader;
    }

    public Logger getLogger(){
        return logger;
    }

    public TrayIcon getTrayIcon(){
        return trayIcon;
    }

    public int getDelay(){
        return Integer.parseInt(checkingDelay.getText());
    }
}
