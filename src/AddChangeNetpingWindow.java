import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AddChangeNetpingWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField deviceName;
    private JTextField ipAddress;
    private JLabel validation;
    private AddNetpingInterface addNetpingInterface;

    private boolean changing;
    private String oldIpAddress;

    public AddChangeNetpingWindow(AddNetpingInterface addNetpingInterfaceIn) {
        addNetpingInterface = addNetpingInterfaceIn;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        changing = false;

        this.setTitle("Добавление netping");
        this.pack();
    }

    public void setAddNetping(){
        changing = false;
        this.setTitle("Добавление netping");
        ipAddress.setText("");
        deviceName.setText("");
    }

    public void setChangeNetping(String ipAddressIn, String nameIn){
        changing = true;
        oldIpAddress = ipAddressIn;
        this.setTitle("Изменение netping");
        ipAddress.setText(ipAddressIn);
        deviceName.setText(nameIn);
    }

    private void onOK() {
        String ipRegexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

        if(ipAddress.getText().matches(ipRegexp)){
            if(changing){
                addNetpingInterface.change(oldIpAddress, ipAddress.getText(), deviceName.getText());
            }else{
                addNetpingInterface.add(ipAddress.getText(), deviceName.getText());
            }
            dispose();
        }else{
            validation.setText("не правильный ip-адрес");
            validation.setForeground(new Color(255, 0, 0));
            this.pack();
        }
    }

    private void onCancel() {
        dispose();
    }
}
