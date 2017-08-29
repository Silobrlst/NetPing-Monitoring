package netpingmon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EditDisplayMessageDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField message;
    private JButton chooseTextColorButton;
    private JPanel textColor;
    private JButton chooseBackgroundColorButton;
    private JPanel backgroundColor;

    private DisplayMessage editingDisplayMessage;

    private int openResult;

    private GuiSaver guiSaver = new GuiSaver(this, "EditDisplayMessageDialog");

    EditDisplayMessageDialog(JDialog parentIn) {
        super(parentIn, ModalityType.APPLICATION_MODAL);

        setTitle("Изменение сообщения");
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

        chooseTextColorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Выбор цвета текста", textColor.getBackground());
            if(color != null){
                textColor.setBackground(color);
            }
        });
        chooseBackgroundColorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Выбор цвета фона", backgroundColor.getBackground());
            if(color != null){
                backgroundColor.setBackground(color);
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

    private void onOK() {
        editingDisplayMessage.setBackgroundColor(backgroundColor.getBackground());
        editingDisplayMessage.setMessageText(message.getText());
        editingDisplayMessage.setTextColor(textColor.getBackground());

        editingDisplayMessage.applySettings();

        guiSaver.save();
        openResult = JOptionPane.OK_OPTION;
        dispose();
    }

    private void onCancel() {
        guiSaver.save();
        openResult = JOptionPane.CANCEL_OPTION;
        dispose();
    }

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
    }

    int open(DisplayMessage displayMessageIn){
        editingDisplayMessage = displayMessageIn;

        message.setText(editingDisplayMessage.getNotAppliedMessageText());
        textColor.setBackground(editingDisplayMessage.getNotAppliedTextColor());
        backgroundColor.setBackground(editingDisplayMessage.getNotAppliedBackgroundColor());

        this.pack();
        this.setVisible(true);
        return openResult;
    }
}
