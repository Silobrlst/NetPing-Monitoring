import javax.swing.*;
import java.awt.event.*;

public class AddEditNetPingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField4;
    private JTextField textField3;
    private JTextField textField2;
    private JTextField textField1;
    private JButton настроитьButton;
    private JCheckBox линия1CheckBox;
    private JCheckBox линия2CheckBox;
    private JCheckBox линия3CheckBox;
    private JCheckBox линия4CheckBox;

    private boolean editing; //режим редактирования - true, режим добавления - false

    public AddEditNetPingDialog(JFrame ownerIn) {
        super(ownerIn, ModalityType.APPLICATION_MODAL);

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

        setAdding();
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void setAdding(){
        editing = false;
        this.setTitle("Добавление NetPing");
    }
    public void setEditing(NetPingWidget netPingWidgetIn){
        editing = true;
        this.setTitle("Изменение NetPing");
    }

    public boolean getEditing(){
        return editing;
    }
}
