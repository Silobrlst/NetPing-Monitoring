package netpingmon;

import javax.swing.*;
import java.awt.event.*;

public class EditIoLineDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField lineName;
    private JTextField getOID;
    private JTextField trapOID;
    private JButton defaultButton;
    private JLabel validation;
    private JButton value0MessageButton;
    private JButton value1MessageButton;

    private IoLineWidget editingIoLineWidget;

    private EditDisplayMessageDialog editDisplayMessageDialog = new EditDisplayMessageDialog(this);

    private int openResult;

    private GuiSaver guiSaver = new GuiSaver(this, "EditIoLineDialog");

    EditIoLineDialog(JDialog parentIn) {
        super(parentIn, ModalityType.APPLICATION_MODAL);

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

        value0MessageButton.addActionListener(e -> editDisplayMessageDialog.open(editingIoLineWidget.getValue0Message()));
        value1MessageButton.addActionListener(e -> editDisplayMessageDialog.open(editingIoLineWidget.getValue1Message()));

        this.pack();

        guiSaver.saveWindowMaximized(true);
        guiSaver.load();
    }

    private void onOK() {
        boolean valid;
        boolean getOIDMatch = getOID.getText().matches("\\d*\\.(\\.?\\d*\\.?)*\\.\\d*");
        boolean trapOIDMatch = trapOID.getText().matches("\\d*\\.(\\.?\\d*\\.?)*\\.\\d*");

        valid = getOIDMatch;
        valid = valid && trapOIDMatch ;

        if(valid){
            editingIoLineWidget.setLineName(lineName.getText());
            editingIoLineWidget.setTrapReceiveOID(trapOID.getText());
            editingIoLineWidget.setSnmpGetOID(getOID.getText());

            editingIoLineWidget.applySettings();

            guiSaver.save();
            openResult = JOptionPane.OK_OPTION;
            dispose();
        }else{
            if(!getOIDMatch){
                validation.setText("неправильный OID опроса линии");
            }else if(!trapOIDMatch){
                validation.setText("неправильный OID SNMP-ловушки:");
            }

            this.pack();
        }
    }

    private void onCancel() {
        guiSaver.save();
        openResult = JOptionPane.CANCEL_OPTION;
        dispose();
    }

    private void onDefault(){
        lineName.setText("");

        switch(editingIoLineWidget.getLineNumber()){
            case "1":
                getOID.setText("1.3.6.1.4.1.25728.8900.1.1.2.1");
                break;
            case "2":
                getOID.setText("1.3.6.1.4.1.25728.8900.1.1.2.2");
                break;
            case "3":
                getOID.setText("1.3.6.1.4.1.25728.8900.1.1.2.3");
                break;
            case "4":
                getOID.setText("1.3.6.1.4.1.25728.8900.1.1.2.4");
                break;
            default:
                getOID.setText("1.3.6.1.4.1.25728.8900.1.1.2.1");
                break;
        }

        trapOID.setText("1.3.6.1.4.1.25728.8900.2.2.0");
    }


    void updateStyle(){
        editDisplayMessageDialog.updateStyle();
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
    }

    int open(IoLineWidget ioLineWidgetIn, String lineNumberIn){
        editingIoLineWidget = ioLineWidgetIn;

        lineName.setText(editingIoLineWidget.getNotAppliedLineName());
        getOID.setText(editingIoLineWidget.getNotAppliedSnmpGetOID());
        trapOID.setText(editingIoLineWidget.getNotAppliedTrapReceiveOID());

        this.setTitle("Изменение линии " + lineNumberIn);

        this.setVisible(true);
        return openResult;
    }
}
