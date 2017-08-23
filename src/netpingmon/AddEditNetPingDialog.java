package netpingmon;

import javax.swing.*;
import java.awt.event.*;

public class AddEditNetPingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField snmpPort;
    private JTextField community;
    private JTextField deviceName;
    private JTextField ipAddress;
    private JCheckBox line1CheckBox;
    private JCheckBox line2CheckBox;
    private JCheckBox line3CheckBox;
    private JCheckBox line4CheckBox;
    private JButton line1SettingsButton;
    private JButton line2SettingsButton;
    private JButton line3SettingsButton;
    private JButton line4SettingsButton;
    private JLabel line1Name;
    private JLabel line2Name;
    private JLabel line3Name;
    private JLabel line4Name;
    private JButton defaultButton;
    private JLabel validation;
    private JButton connectedMessageButton;
    private JButton disconnectedMessageButton;
    private JComboBox gridType;

    private boolean editing; //режим редактирования - true, режим добавления - false
    private NetPingWidget editingNetPingWidget;

    private EditIoLineDialog editIoLineDialog;

    private int openResult;

    private EditDisplayMessageDialog editDisplayMessageDialog = new EditDisplayMessageDialog(this);

    private MainWindow mainWindow;

    private GuiSaver guiSaver = new GuiSaver(this, "AddEditNetPingDialog");

    AddEditNetPingDialog(MainWindow mainWindowIn) {
        super(mainWindowIn, ModalityType.APPLICATION_MODAL);

        mainWindow = mainWindowIn;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        defaultButton.addActionListener(e -> onDefault());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        editIoLineDialog = new EditIoLineDialog(this);

        connectedMessageButton.addActionListener(e -> editDisplayMessageDialog.open(editingNetPingWidget.getConnectedMessage()));
        disconnectedMessageButton.addActionListener(e -> editDisplayMessageDialog.open(editingNetPingWidget.getDisconnectedMessage()));

        AddEditNetPingDialog addEditNetPingDialog = this;
        line1SettingsButton.addActionListener(e -> {
            if(editIoLineDialog.open(editingNetPingWidget.getLine("1"), "1") == JOptionPane.OK_OPTION){
                line1Name.setText(editingNetPingWidget.getLine("1").getNotAppliedLineName());
                addEditNetPingDialog.pack();
            }
        });
        line2SettingsButton.addActionListener(e -> {
            if(editIoLineDialog.open(editingNetPingWidget.getLine("2"), "2") == JOptionPane.OK_OPTION){
                line2Name.setText(editingNetPingWidget.getLine("2").getNotAppliedLineName());
                addEditNetPingDialog.pack();
            }
        });
        line3SettingsButton.addActionListener(e -> {
            if(editIoLineDialog.open(editingNetPingWidget.getLine("3"), "3") == JOptionPane.OK_OPTION){
                line3Name.setText(editingNetPingWidget.getLine("3").getNotAppliedLineName());
                addEditNetPingDialog.pack();
            }
        });
        line4SettingsButton.addActionListener(e -> {
            if(editIoLineDialog.open(editingNetPingWidget.getLine("4"), "4") == JOptionPane.OK_OPTION){
                line4Name.setText(editingNetPingWidget.getLine("4").getNotAppliedLineName());
                addEditNetPingDialog.pack();
            }
        });

        this.pack();

        guiSaver.saveWindowMaximized(true);
        guiSaver.load();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                guiSaver.save();
            }
        });
    }

    //<on>==============================================================================================================
    private void onOK() {
        boolean valid;
        boolean ipAddressValid = ipAddress.getText().matches("([0-9]{1,3}\\.){3}[0-9]{1,3}");
        boolean snmpPortValid = snmpPort.getText().matches("^[1-9]\\d*");
        boolean ipAddressExist = mainWindow.isNetPingExist(ipAddress.getText());

        valid = ipAddressValid;
        valid = valid && snmpPortValid;

        if(!editing){
            valid = valid && !ipAddressExist;
        }

        if(valid){
            editingNetPingWidget.setIpAddress(ipAddress.getText());
            editingNetPingWidget.setSnmpPort(snmpPort.getText());
            editingNetPingWidget.setSnmpCommunity(community.getText());
            editingNetPingWidget.setDeviceName(deviceName.getText());

            editingNetPingWidget.setLineActive("1", line1CheckBox.isSelected());
            editingNetPingWidget.setLineActive("2", line2CheckBox.isSelected());
            editingNetPingWidget.setLineActive("3", line3CheckBox.isSelected());
            editingNetPingWidget.setLineActive("4", line4CheckBox.isSelected());

            editingNetPingWidget.setGridType((String)gridType.getSelectedItem());

            editingNetPingWidget.applySettings();

            openResult = JOptionPane.OK_OPTION;
            dispose();
        }else{
            if(!ipAddressValid){
                validation.setText("неправильный ip-адрес");
            }else if(!snmpPortValid){
                validation.setText("неправильный SNMP-порт");
            }else if(ipAddressExist && !editing){
                validation.setText("такой ip-адрес уже есть");
            }

            this.pack();
        }
    }
    private void onDefault() {
        ipAddress.setText("192.168.0.1");
        deviceName.setText("");
        snmpPort.setText("161");
        community.setText("SWITCH");

        line1CheckBox.setSelected(true);
        line2CheckBox.setSelected(true);
        line3CheckBox.setSelected(true);
        line4CheckBox.setSelected(true);
    }
    private void onCancel() {
        openResult = JOptionPane.CANCEL_OPTION;
        dispose();
    }
    //</on>=============================================================================================================

    void setAdding(NetPingWidget netPingWidgetIn){
        ipAddress.requestFocus();

        editing = false;
        editingNetPingWidget = netPingWidgetIn;
        ipAddress.setText(netPingWidgetIn.getIpAddress());
        deviceName.setText("");
        this.setTitle("Добавление NetPing");

        onDefault();
    }
    void setAddingCopy(NetPingWidget newNetPingWidgetIn, NetPingWidget oldNetPingWidgetIn){
        ipAddress.requestFocus();

        editing = false;
        editingNetPingWidget = newNetPingWidgetIn;
        ipAddress.setText(newNetPingWidgetIn.getNotAppliedIpAddress());
        deviceName.setText(newNetPingWidgetIn.getNotAppliedDeviceName());
        community.setText(newNetPingWidgetIn.getNotAppliedSnmpCommunity());
        snmpPort.setText(newNetPingWidgetIn.getNotAppliedSnmpPort());

        line1Name.setText(newNetPingWidgetIn.getLine("1").getLineName());
        line2Name.setText(newNetPingWidgetIn.getLine("2").getLineName());
        line3Name.setText(newNetPingWidgetIn.getLine("3").getLineName());
        line4Name.setText(newNetPingWidgetIn.getLine("4").getLineName());

        line1CheckBox.setSelected(oldNetPingWidgetIn.getLine("1").isActive());
        line2CheckBox.setSelected(oldNetPingWidgetIn.getLine("2").isActive());
        line3CheckBox.setSelected(oldNetPingWidgetIn.getLine("3").isActive());
        line4CheckBox.setSelected(oldNetPingWidgetIn.getLine("4").isActive());

        this.setTitle("Копирование NetPing");
    }
    void setEditing(NetPingWidget netPingWidgetIn){
        ipAddress.requestFocus();

        editing = true;
        editingNetPingWidget = netPingWidgetIn;
        ipAddress.setText(editingNetPingWidget.getNotAppliedIpAddress());
        deviceName.setText(editingNetPingWidget.getNotAppliedDeviceName());
        community.setText(editingNetPingWidget.getNotAppliedSnmpCommunity());
        snmpPort.setText(editingNetPingWidget.getNotAppliedSnmpPort());

        gridType.setSelectedItem(editingNetPingWidget.getGridType());

        line1Name.setText(editingNetPingWidget.getLine("1").getLineName());
        line2Name.setText(editingNetPingWidget.getLine("2").getLineName());
        line3Name.setText(editingNetPingWidget.getLine("3").getLineName());
        line4Name.setText(editingNetPingWidget.getLine("4").getLineName());

        line1CheckBox.setSelected(editingNetPingWidget.getLine("1").isActive());
        line2CheckBox.setSelected(editingNetPingWidget.getLine("2").isActive());
        line3CheckBox.setSelected(editingNetPingWidget.getLine("3").isActive());
        line4CheckBox.setSelected(editingNetPingWidget.getLine("4").isActive());

        this.setTitle("Изменение NetPing");
    }

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        editIoLineDialog.updateStyle();
        editDisplayMessageDialog.updateStyle();
        this.pack();
    }

    int open(){
        ipAddress.requestFocus();
        ipAddress.selectAll();
        this.setVisible(true);
        return openResult;
    }
}
