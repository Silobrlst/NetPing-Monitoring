import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
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
import javax.swing.Timer;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindow extends JFrame implements CommandResponder {
    private JLabel appStatus;
    private JPanel netpingGrid;
    private JPanel rootPanel;
    private JButton settingsButton;
    private JTextField checkTime;

    private Timer checkTimer;

    private SettingsWindow settingsWindow;

    private Logger logger;

    private SettingsLoader settingsLoader;
    private OID oid;
    private OID getIO1oid;

    private Snmp snmp;
    private Map<String, NetpingWidget> ipMap;
    private ExecutorService executor;


    private void initLogger(){
        logger = LogManager.getFormatterLogger("MainWindow");
    }

    private void init(){
        initLogger();

        snmp = null;

        settingsLoader = new SettingsLoader("config.json");
        SnmpSettings snmpSettings = settingsLoader.getSnmpSettings();
        String address = snmpSettings.ipAddress + "/" + snmpSettings.snmpTrapsPort;
        oid = new OID(snmpSettings.trapOID);
        getIO1oid = new OID(snmpSettings.getIo1OID);

        checkTime.setText(Integer.toString(settingsLoader.getCheckTime()));

        ipMap = settingsLoader.getNetpingWidgetsMap();
        for(String ip: ipMap.keySet()){
            netpingGrid.add(ipMap.get(ip));
            netpingGrid.revalidate();
            netpingGrid.repaint();
            this.pack();
        }

        settingsWindow = new SettingsWindow(settingsLoader, () -> {
            Map<String, String> map = settingsLoader.getNetpingIpNameMap();

            //удаляем лишние
            for(String ip: ipMap.keySet()){
                if(!map.containsKey(ip)){
                    deleteNetping(ip);
                }
            }

            //добавляем новые или изменяем существующие
            for(String ip: map.keySet()){
                setNetping(ip, map.get(ip));
            }
        });

        settingsButton.addActionListener(e -> settingsWindow.setVisible(true));

        try {
            this.listen(new UdpAddress(address));
        } catch (IOException e) {
            System.err.println("Error in Listening for Trap");
            System.err.println("Exception Message = " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error in Listening for Trap", JOptionPane.ERROR_MESSAGE);
        }
    }


    private MainWindow(){
        this.setTitle("Netping мониторинг");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        checkTime.addActionListener(e -> {
            int delay = Integer.parseInt(checkTime.getText());

            settingsLoader.setCheckTime(delay);
            settingsLoader.saveConfig();
            checkTimer.setDelay(delay*1000);
        });

        GridLayout gridLayout = new GridLayout(5, 5);
        netpingGrid.setLayout(gridLayout);

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

        snmp = new Snmp(mtDispatcher, transport);
        snmp.addCommandResponder(this);

        transport.listen();
        String message = "прием SNMP-ловушек: " + address;
        logger.info("запущен мониторинг " + address);
        System.out.println(message);
        appStatus.setText(message);

        executor = Executors.newFixedThreadPool(25);
        checkAll();

        checkTimer = new Timer(settingsLoader.getCheckTime()*1000, e -> checkAll(false));
        checkTimer.start();

        try {
            this.wait();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
        System.out.println("Received PDU...");
        PDU pdu = cmdRespEvent.getPDU();
        if (pdu != null) {

            if(pdu.getVariable(oid) != null){
                int opened = pdu.getVariable(oid).toInt();

                String ipPort = cmdRespEvent.getPeerAddress().toString();
                String ip = ipPort.split("/")[0];

                if(opened == 0){
                    setNetpingState(ip, NetpingStateEnum.Opened);
                }else{
                    setNetpingState(ip, NetpingStateEnum.Closed);
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


    private void setNetping(String ipAddressIn, String nameIn){
        if(ipMap.containsKey(ipAddressIn)){
            if(ipMap.get(ipAddressIn).getDeviceName().compareTo(nameIn) != 0){
                String oldName = ipMap.get(ipAddressIn).getDeviceName();
                ipMap.get(ipAddressIn).setDeviceName(nameIn);
                logger.info("изменено имя netping " + ipAddressIn + " с " + oldName + " на " + nameIn);
            }
        }else{
            NetpingWidget netping = new NetpingWidget(ipAddressIn, nameIn);
            netpingGrid.add(netping);
            netpingGrid.revalidate();
            netpingGrid.repaint();
            this.pack();
            ipMap.put(ipAddressIn, netping);

            logger.info("добавлен netping " + ipAddressIn + " " + nameIn);

            if(snmp != null){
                checkNetping(ipAddressIn);
            }
        }
    }

    private void deleteNetping(String ipAddressIn){
        String name = ipMap.get(ipAddressIn).getDeviceName();
        netpingGrid.remove(ipMap.get(ipAddressIn));
        netpingGrid.revalidate();
        netpingGrid.repaint();
        this.pack();

        logger.info("удалён netping " + ipAddressIn + " " + name);
    }

    private void setNetpingState(String ipAddressIn, NetpingStateEnum stateIn){
        NetpingWidget netpingWidget = ipMap.get(ipAddressIn);
        NetpingStateEnum state = netpingWidget.getState();

        if(stateIn != state){
            ipMap.get(ipAddressIn).setState(stateIn);

            switch (stateIn){
                case Opened:
                    logger.info("открыт шкаф " + ipAddressIn + " " + netpingWidget.getDeviceName());
                    break;
                case Closed:
                    logger.info("закрыт шкаф " + ipAddressIn + " " + netpingWidget.getDeviceName());
                    break;
                case Disconneted:
                    logger.info("нет связи с " + ipAddressIn + " " + netpingWidget.getDeviceName());
                    break;
            }
        }
    }

    private void setNetpingChecking(String ipAddressIn){
        ipMap.get(ipAddressIn).setChecking();
    }


    private void checkNetping(String ipAddressIn, boolean checkingIndicationIn) {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        pdu.setRequestID(new Integer32(1));

        // Create Target Address object
        CommunityTarget comtarget = new CommunityTarget();
        comtarget.setCommunity(new OctetString(settingsLoader.getSnmpSettings().community));
        comtarget.setVersion(SnmpConstants.version1);
        comtarget.setAddress(new UdpAddress(ipAddressIn + "/" + settingsLoader.getSnmpSettings().snmpPort));
        comtarget.setRetries(2);
        comtarget.setTimeout(1000);

        System.out.println("Sending Request to Agent...");
        if(checkingIndicationIn){
            setNetpingChecking(ipAddressIn);
        }

        try{
            ResponseEvent response = snmp.get(pdu, comtarget);

            // Process Agent Response
            if (response != null)
            {
                System.out.println("Got Response from Agent");
                PDU responsePDU = response.getResponse();

                if (responsePDU != null)
                {
                    int errorStatus = responsePDU.getErrorStatus();
                    int errorIndex = responsePDU.getErrorIndex();
                    String errorStatusText = responsePDU.getErrorStatusText();

                    if (errorStatus == PDU.noError)
                    {
                        if(responsePDU.getVariable(getIO1oid) != null) {
                            int opened = responsePDU.getVariable(getIO1oid).toInt();

                            if(opened == 0){
                                setNetpingState(ipAddressIn, NetpingStateEnum.Closed);
                            }else{
                                setNetpingState(ipAddressIn, NetpingStateEnum.Opened);
                            }
                        }
                    }else{
                        System.out.println("Error: Request Failed");
                        System.out.println("Error Status = " + errorStatus);
                        System.out.println("Error Index = " + errorIndex);
                        System.out.println("Error Status Text = " + errorStatusText);
                    }
                }
                else
                {
                    System.out.println("Error: Response PDU is null");
                    setNetpingState(ipAddressIn, NetpingStateEnum.Disconneted);
                }
            }
            else
            {
                System.out.println("Error: Agent Timeout... ");
                setNetpingState(ipAddressIn, NetpingStateEnum.Disconneted);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    private void checkNetping(String ipAddressIn){
        checkNetping(ipAddressIn, true);
    }

    private void addCheckingTask(String ipAddressIn, boolean checkingIndicationIn){
        executor.submit(() -> {
            checkNetping(ipAddressIn, checkingIndicationIn);
            return null;
        });
    }

    private void checkAll(boolean checkingIndicationIn){
        for(String ip: ipMap.keySet()){
            addCheckingTask(ip, checkingIndicationIn);
        }
    }
    private void checkAll(){
        checkAll(true);
    }
}