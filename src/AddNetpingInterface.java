public interface AddNetpingInterface {
    void add(String ipAddress, String deviceNameIn);
    void change(String oldIpAddressIn, String newIpAddress, String deviceNameIn);
}
