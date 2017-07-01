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
        AddChangeNetpingWindow addChangeNetpingWindow = new AddChangeNetpingWindow(new AddNetpingInterface() {
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
        SnmpSettings snmpSettings = new SnmpSettings();
        snmpSettings.snmpPort = snmpPort.getText();
        snmpSettings.snmpTrapsPort = snmpTrapPort.getText();
        snmpSettings.community = community.getText();

        settingsLoaderIn.setSnmpSettings(snmpSettings);

        settingsLoaderIn.saveConfig();

        netpingsChangeInterfaceIn.changed();
    }
}
