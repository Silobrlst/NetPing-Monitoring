import java.util.ArrayList;

public class NetPingWidgets extends ArrayList<NetPingWidget> {
    private ArrayList<NetPingWidgetListener> listeners;
    private MainWindow mainWindow;

    NetPingWidgets(MainWindow mainWindowIn){
        mainWindow = mainWindowIn;
        listeners = new ArrayList<>();
    }

    void addListener(NetPingWidgetListener netPingWidgetListenerIn){
        listeners.add(netPingWidgetListenerIn);
    }

    NetPingWidget newNetPingWidget(String ipAddressIn){
        NetPingWidget netPingWidget = new NetPingWidget(listeners, mainWindow, ipAddressIn);
        this.add(netPingWidget);
        listeners.forEach(netPingWidgetListener -> netPingWidgetListener.created(netPingWidget));
        return netPingWidget;
    }

    void removeNetPingWidget(NetPingWidget netPingWidgetIn){
        this.remove(netPingWidgetIn);
        listeners.forEach(netPingWidgetListener -> netPingWidgetListener.removed(netPingWidgetIn));
    }

    NetPingWidget getNetPingWidget(String ipAddressIn){
        for(NetPingWidget netPingWidget: this){
            if(netPingWidget.getIpAddress().equals(ipAddressIn)){
                return netPingWidget;
            }
        }

        return null;
    }
}
