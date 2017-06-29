import javax.swing.*;
import java.awt.*;

/**
 * Created by trushkov.anton on 29.06.2017.
 */
public class NetpingWidget extends JPanel {
    private JLabel deviceName;
    private JLabel status;
    private JPanel rootPanel;
    private JLabel ipAddress;

    NetpingWidget(String deviceNameIn, String ipAddressIn){
        deviceName.setText(deviceNameIn);
        ipAddress.setText(ipAddressIn);
        this.add(rootPanel);
    }

    NetpingWidget(){
        this.add(rootPanel);
    }

    public void setDeviceName(String deviceNameIn){
        deviceName.setText(deviceNameIn);
    }

    public void setOpened(boolean opened){
        if(opened){
            status.setText("открыт");
            Color color = new Color(255, 250, 0);
            rootPanel.setBackground(color);
        }else{
            status.setText("закрыт");
            Color color = new Color(100, 255, 100);
            rootPanel.setBackground(color);
        }
    }
    public void setOpened(){
        setOpened(true);
    }

    public void setDisconnected(){
        status.setText("нет связи");
        Color color = new Color(255, 100, 100);
        rootPanel.setBackground(color);
    }
}
