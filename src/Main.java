import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.json.JSONObject;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
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

public class Main implements CommandResponder {
    static OID oid;
    static OID getIO1oid;
    static JSONObject config;

    static Snmp snmp;

    static Map<String, NetpingWidget> ipMap = new HashMap<>();
    static Map<String, Timer> timersMap = new HashMap<>();

    static JFrame frame;
    static JPanel gridPanel;
    static TrayIcon trayIcon;

    public Main() {
        createGUI();

        JSONObject json = loadJSON("config.json");
        JSONObject netpings = json.getJSONObject("netpings");

        for(String ip: netpings.keySet()){
            addNetping(netpings.getString(ip), ip);
        }

        frame.pack();
    }

    private static String loadDataFromfile(String fileNameIn){
        try{
            File file = new File(fileNameIn);
            file.createNewFile();

            byte[] encoded = Files.readAllBytes(Paths.get(fileNameIn));
            return new String(encoded, Charset.defaultCharset());
        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    private static JSONObject loadJSON(String fileNameIn){
        String data = loadDataFromfile(fileNameIn);

        JSONObject json;
        if(data.isEmpty()){
            json = new JSONObject();
        }else{
            json = new JSONObject(data);
        }

        boolean saveFile = false;

        if(!json.has("ip-address")){
            json.put("ip-address", "0.0.0.0");
            saveFile=true;
        }

        if(!json.has("netpingSNMPport")){
            json.put("netpingSNMPport", "161");
            saveFile=true;
        }

        if(!json.has("trapReceivePort")){
            json.put("trapReceivePort", "162");
            saveFile=true;
        }

        if(!json.has("oid")){
            json.put("oid", "1.3.6.1.4.1.25728.8900.2.2.0");
            saveFile=true;
        }

        if(!json.has("getIO1")){
            json.put("getIO1", "1.3.6.1.4.1.25728.8900.1.1.2.1");
            saveFile=true;
        }

        if(!json.has("community")){
            json.put("community", "SWITCH");
            saveFile=true;
        }

        if(!json.has("netpings")){
            json.put("netpings", new JSONObject());
            saveFile=true;
        }

        if(saveFile){
            saveDataToFile(fileNameIn, json.toString(5));
        }

        return json;
    }

    private static void saveDataToFile(String fileNameIn, String dataIn){
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(fileNameIn);
            bw = new BufferedWriter(fw);
            bw.write(dataIn);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        setTrayIcon();

        snmp = null;

        config = loadJSON("config.json");

        String address = config.getString("ip-address") + "/" + config.getString("trapReceivePort");

        oid = new OID(config.getString("oid"));
        getIO1oid = new OID(config.getString("getIO1"));

        Main snmp4jTrapReceiver = new Main();
        try {
            snmp4jTrapReceiver.listen(new UdpAddress(address));
        } catch (IOException e) {
            System.err.println("Error in Listening for Trap");
            System.err.println("Exception Message = " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error in Listening for Trap", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method will listen for traps and response pdu's from SNMP agent.
     */
    public synchronized void listen(TransportIpAddress address) throws IOException {
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
        target.setCommunity(new OctetString(config.getString("community")));

        snmp = new Snmp(mtDispatcher, transport);
        snmp.addCommandResponder(this);

        transport.listen();
        System.out.println("Listening traps on " + address);
        String message = "trap receiver executed!\nListening on " + address;
        trayIcon.displayMessage("trap receiver", message, TrayIcon.MessageType.INFO);

        for(String ip: ipMap.keySet()){
            checkNetpingOpened(ip);
        }

        try {
            this.wait();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * This method will be called whenever a pdu is received on the given port specified in the listen() method
     */
    public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
        System.out.println("Received PDU...");
        PDU pdu = cmdRespEvent.getPDU();
        if (pdu != null) {

            if(pdu.getVariable(oid) != null){
                int opened = pdu.getVariable(oid).toInt();

                String ipPort = cmdRespEvent.getPeerAddress().toString();
                String ip = ipPort.split("/")[0];

                if(opened == 0){
                    setNetpingOpened(ip, false);
                }else{
                    setNetpingOpened(ip, true);
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


    private static void setTrayIcon() {
        PopupMenu trayMenu = new PopupMenu();
        MenuItem item = new MenuItem("Exit");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        trayMenu.add(item);

        Image icon = Toolkit.getDefaultToolkit().getImage("icon.png");
        trayIcon = new TrayIcon(icon, "trap receiver", trayMenu);
        trayIcon.setImageAutoSize(true);

        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


    private static void addNetpingToConfig(String nameIn, String ipAddressIn){
        JSONObject json = loadJSON("config.json");
        JSONObject netpings = json.getJSONObject("netpings");

        netpings.put(ipAddressIn, nameIn);

        saveDataToFile("config.json", json.toString(5));
    }

    private static void addNetping(String nameIn, String ipAddressIn){
        NetpingWidget netping = new NetpingWidget(nameIn, ipAddressIn);
        ipMap.put(ipAddressIn, netping);

        System.out.println(ipAddressIn + ": " + nameIn);

        addNetpingToConfig(nameIn, ipAddressIn);

        gridPanel.add(netping);
        gridPanel.revalidate();
        gridPanel.repaint();

        if(snmp != null){
            checkNetpingOpened(ipAddressIn);
        }
    }

    private static void setNetpingOpened(String ipAddressIn, boolean openedIn){
        ipMap.get(ipAddressIn).setOpened(openedIn);
    }

    private static void setNetpingChecking(String ipAddressIn){
        ipMap.get(ipAddressIn).setChecking();
    }

    private static void setNetpingDisconnected(String netpingNameIn){
        ipMap.get(netpingNameIn).setDisconnected();
    }


    private static boolean checkNetpingOpened(String ipAddressIn) {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        pdu.setRequestID(new Integer32(1));

        // Create Target Address object
        CommunityTarget comtarget = new CommunityTarget();
        comtarget.setCommunity(new OctetString(config.getString("community")));
        comtarget.setVersion(SnmpConstants.version1);
        comtarget.setAddress(new UdpAddress(ipAddressIn + "/" + config.getString("netpingSNMPport")));
        comtarget.setRetries(2);
        comtarget.setTimeout(1000);

        System.out.println(config.getString("community"));
        System.out.println(ipAddressIn);

        System.out.println("Sending Request to Agent...");
        setNetpingChecking(ipAddressIn);

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
                                setNetpingOpened(ipAddressIn, false);
                            }else{
                                setNetpingOpened(ipAddressIn, true);
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
                    setNetpingDisconnected(ipAddressIn);
                }
            }
            else
            {
                System.out.println("Error: Agent Timeout... ");
                setNetpingDisconnected(ipAddressIn);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }

        return true;
    }


    private static void createGUI()
    {
        frame = new JFrame("Netping мониторинг");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

        JPanel toolPanel = new JPanel();
        JButton addButton = new JButton("добавить");
        JButton deleteButton = new JButton("удалить");
        toolPanel.add(addButton);
        toolPanel.add(deleteButton);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddNetpingWindow(new AddNetpingInterface() {
                    @Override
                    public void add(String deviceNameIn, String ipAddressIn) {
                        addNetping(deviceNameIn, ipAddressIn);
                    }
                });
            }
        });

        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(5, 5));

        //addNetping("ХАДТ эстакада", "192.168.1.214");
        //addNetping("пристанционный узел", "192.168.1.207");

        rootPanel.add(toolPanel);
        rootPanel.add(gridPanel);

        frame.getContentPane().add(rootPanel);
        frame.pack();
        frame.setVisible(true);
    }

}