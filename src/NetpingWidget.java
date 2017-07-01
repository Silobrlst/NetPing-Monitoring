import javax.swing.*;
import java.awt.*;

public class NetpingWidget extends JPanel {
    private JLabel deviceName;
    private JLabel status;
    private JPanel rootPanel;
    private JLabel ipAddress;
    private JPanel ipStatePanel;

    private NetpingStateEnum state;

    NetpingWidget(String ipAddressIn, String deviceNameIn){
        deviceName.setText(deviceNameIn);
        ipAddress.setText(ipAddressIn);
        this.add(rootPanel);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void setDeviceName(String deviceNameIn){
        deviceName.setText(deviceNameIn);
    }

    public void setState(NetpingStateEnum stateIn){
        state = stateIn;

        switch(state){
            case Opened:
                status.setText("открыт");
                setColor(new Color(255, 250, 0));
                break;
            case Closed:
                status.setText("закрыт");
                setColor(new Color(100, 255, 100));
                break;
            case Disconneted:
                status.setText("нет связи");
                setColor(new Color(255, 100, 100));
                break;
        }
    }

    public void setChecking(){
        status.setText("проверка...");
    }

    public String getDeviceName(){
        return deviceName.getText();
    }

    public NetpingStateEnum getState(){
        return state;
    }


    private void setColor(Color colorIn){
        rootPanel.setBackground(colorIn);
        ipStatePanel.setBackground(colorIn);
    }
}
