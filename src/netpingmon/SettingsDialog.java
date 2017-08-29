package netpingmon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SettingsDialog extends JDialog implements ApplyInterface {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton applyButton;
    private JButton defaultButton;
    private JButton changeButton;
    private JButton addButton;
    private JButton deleteButton;
    private JTextField snmpTrapPort;
    private JTextField snmpPort;
    private JCheckBox trayIcon;
    private JTable netPingsTable;
    private JComboBox style;
    private JLabel validationStatus;
    private JTextField timeout;
    private JTextField checkingDelay;
    private JTextField retries;
    private JTextField community;
    private JButton copyButton;
    private JTextField gridColumns;
    private JTextField gridRows;

    //<table columns>===================================================================================================
    private static final int ipAddressColumn = 0;
    private static final int nameColumn = 1;
    private static final int activeColumn = 2;
    private static final int netPingWidgetColumn = 3;
    //</table columns>==================================================================================================

    private AddEditNetPingDialog addEditNetPingDialog;
    private MainWindow mainWindow;

    private NetPingTableModel model;

    private GuiSaver guiSaver = new GuiSaver(this, "SettingsDialog");

    SettingsDialog(MainWindow mainWindowIn) {
        super(mainWindowIn, ModalityType.APPLICATION_MODAL);
        setTitle("Настройки");
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        mainWindow = mainWindowIn;

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        applyButton.addActionListener(e -> applyAll());
        defaultButton.addActionListener(e -> onDefault());

        addButton.addActionListener(e -> onAdd());
        changeButton.addActionListener(e -> onChange());
        deleteButton.addActionListener(e -> onDelete());
        copyButton.addActionListener(e -> onCopy());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        Vector<String> head = new Vector<>();
        head.add("ip-адрес");
        head.add("имя");
        head.add("активен");
        head.add("виджет");

        model = new NetPingTableModel(head);
        netPingsTable.setModel(model);

        TableRowSorter<NetPingTableModel> sorter = new TableRowSorter<>(model);
        netPingsTable.setRowSorter(sorter);
        netPingsTable.updateUI();

        TableColumnModel tcm = netPingsTable.getColumnModel();
        tcm.removeColumn(tcm.getColumn(netPingWidgetColumn));

        addEditNetPingDialog = new AddEditNetPingDialog(mainWindow);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                onShown();
            }
        });

        this.pack();

        guiSaver.saveWindowMaximized(true);
        guiSaver.saveTableSortKeys(sorter, "sorter");
        guiSaver.load();
    }

    //<on>==============================================================================================================
    private void onOK() {
        if(applyAll()){
            guiSaver.save();
            dispose();
        }
    }
    private void onCancel() {
        //отменяем настройки для каждого netPing
        for(int i=0; i<model.getRowCount(); i++){
            model.getNetPingWidget(i).discardSettings();
        }

        guiSaver.save();
        dispose();
    }
    private void onDefault(){
        trayIcon.setSelected(false);
        style.setSelectedItem("Metal");

        timeout.setText("3");
        checkingDelay.setText("60");
        retries.setText("4");
        snmpPort.setText("64123");
        snmpTrapPort.setText("162");

        validationStatus.setText("");
    }
    private void onAdd(){
        NetPingWidget netPingWidget = new NetPingWidget(mainWindow, "192.168.0.1");
        addEditNetPingDialog.setAdding(netPingWidget);
        if(addEditNetPingDialog.open() == JOptionPane.OK_OPTION){
            model.addNetPingWidget(netPingWidget);
            netPingWidget.applySettings();
            netPingWidget.snmpInitialized();
            addedNetPing(netPingWidget);
        }
    }
    private void onChange(){
        if(netPingsTable.getSelectedRowCount() == 1){
            int row = netPingsTable.getSelectedRows()[0];
            NetPingWidget netPingWidget = model.getNetPingWidget(row);
            addEditNetPingDialog.setEditing(netPingWidget);
            if(addEditNetPingDialog.open() == JOptionPane.OK_OPTION){
                model.setNotAppliedNetPingWidgetSettings(row);
                netPingsTable.repaint();
                netPingsTable.revalidate();

                netPingWidget.applySettings();
                netPingWidget.snmpInitialized();
                addedNetPing(netPingWidget);

                changedNetPing(netPingWidget);
            }

            validationStatus.setText("");
        }else{
            validationStatus.setText("для изменения нужно выбрать 1 элемент");
        }
    }
    private void onDelete(){
        int dialogResult = JOptionPane.showConfirmDialog(null, "Удалить выбранные устройства?", "Удаление", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
            int[] rows = netPingsTable.getSelectedRows();

            List<NetPingWidget> toRemove = new ArrayList<>();
            for(int i=0;i<rows.length;i++){
                int row = netPingsTable.convertRowIndexToModel(rows[i]-i);

                toRemove.add(model.getNetPingWidget(row));
                model.removeRow(row);
            }

            removedNetPings(toRemove);

            validationStatus.setText("");
        }
    }
    private void onShown(){
        trayIcon.setSelected(mainWindow.getTrayIconVisible());

        style.removeAllItems();
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            style.addItem(info.getName());
        }
        style.setSelectedItem(mainWindow.getStyle());

        model.clear();
        Collection<NetPingWidget> netPingWidgets = mainWindow.getNetPingWidgets();
        for(NetPingWidget netPingWidget: netPingWidgets){
            model.addNetPingWidget(netPingWidget);
        }

        snmpPort.setText(Integer.toString(mainWindow.getSnmpPort()));
        snmpTrapPort.setText(Integer.toString(mainWindow.getReceiveTrapsPort()));
        community.setText(mainWindow.getSnmpCommunity());
        timeout.setText(Integer.toString(mainWindow.getTimeOut()));
        checkingDelay.setText(Integer.toString(mainWindow.getCheckingDelay()));
        retries.setText(Integer.toString(mainWindow.getRetries()));
        gridColumns.setText(Integer.toString(mainWindow.getGridColumns()));
        gridRows.setText(Integer.toString(mainWindow.getGridRows()));

        validationStatus.setText("");
    }
    private void onCopy(){
        if(netPingsTable.getSelectedRowCount() == 1){
            int row = netPingsTable.getSelectedRows()[0];
            NetPingWidget netPingWidget = model.getNetPingWidget(row);
            NetPingWidget netPingWidgetCopied = new NetPingWidget(mainWindow, netPingWidget.getIpAddress());
            netPingWidget.copyTo(netPingWidgetCopied);
            addEditNetPingDialog.setAddingCopy(netPingWidgetCopied, netPingWidget);
            if(addEditNetPingDialog.open() == JOptionPane.OK_OPTION){
                model.addNetPingWidget(netPingWidgetCopied);
                netPingWidgetCopied.applySettings();
                netPingWidgetCopied.snmpInitialized();
                addedNetPing(netPingWidgetCopied);
            }

            validationStatus.setText("");
        }else{
            validationStatus.setText("для копирования нужно выбрать 1 элемент");
        }
    }
    //</on>=============================================================================================================

    private boolean applyAll(){
        String intRegex = "^[1-9]\\d*";

        boolean gridColumnsValid = gridColumns.getText().matches(intRegex);
        boolean gridRowsValid = gridRows.getText().matches(intRegex);
        boolean snmpPortValid = snmpPort.getText().matches(intRegex);
        boolean snmpTrapPortValid = snmpTrapPort.getText().matches(intRegex);
        boolean timeoutValid = timeout.getText().matches(intRegex);
        boolean checkingDelayValid = checkingDelay.getText().matches(intRegex);
        boolean retriesValid = retries.getText().matches(intRegex);
        boolean valid = gridColumnsValid && gridRowsValid && retriesValid && timeoutValid && checkingDelayValid && snmpPortValid && snmpTrapPortValid;

        if(valid){
            validationStatus.setText("");

            //применяем настройки для каждого netPing
            for(int i=0; i<model.getRowCount(); i++){
                NetPingWidget netPingWidget = (model.getNetPingWidget(i));
                netPingWidget.setActive(model.isNetPingActive(i));
                netPingWidget.applySettings();
            }

            appliedAll();

            return true;
        }else{
            if(!gridColumnsValid){
                validationStatus.setText("неправильное количество столбцов плитки");
            }else if(!gridRowsValid){
                validationStatus.setText("неправильное количество строк плитки");
            }else if(!snmpPortValid){
                validationStatus.setText("неправильный SNMP-порт");
            }else if(!snmpTrapPortValid){
                validationStatus.setText("неправильный порт приема SNMP-ловушек");
            }else if(!checkingDelayValid){
                validationStatus.setText("неправильный интервал между проверками");
            }else if(!timeoutValid){
                validationStatus.setText("неправильное время ожидания");
            }else if(!retriesValid){
                validationStatus.setText("неправильное кол-во повторных запросов");
            }
        }

        return false;
    }

    public void appliedAll(){}

    public void addedNetPing(NetPingWidget netPingWidgetIn){}

    public void removedNetPings(List<NetPingWidget> netPingWidgetsIn) {}

    public void changedNetPing(NetPingWidget netPingWidgetIn) {}

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        addEditNetPingDialog.updateStyle();
    }

    //<get>=============================================================================================================
    String getSnmpTrapPort(){
        return snmpTrapPort.getText();
    }
    int getSnmpPort(){
        return Integer.parseInt(snmpPort.getText());
    }
    String getSnmpCommunity(){
        return community.getText();
    }
    int getTimeOut(){
        return Integer.parseInt(timeout.getText());
    }
    int getCheckingDelay(){
        return Integer.parseInt(checkingDelay.getText());
    }
    int getRetries(){
        return Integer.parseInt(retries.getText());
    }
    String getStyle(){
        return (String)style.getSelectedItem();
    }
    boolean getTrayIconVisible(){
        return trayIcon.isSelected();
    }
    int getGridColumns(){
        return Integer.valueOf(gridColumns.getText());
    }
    int getGridRows(){
        return Integer.valueOf(gridRows.getText());
    }
    Collection<NetPingWidget> getNetPingWidgets(){
        ArrayList<NetPingWidget> netPingWidgets = new ArrayList<>();
        for(int i=0; i<model.getRowCount(); i++){
            netPingWidgets.add(model.getNetPingWidget(i));
        }

        return netPingWidgets;
    }
    //</get>============================================================================================================

    private class NetPingTableModel extends DefaultTableModel {
        NetPingTableModel(Vector<String> headIn){
            super(headIn, 0);
        }

        void addNetPingWidget(NetPingWidget netPingWidgetIn){
            Object[] row = {
                    netPingWidgetIn.getNotAppliedIpAddress(),
                    netPingWidgetIn.getNotAppliedDeviceName(),
                    netPingWidgetIn.isActive(),
                    netPingWidgetIn
            };

            this.addRow(row);
        }

        void setNotAppliedNetPingWidgetSettings(int rowIn){
            NetPingWidget netPingWidget = this.getNetPingWidget(rowIn);
            this.setValueAt(new Boolean(netPingWidget.isActive()), rowIn, activeColumn);
            this.setValueAt(netPingWidget.getNotAppliedIpAddress(), rowIn, ipAddressColumn);
            this.setValueAt(netPingWidget.getNotAppliedDeviceName(), rowIn, nameColumn);
        }

        NetPingWidget getNetPingWidget(int rowIn){
            return (NetPingWidget)this.getValueAt(rowIn, netPingWidgetColumn);
        }

        void clear(){
            for (int i = getRowCount() - 1; i >= 0; i--) {
                removeRow(i);
            }
        }

        public boolean isCellEditable(int row, int column) {
            if(column == activeColumn){
                return true;
            }
            return false;
        }

        public Class getColumnClass(int column) {
            switch (column) {
                case ipAddressColumn:
                    return String.class;
                case nameColumn:
                    return String.class;
                case activeColumn:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        public boolean isNetPingActive(int rowIn){
            return (boolean)getValueAt(rowIn, activeColumn);
        }
    }
}
