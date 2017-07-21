import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

public class SettingsDialog extends JDialog implements ApplyInterface {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton applyButton;
    private JButton defaultButton;
    private JButton changeButton;
    private JButton addButton;
    private JButton deleteButton;
    private JTextField community;
    private JTextField snmpTrapPort;
    private JTextField snmpPort;
    private JCheckBox trayIcon;
    private JTable netPingsTable;
    private JComboBox style;
    private JLabel validationStatus;

    private AddEditNetPingDialog addEditNetPingDialog;
    private MainWindow mainWindow;

    private NetPingTableModel model;

    public SettingsDialog(MainWindow mainWindowIn) {
        super(mainWindowIn, ModalityType.APPLICATION_MODAL);
        setTitle("Настройки");
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        mainWindow = mainWindowIn;

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

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
        head.add("виджет");

        model = new NetPingTableModel(head);
        netPingsTable.setModel(model);

        TableColumnModel tcm = netPingsTable.getColumnModel();
        tcm.removeColumn(tcm.getColumn(2));


        SettingsDialog settingsDialogContext = this;
        addEditNetPingDialog = new AddEditNetPingDialog(this);

        applyButton.addActionListener(e -> apply());

        defaultButton.addActionListener(e -> {
            community.setText("SWITCH");

            validationStatus.setText("");
        });

        addButton.addActionListener(e -> {
            NetPingWidget netPingWidget = new NetPingWidget(mainWindowIn, "192.168.0.1");
            addEditNetPingDialog.setAdding(netPingWidget);
            addEditNetPingDialog.setVisible(true);
            model.addNetPingWidget(netPingWidget);
        });

        changeButton.addActionListener(e -> {
            if(netPingsTable.getSelectedRowCount() == 1){
                int row = netPingsTable.getSelectedRows()[0];
                NetPingWidget netPingWidget = model.getNetPingWidget(row);
                addEditNetPingDialog.setEditing(netPingWidget);
                addEditNetPingDialog.setVisible(true);
                model.setNotAppliedNetPingWidgetSettings(row);
                netPingsTable.repaint();
                netPingsTable.revalidate();

                validationStatus.setText("");
            }else{
                validationStatus.setText("для изменения нужно выбрать 1 элемент");
            }
        });

        deleteButton.addActionListener(e -> {
            //сортируем для правильного удаления
            int[] primitiveRows = netPingsTable.getSelectedRows();

            ArrayList<Integer> rows = new ArrayList<>();
            for(int row: primitiveRows){
                rows.add(row);
            }

            Collections.sort(rows, Collections.reverseOrder());

            for (int row: rows){
                model.removeRow(row);
            }

            validationStatus.setText("");
        });


        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);

                validationStatus.setText("");

                trayIcon.setSelected(mainWindow.getTrayIconVisible());

                style.removeAllItems();
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    style.addItem(info.getName());
                }
                style.setSelectedItem(mainWindow.getStyle());

                Collection<NetPingWidget> netPingWidgets = mainWindow.getNetPingWidgets();

                model.clear();

                for(NetPingWidget netPingWidget: netPingWidgets){
                    model.addNetPingWidget(netPingWidget);
                }

                community.setText(mainWindow.getCommunity());

                settingsDialogContext.pack();
            }
        });
    }

    private void onOK() {
        apply();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void apply(){
        if(snmpPort.getText().matches("^[1-9]\\d*")){
            //snmpSettings.snmpPort = snmpPort.getText();
        }

        if(snmpTrapPort.getText().matches("^[1-9]\\d*")){
            //snmpSettings.snmpTrapsPort = snmpTrapPort.getText();
        }

        applied();
    }

    public void applied(){

    }

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        addEditNetPingDialog.updateStyle();
        this.pack();
    }

    public String getStyle(){
        return (String)style.getSelectedItem();
    }
    public boolean getTrayIconVisible(){
        return trayIcon.isSelected();
    }
    public Collection<NetPingWidget> getNetPingWidgets(){
        ArrayList<NetPingWidget> netPingWidgets = new ArrayList<>();
        for(int i=0; i<model.getRowCount(); i++){
            netPingWidgets.add((NetPingWidget)model.getValueAt(i, 2));
        }

        return netPingWidgets;
    }

    private class NetPingTableModel extends DefaultTableModel {
        NetPingTableModel(Vector<String> headIn){
            super(headIn, 0);
        }

        void addNetPingWidget(NetPingWidget netPingWidgetIn){
            Object[] row = {
                    netPingWidgetIn.getNotAppliedIpAddress(),
                    netPingWidgetIn.getNotAppliedDeviceName(),
                    netPingWidgetIn
            };

            this.addRow(row);
        }

        void setNotAppliedNetPingWidgetSettings(int rowIn){
            NetPingWidget netPingWidget = this.getNetPingWidget(rowIn);
            this.setValueAt(netPingWidget.getNotAppliedIpAddress(), rowIn, 0);
            this.setValueAt(netPingWidget.getNotAppliedDeviceName(), rowIn, 1);
        }

        NetPingWidget getNetPingWidget(int rowIn){
            return (NetPingWidget)this.getValueAt(rowIn, 2);
        }

        void clear(){
            for (int i = getRowCount() - 1; i >= 0; i--) {
                removeRow(i);
            }
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
