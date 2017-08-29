package netpingmon;

import java.awt.*;

class DefaultSettings {
    static final Color backgroundColor0 = new Color(0x990000);
    static final Color textColor0 = new Color(0xCCFFFF);
    static final Color backgroundColor1 = new Color(0x006600);
    static final Color textColor1 = new Color(0xCCFFCC);

    static final Color backgroundColorDisconnected = new Color(0x990000);
    static final Color textColorDisconnected = new Color(0xCCFFFF);
    static final Color backgroundColorConnected = new Color(0x006600);
    static final Color textColorConnected = new Color(0xCCFFCC);

    static final int gridNetPingsColumns = 10;
    static final int gridNetPingsRows = 2;

    static final int checkingDelay = 60;
    static final int timeOut = 3;
    static final int retries = 4;
    static final String snmpPort = "64123";
    static final String snmpTrapPort = "162";
    static final String snmpCommunity = "SWITCH";

    static final String style = "Metal";
    static final boolean trayIcon = false;

    static final String linesGridType = "1x4";

    static final String snmpGetLine1Oid = "1.3.6.1.4.1.25728.8900.1.1.2.1";

    static final boolean active = true;
}
