import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

public class SettingsWindow extends JFrame {
    private JButton cancelButton;
    private JButton okButton;
    private JButton applyButton;
    private JButton addButton;
    private JButton changeButton;
    private JButton deleteButton;
    private JButton defaultButton;
    private JTextField snmpPort;
    private JTextField snmpTrapPort;
    private JTextField community;
    private JPanel rootPanel;
    private JTable netpingsTable;
    private JLabel validationStatus;
    private JCheckBox trayIcon;
    private JComboBox<String> style;

    private AddEditNetPingDialog addEditNetPingDialog;

    private NetPingWidgetListener netPingWidgetListener;
    private MainWindow mainWindow;

    SettingsWindow(NetPingWidgetListener netPingWidgetListenerIn, MainWindow mainWindowIn){
        this.setTitle("Настройки");

        netPingWidgetListener = netPingWidgetListenerIn;
        mainWindow = mainWindowIn;

        Vector<String> head = new Vector<>();
        head.add("ip-адрес");
        head.add("имя");
        head.add("виджет");

        NetPingTableModel model = new NetPingTableModel(head){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        netpingsTable.setModel(model);

        TableColumnModel tcm = netpingsTable.getColumnModel();
        tcm.removeColumn(tcm.getColumn(2));


        SettingsWindow context = this;
        addEditNetPingDialog = new AddEditNetPingDialog(this);

//        new AddNetpingInterface() {
//            @Override
//            public void add(String ipAddress, String deviceNameIn) {
//                if(!settingsLoaderIn.isNetpingExists(ipAddress)){
//                    Vector<String> row = new Vector<>();
//                    row.add(ipAddress);
//                    row.add(deviceNameIn);
//                    model.addRow(row);
//
//                    //settingsLoaderIn.setNetping(ipAddress, deviceNameIn);
//
//                    validationStatus.setText("");
//                }else{
//                    validationStatus.setText("netping с ip-адресом " + ipAddress + " уже есть");
//                }
//            }
//
//            @Override
//            public void change(String oldIpAddressIn, String newIpAddress, String deviceNameIn) {
//                Vector<String> row = new Vector<>();
//                row.add(newIpAddress);
//                row.add(deviceNameIn);
//                model.addRow(row);
//
//                settingsLoaderIn.removeNetping(oldIpAddressIn);
//                //settingsLoaderIn.setNetping(newIpAddress, deviceNameIn);
//            }
//        }

        applyButton.addActionListener(e -> apply(settingsLoaderIn, netpingsChangeInterfaceIn));

        okButton.addActionListener(e -> {
            apply(settingsLoaderIn, netpingsChangeInterfaceIn);
            dispose();
        });

        cancelButton.addActionListener(e -> {
            settingsLoaderIn.loadConfig();
            dispose();
        });

        defaultButton.addActionListener(e -> {
            settingsLoaderIn.loadDefaultConfig();

            SnmpSettings snmpSettings = settingsLoaderIn.getSnmpSettings();
            community.setText(snmpSettings.community);
            snmpTrapPort.setText(snmpSettings.snmpTrapsPort);
            snmpPort.setText(snmpSettings.snmpPort);

            validationStatus.setText("");
        });

        addButton.addActionListener(e -> {
            addEditNetPingDialog.setAdding(mainWindow.getSettingsLoader().newNetPing(mainWindow, "192.168.0.1"));
            addEditNetPingDialog.setVisible(true);
        });

        changeButton.addActionListener(e -> {
            if(netpingsTable.getSelectedRowCount() == 1){
                int row = netpingsTable.getSelectedRows()[0];
                addEditNetPingDialog.setEditing(model.getNetPingWidget(row));
                addEditNetPingDialog.setVisible(true);

                validationStatus.setText("");
            }else{
                validationStatus.setText("для изменения нужно выбрать 1 элемент");
            }
        });

        deleteButton.addActionListener(e -> {
            //сортируем для правильного удаления
            int[] primitiveRows = netpingsTable.getSelectedRows();

            ArrayList<Integer> rows = new ArrayList<>();
            for(int row: primitiveRows){
                rows.add(row);
            }

            Collections.sort(rows, Collections.reverseOrder());

            for (int row: rows){
                settingsLoaderIn.removeNetping(model.getValueAt(row, 0).toString());

                model.removeRow(row);
            }

            validationStatus.setText("");
        });


        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);

                validationStatus.setText("");

                trayIcon.setSelected(settingsLoaderIn.isTrayIcon());

                style.removeAllItems();
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    style.addItem(info.getName());
                }
                style.setSelectedItem(settingsLoaderIn.getStyle());

//                Map<String, String> map = settingsLoaderIn.getNetpingIpNameMap();
//
//                for (int i = model.getRowCount() - 1; i >= 0; i--) {
//                    model.removeRow(i);
//                }
//
//                for(String ip: map.keySet()){
//                    Vector<String> row = new Vector<>();
//                    row.add(ip);
//                    row.add(map.get(ip));
//                    model.addRow(row);
//                }

                SnmpSettings snmpSettings = settingsLoaderIn.getSnmpSettings();
                community.setText(snmpSettings.community);
                snmpTrapPort.setText(snmpSettings.snmpTrapsPort);
                snmpPort.setText(snmpSettings.snmpPort);

                context.pack();
            }
        });

        this.getContentPane().add(rootPanel);
    }

    private void apply(SettingsLoader settingsLoaderIn, NetpingsChangeInterface netpingsChangeInterfaceIn){
        settingsLoaderIn.setTrayIconVisible(trayIcon.isSelected());

        SnmpSettings snmpSettings = new SnmpSettings();
        snmpSettings.community = community.getText();

        if(snmpPort.getText().matches("^[1-9]\\d*")){
            snmpSettings.snmpPort = snmpPort.getText();
        }

        String changedParmasRestart = "";

        if(snmpTrapPort.getText().matches("^[1-9]\\d*")){
            if(settingsLoaderIn.getSnmpSettings().snmpTrapsPort.compareTo(snmpTrapPort.getText()) != 0){
                changedParmasRestart += "порт прима SNMP-ловушек был изменен,\n";
            }

            snmpSettings.snmpTrapsPort = snmpTrapPort.getText();
        }

        String newStyle = style.getSelectedItem().toString();
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (newStyle.equals(info.getName())) {
                    settingsLoaderIn.setStyle(newStyle);
                    UIManager.setLookAndFeel(info.getClassName());
                    mainWindow.updateStyle();
                    this.pack();
                    break;
                }
            }
        } catch (Exception ex) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        if(!changedParmasRestart.isEmpty()){
            JOptionPane.showMessageDialog(null, changedParmasRestart + "для вступления изменений в силу, нужно перезапустить программу", "Изменение параметров", JOptionPane.INFORMATION_MESSAGE);
        }

        settingsLoaderIn.setSnmpSettings(snmpSettings);

        settingsLoaderIn.saveConfig();

        netpingsChangeInterfaceIn.changed();
    }

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        addEditNetPingDialog.updateStyle();
        this.pack();
    }

    public MainWindow getMainWindow(){
        return mainWindow;
    }



    private class NetPingTableModel extends DefaultTableModel{
        NetPingTableModel(Vector<String> headIn){
            super(headIn, 3);
        }

        void addNetPingWidget(NetPingWidget netPingWidgetIn){
            Object[] row = {
                    netPingWidgetIn.getIpAddress(),
                    netPingWidgetIn.getDeviceName(),
                    netPingWidgetIn
            };

            this.addRow(row);
        }

        void setNetPingWidget(NetPingWidget netPingWidgetIn, int rowIn){
            this.setValueAt(netPingWidgetIn, rowIn, 2);
        }

        NetPingWidget getNetPingWidget(int rowIn){
            return (NetPingWidget)this.getValueAt(rowIn, 2);
        }
    }
}
