package netpingmon;

import java.util.List;

interface ApplyInterface {
    void appliedAll();
    void addedNetPing(NetPingWidget netPingWidgetIn);
    void removedNetPings(List<NetPingWidget> netPingWidgetsIn);
    void changedNetPing(NetPingWidget netPingWidgetIn);
}
