import javax.swing.*;
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

    private AddEditNetPingDialog addEditNetPingDialog;

    private String currentLineNumber;
    private IOLineWidget currentIOLineWidget;

    public EditIOLineDialog(AddEditNetPingDialog addEditNetPingDialogIn) {
        super(addEditNetPingDialogIn, ModalityType.APPLICATION_MODAL);
        addEditNetPingDialog = addEditNetPingDialogIn;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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

        value0ChooseTextColorButton.addActionListener(e -> value0TextColor.setBackground(JColorChooser.showDialog(this, "Выбор цвета текста 0", value0TextColor.getBackground())));
        value0ChooseBackgroundColorButton.addActionListener(e -> value0BackgroundColor.setBackground(JColorChooser.showDialog(this, "Выбор цвета фона 0", value0BackgroundColor.getBackground())));
        value1ChooseTextColorButton.addActionListener(e -> value1TextColor.setBackground(JColorChooser.showDialog(this, "Выбор цвета текста 1", value1TextColor.getBackground())));
        value1ChooseBackgroundColorButton.addActionListener(e -> value1BackgroundColor.setBackground(JColorChooser.showDialog(this, "Выбор цвета фона 1", value1BackgroundColor.getBackground())));

        this.pack();
    }

    private void onOK() {
        addEditNetPingDialog.setLineName(currentLineNumber, lineName.getText());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    public void setIOLineEditing(IOLineWidget ioLineWidgetIn, String lineNumberIn){
        currentLineNumber = lineNumberIn;
        currentIOLineWidget = ioLineWidgetIn;
        this.setTitle("Изменение линии " + currentLineNumber);
    }

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
    }
}
