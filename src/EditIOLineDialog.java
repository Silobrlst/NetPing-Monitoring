import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EditIOLineDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField lineName;
    private JTextField getOID;
    private JTextField trapOID;

    private JTextField value0Message;
    private JPanel value0TextColor;
    private JPanel value0BackgroundColor;
    private JButton value0ChooseTextColorButton;
    private JButton value0ChooseBackgroundColorButton;

    private JTextField value1Message;
    private JPanel value1TextColor;
    private JPanel value1BackgroundColor;
    private JButton value1ChooseTextColorButton;
    private JButton value1ChooseBackgroundColorButton;
    private JButton defaultButton;
    private JLabel validation;

    private IOLineWidget currentIOLineWidget;

    private int openResult;

    public EditIOLineDialog(AddEditNetPingDialog addEditNetPingDialogIn) {
        super(addEditNetPingDialogIn, ModalityType.APPLICATION_MODAL);

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

        value0ChooseTextColorButton.addActionListener(e -> value0TextColor.setBackground(JColorChooser.showDialog(this, "Выбор цвета текста 0", value0TextColor.getBackground())));
        value0ChooseBackgroundColorButton.addActionListener(e -> value0BackgroundColor.setBackground(JColorChooser.showDialog(this, "Выбор цвета фона 0", value0BackgroundColor.getBackground())));
        value1ChooseTextColorButton.addActionListener(e -> value1TextColor.setBackground(JColorChooser.showDialog(this, "Выбор цвета текста 1", value1TextColor.getBackground())));
        value1ChooseBackgroundColorButton.addActionListener(e -> value1BackgroundColor.setBackground(JColorChooser.showDialog(this, "Выбор цвета фона 1", value1BackgroundColor.getBackground())));

        this.pack();
    }

    private void onOK() {
        boolean valid = true;
        boolean getOIDMatch = getOID.getText().matches("\\d*\\.(\\.?\\d*\\.?)*\\.\\d*");
        boolean trapOIDMatch = trapOID.getText().matches("\\d*\\.(\\.?\\d*\\.?)*\\.\\d*");

        valid = valid && getOIDMatch;
        valid = valid && trapOIDMatch ;

        if(valid){
            currentIOLineWidget.setLineName(lineName.getText());
            currentIOLineWidget.setTrapReceiveOID(trapOID.getText());
            currentIOLineWidget.setSnmpGetOID(getOID.getText());

            DisplayMessageSettings displayMessageSettings0 = new DisplayMessageSettings();
            displayMessageSettings0.backgroundColor = value0BackgroundColor.getBackground();
            displayMessageSettings0.messageText = value0Message.getText();
            displayMessageSettings0.textColor = value0TextColor.getBackground();
            currentIOLineWidget.setValue0Message(displayMessageSettings0);

            DisplayMessageSettings displayMessageSettings1 = new DisplayMessageSettings();
            displayMessageSettings1.backgroundColor = value1BackgroundColor.getBackground();
            displayMessageSettings1.messageText = value1Message.getText();
            displayMessageSettings1.textColor = value1TextColor.getBackground();
            currentIOLineWidget.setValue1Message(displayMessageSettings1);

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
        openResult = JOptionPane.CANCEL_OPTION;
        dispose();
    }

    private void onDefault(){
        lineName.setText("");

        switch(currentIOLineWidget.getLineNumber()){
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

        value0Message.setText("");
        value0TextColor.setBackground(new Color(0, 0, 0));
        value0BackgroundColor.setBackground(new Color(255, 255, 255));

        value1Message.setText("");
        value1TextColor.setBackground(new Color(0, 0, 0));
        value1BackgroundColor.setBackground(new Color(255, 255, 255));
    }


    public void setIOLineEditing(IOLineWidget ioLineWidgetIn, String lineNumberIn){
        currentIOLineWidget = ioLineWidgetIn;

        lineName.setText(currentIOLineWidget.getNotAppliedLineName());
        getOID.setText(currentIOLineWidget.getNotAppliedSnmpGetOID());
        trapOID.setText(currentIOLineWidget.getNotAppliedTrapReceiveOID());

        DisplayMessageSettings displayMessageSettings1 = currentIOLineWidget.getNotAppliedValue1Message();
        value1Message.setText(displayMessageSettings1.messageText);
        value1TextColor.setBackground(displayMessageSettings1.textColor);
        value1BackgroundColor.setBackground(displayMessageSettings1.backgroundColor);

        DisplayMessageSettings displayMessageSettings0 = currentIOLineWidget.getNotAppliedValue0Message();
        value0Message.setText(displayMessageSettings0.messageText);
        value0TextColor.setBackground(displayMessageSettings0.textColor);
        value0BackgroundColor.setBackground(displayMessageSettings0.backgroundColor);

        this.setTitle("Изменение линии " + lineNumberIn);
    }

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
    }

    int open(){
        this.setVisible(true);
        return openResult;
    }
}
