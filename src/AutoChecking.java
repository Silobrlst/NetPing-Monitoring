import java.util.TimerTask;

public class AutoChecking {
    private Thread thread;
    private Runnable checkTask;
    private java.util.Timer timer;
    private boolean autoCheckEnable;
    private int delaySeconds;

    AutoChecking(Runnable checkTaskIn, int delaySecondsIn){
        checkTask = checkTaskIn;
        thread = new Thread(checkTask);
        timer = new java.util.Timer();
        autoCheckEnable = false;

        delaySeconds = delaySecondsIn;

        checkTask = () -> {
            checkTaskIn.run();

            if(autoCheckEnable){
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        thread = new Thread(checkTask);
                        thread.start();
                    }
                }, delaySeconds*1000);
            }
        };
    }

    void start(){
        autoCheckEnable = true;
        thread = new Thread(checkTask);
        thread.start();
    }

    void stop(){
        autoCheckEnable = false;
    }
}
