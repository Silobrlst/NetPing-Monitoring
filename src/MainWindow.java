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
    private JPanel netPingGrid;
    private JPanel rootPanel;
    private JButton settingsButton;

    private boolean trayIconVisible;

    private SettingsDialog settingsDialog;

    private final String appName = "NetPing мониторинг";
    private Logger logger;
    private Snmp snmp;
    private TrayIcon trayIcon;

    private Map<String, NetPingWidget> ipMap;

    private String community;

    private Integer checkingDelay;
    private Integer snmpRetries;
    private Integer snmpTimeOut;

    private GridLayout gridLayout;

    private String receiveTrapsIpAddress;
    private String receiveTrapsPort;


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

        Image icon = Toolkit.getDefaultToolkit().getImage("icon.png");
        trayIcon = new TrayIcon(icon, "Netping мониторинг", trayMenu);
    }

    private void init() {
        logger = LogManager.getFormatterLogger("MainWindow");

        snmp = null;

        ipMap = new HashMap<>();

        receiveTrapsIpAddress = "0.0.0.0";
        receiveTrapsPort = "162";
        String address = receiveTrapsIpAddress + "/" + receiveTrapsPort;

        gridLayout = new GridLayout();
        netPingGrid.setLayout(gridLayout);

        trayIconVisible = false;
        checkingDelay = 60;
        snmpRetries = 4;
        snmpTimeOut = 3;

        MainWindow mainWindowContext = this;

        settingsDialog = new SettingsDialog(this){
            @Override
            public void applied() {
                mainWindowContext.setStyle(settingsDialog.getStyle());
            }
        };

        settingsButton.addActionListener(e -> settingsDialog.setVisible(true));

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


    public MainWindow() {
        community = "SWITCH";

        this.setTitle(appName);

        initTrayIcon();

        appStatus.setText("Запуск...");

        this.getContentPane().add(rootPanel);
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.init();
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

        TransportMapping transportGet = new DefaultUdpTransportMapping();
        transportGet.listen();
        snmp = new Snmp(mtDispatcher, transportGet);
        snmp.addCommandResponder(this);

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


    public void setReceiveAddress(String receiveTrapsIpAddressIn, String receiveTrapsPortIn){
        receiveTrapsIpAddress = receiveTrapsIpAddressIn;
        receiveTrapsPort = receiveTrapsPortIn;
    }

    public void setTrayIconVisible(boolean visibleIn){
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

    public void setStyle(String styleIn){
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (styleIn.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                    settingsDialog.updateStyle();
                    this.pack();
                    break;
                }
            }
        } catch (Exception ex) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
    }

    public void setCommunity(String communityIn){
        community = communityIn;
    }

    public String getCommunity(){
        return community;
    }

    public boolean getTrayIconVisible(){
        return trayIconVisible;
    }

    public void setGridSize(int columnsIn, int rowsIn){
        gridLayout.setColumns(columnsIn);
        gridLayout.setRows(rowsIn);
    }


    public void addNetPingWidget(NetPingWidget netPingWidgetIn){
        ipMap.put(netPingWidgetIn.getIpAddress(), netPingWidgetIn);
        netPingGrid.add(netPingWidgetIn);
        netPingGrid.revalidate();
        netPingGrid.repaint();
        this.pack();
    }


    public Integer getSnmpTimeOut(){
        return snmpTimeOut;
    }
    public Integer getSnmpRetries(){
        return snmpRetries;
    }
    public Integer getCheckingDelay(){
        return checkingDelay;
    }
    public String getStyle(){
        return UIManager.getLookAndFeel().getName();
    }
    public String getAppName(){
        return appName;
    }
    public Snmp getSnmp(){
        return snmp;
    }
    public Logger getLogger(){
        return logger;
    }
    public TrayIcon getTrayIcon(){
        return trayIcon;
    }

    public Collection<NetPingWidget> getNetPingWidgets(){
        return ipMap.values();
    }
}
