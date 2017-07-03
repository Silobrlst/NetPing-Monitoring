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

public class MainWindow extends JFrame implements CommandResponder {
    private JLabel appStatus;
    private JPanel netpingGrid;
    private JPanel rootPanel;
    private JButton settingsButton;
    private JTextField checkTime;

    private final String appName = "Netping мониторинг";
    private TrayIcon trayIcon;
    private boolean trayIconVisible;

    private SettingsWindow settingsWindow;

    private Logger logger;

    private SettingsLoader settingsLoader;
    private OID oid;

    private Snmp snmp;
    private Map<String, NetpingWidget> ipMap;


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
        oid = new OID(snmpSettings.trapOID);

        GridLayout gridLayout = new GridLayout(settingsLoader.getGridRows(), settingsLoader.getGridCollumns());
        netpingGrid.setLayout(gridLayout);

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

        checkTime.setText(Integer.toString(settingsLoader.getCheckDelay()));

        settingsWindow = new SettingsWindow(settingsLoader, () -> {
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

            Map<String, String> map = settingsLoader.getNetpingIpNameMap();

            //удаляем лишние
            for (String ip : ipMap.keySet()) {
                if (!map.containsKey(ip)) {
                    deleteNetping(ip);
                }
            }

            //добавляем новые или изменяем существующие
            for (String ip : map.keySet()) {
                setNetping(ip, map.get(ip));
            }
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


    private MainWindow() {
        this.setTitle(appName);

        initTrayIcon();

        checkTime.addActionListener(e -> {
            if (checkTime.getText().matches("^[1-9]\\d*")) {
                int delay = Integer.parseInt(checkTime.getText());

                settingsLoader.setCheckTime(delay);
                settingsLoader.saveConfig();
            } else {
                checkTime.setText(Integer.toString(settingsLoader.getCheckDelay()));
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

        Map<String, String> ipNameMap = settingsLoader.getNetpingIpNameMap();
        for (String ip : ipNameMap.keySet()) {
            NetpingWidget netpingWidget = new NetpingWidget(ip, ipNameMap.get(ip), this);

            ipMap.put(ip, netpingWidget);
            netpingGrid.add(netpingWidget);
            netpingGrid.revalidate();
            netpingGrid.repaint();
            this.pack();
        }

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

    public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
        PDU pdu = cmdRespEvent.getPDU();
        if (pdu != null) {

            if (pdu.getVariable(oid) != null) {
                int opened = pdu.getVariable(oid).toInt();

                String ipPort = cmdRespEvent.getPeerAddress().toString();
                String ip = ipPort.split("/")[0];
                NetpingWidget netpingWidget = ipMap.get(ip);

                if (opened == 0) {
                    netpingWidget.setState(NetpingStateEnum.Opened);
                } else {
                    netpingWidget.setState(NetpingStateEnum.Closed);
                }
            }

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


    private void setNetping(String ipAddressIn, String nameIn) {
        if (ipMap.containsKey(ipAddressIn)) {
            if (ipMap.get(ipAddressIn).getDeviceName().compareTo(nameIn) != 0) {
                String oldName = ipMap.get(ipAddressIn).getDeviceName();
                ipMap.get(ipAddressIn).setDeviceName(nameIn);
                logger.info("изменено имя netping " + ipAddressIn + " с " + oldName + " на " + nameIn);
            }
        } else {
            NetpingWidget netping = new NetpingWidget(ipAddressIn, nameIn, this);
            netpingGrid.add(netping);
            netpingGrid.revalidate();
            netpingGrid.repaint();
            this.pack();
            ipMap.put(ipAddressIn, netping);

            logger.info("добавлен netping " + ipAddressIn + " " + nameIn);
        }
    }

    private void deleteNetping(String ipAddressIn) {
        String name = ipMap.get(ipAddressIn).getDeviceName();
        netpingGrid.remove(ipMap.get(ipAddressIn));
        netpingGrid.revalidate();
        netpingGrid.repaint();
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
}
