public interface NetPingWidgetListener {
    void created(NetPingWidget netPingWidgetIn);
    void removed(NetPingWidget netPingWidgetIn);
    void changedSettings(NetPingWidget netPingWidgetIn);
    void changedIOLineSettings(NetPingWidget netPingWidgetIn, IOLineWidget ioLineWidgetIn, String lineNumberIn);
}
