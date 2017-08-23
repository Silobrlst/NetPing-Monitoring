package netpingmon;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import java.util.Timer;

public class LogsWindow extends JFrame {
    private JPanel rootPanel;
    private JTextArea textArea;

    private static File logsFile = new File("netping.log");

    private GuiSaver guiSaver = new GuiSaver(this, "LogsWindow");

    private java.util.Timer timer = new Timer();
    private Thread thread;
    private Runnable updateLog;

    LogsWindow(){
        setTitle("Журнал событий NetPing");

        getContentPane().add(rootPanel);

        updateLog = () -> {
            String logs = JsonLoader.loadDataFromfile(logsFile);

            if(!logs.equals(textArea.getText())){
                textArea.setText(logs);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        };

        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

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

    void open(){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                thread = new Thread(updateLog);
                thread.start();
            }
        }, 0, 5000);
        setVisible(true);

        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    void updateStyle(){
        SwingUtilities.updateComponentTreeUI(this);
    }
}
