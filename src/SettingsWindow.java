import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private JComboBox style;

    private AddChangeNetpingWindow addChangeNetpingWindow;

    SettingsWindow(SettingsLoader settingsLoaderIn, NetpingsChangeInterface netpingsChangeInterfaceIn){
        this.setTitle("Настройки");

        Vector<String> head = new Vector<>();
        head.add("ip-адрес");
        head.add("имя");
        DefaultTableModel model = new DefaultTableModel(head, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        netpingsTable.setModel(model);


        SettingsWindow context = this;
        addChangeNetpingWindow = new AddChangeNetpingWindow(new AddNetpingInterface() {
            @Override
            public void add(String ipAddress, String deviceNameIn) {
                if(!settingsLoaderIn.isNetpingExists(ipAddress)){
                    Vector<String> row = new Vector<>();
                    row.add(ipAddress);
                    row.add(deviceNameIn);
                    model.addRow(row);

                    settingsLoaderIn.setNetping(ipAddress, deviceNameIn);

                    validationStatus.setText("");
                }else{
                    System.out.print("11111111111111111111111111");
                    validationStatus.setText("netping с ip-адресом " + ipAddress + " уже есть");
                }
            }

            @Override
            public void change(String oldIpAddressIn, String newIpAddress, String deviceNameIn) {
                Vector<String> row = new Vector<>();
                row.add(newIpAddress);
                row.add(deviceNameIn);
                model.addRow(row);

                settingsLoaderIn.deleteNetping(oldIpAddressIn);
                settingsLoaderIn.setNetping(newIpAddress, deviceNameIn);
            }
        });

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
            addChangeNetpingWindow.setAddNetping();
            addChangeNetpingWindow.setVisible(true);
        });

        changeButton.addActionListener(e -> {
            if(netpingsTable.getSelectedRowCount() == 1){
                int row = netpingsTable.getSelectedRows()[0];
                addChangeNetpingWindow.setChangeNetping(model.getValueAt(row, 0).toString(), model.getValueAt(row, 1).toString());
                addChangeNetpingWindow.setVisible(true);

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
                settingsLoaderIn.deleteNetping(model.getValueAt(row, 0).toString());

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

                Map<String, String> map = settingsLoaderIn.getNetpingIpNameMap();

                for (int i = model.getRowCount() - 1; i >= 0; i--) {
                    model.removeRow(i);
                }

                for(String ip: map.keySet()){
                    Vector<String> row = new Vector<>();
                    row.add(ip);
                    row.add(map.get(ip));
                    model.addRow(row);
                }

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
                    SwingUtilities.updateComponentTreeUI(this);
                    this.pack();
                    SwingUtilities.updateComponentTreeUI(addChangeNetpingWindow);
                    addChangeNetpingWindow.pack();
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
}
