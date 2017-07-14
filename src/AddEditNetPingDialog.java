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

    private boolean editing; //режим редактирования - true, режим добавления - false
    private NetPingWidget currentNetPingWidget;

    private EditIOLineDialog editIOLineDialog;

    private SettingsWindow settingsWindow;

    public AddEditNetPingDialog(SettingsWindow settingsWindowIn) {
        super(settingsWindowIn, ModalityType.APPLICATION_MODAL);

        settingsWindow = settingsWindowIn;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onCancel());

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

        editIOLineDialog = new EditIOLineDialog(this);

        line1SettingsButton.addActionListener(e -> {
            editIOLineDialog.setIOLineEditing(currentNetPingWidget.getLine("1"), "1");
            editIOLineDialog.setVisible(true);
        });
        line2SettingsButton.addActionListener(e -> {
            editIOLineDialog.setIOLineEditing(currentNetPingWidget.getLine("2"), "2");
            editIOLineDialog.setVisible(true);
        });
        line3SettingsButton.addActionListener(e -> {
            editIOLineDialog.setIOLineEditing(currentNetPingWidget.getLine("3"), "3");
            editIOLineDialog.setVisible(true);
        });
        line4SettingsButton.addActionListener(e -> {
            editIOLineDialog.setIOLineEditing(currentNetPingWidget.getLine("4"), "4");
            editIOLineDialog.setVisible(true);
        });

        this.pack();
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void setAdding(NetPingWidget netPingWidgetIn){
        editing = false;
        currentNetPingWidget = netPingWidgetIn;
        this.setTitle("Добавление NetPing");
    }
    public void setEditing(NetPingWidget netPingWidgetIn){
        editing = true;
        currentNetPingWidget = netPingWidgetIn;
        this.setTitle("Изменение NetPing");
    }

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        editIOLineDialog.updateStyle();
        this.pack();
    }

    public boolean getEditing(){
        return editing;
    }

    void setLineName(String lineNumberIn, String nameIn){
        switch (lineNumberIn){
            case "1":
                line1Name.setText(nameIn);
                break;
            case "2":
                line2Name.setText(nameIn);
                break;
            case "3":
                line3Name.setText(nameIn);
                break;
            case "4":
                line4Name.setText(nameIn);
                break;
        }
    }

    JLabel getLineNameLabel(String lineNumberIn){
        switch(lineNumberIn){
            case "1":
                return line1Name;
            case "2":
                return line2Name;
            case "3":
                return line3Name;
            case "4":
                return line4Name;
            default:
                return null;
        }
    }

    public MainWindow getMainWindow(){
        return settingsWindow.getMainWindow();
    }
}
