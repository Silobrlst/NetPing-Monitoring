package netpingmon;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

class GuiSaver {
    //<string names>====================================================================================================
    private static final String xJsonName = "x";
    private static final String yJsonName = "y";
    private static final String widthJsonName = "width";
    private static final String heightJsonName = "height";
    private static final String maximizedJsonName = "maximized";
    private static final File guiSettings = new File("guiSettings.json");
    //</string names>===================================================================================================

    private String windowName;
    private JFrame frame = null;
    private JDialog dialog = null;

    private Map<String, JSplitPane> saveSplitPaneMap = new HashMap<>();

    private boolean windowMaximizedSave = false;

    GuiSaver(JFrame windowIn, String windowNameIn){
        windowName = windowNameIn;
        frame = windowIn;
    }

    GuiSaver(JDialog dialogIn, String windowNameIn){
        windowName = windowNameIn;
        dialog = dialogIn;
    }

    void saveWindowMaximized(boolean saveWindowMaximizedIn){
        windowMaximizedSave = saveWindowMaximizedIn;
    }

    //запоминать SplitPane
    void saveSplitPane(JSplitPane splitPaneIn, String nameIn){
        saveSplitPaneMap.put(nameIn, splitPaneIn);
    }

    //<validation>======================================================================================================
    private void validateAttribute(JSONObject jsonIn, String nameIn, Object defaultIn){
        if (!jsonIn.has(nameIn)) {
            jsonIn.put(nameIn, defaultIn);
        }
    }
    private void validateGuiSettings(JSONObject jsonIn) {
        validateAttribute(jsonIn, windowName, new JSONObject());

        JSONObject windowJSON = jsonIn.getJSONObject(windowName);
        validateAttribute(windowJSON, xJsonName, 0);
        validateAttribute(windowJSON, yJsonName, 0);
        validateAttribute(windowJSON, widthJsonName, 0);
        validateAttribute(windowJSON, heightJsonName, 0);
        validateAttribute(windowJSON, maximizedJsonName, false);

        for (String splitPaneName: saveSplitPaneMap.keySet()){
            validateAttribute(windowJSON, splitPaneName, 0);
        }
    }
    //</validation>=====================================================================================================

    void load() {
        JSONObject guiJSON = JsonLoader.loadJSON(guiSettings);
        validateGuiSettings(guiJSON);

        JSONObject windowJson = guiJSON.getJSONObject(windowName);

        if(frame != null){
            frame.setBounds(windowJson.getInt(xJsonName), windowJson.getInt(yJsonName), windowJson.getInt(widthJsonName), windowJson.getInt(heightJsonName));

            if(windowMaximizedSave){
                if(windowJson.getBoolean(maximizedJsonName)){
                    frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
                }
            }
        }else if(dialog != null){
            if(windowJson.getInt(widthJsonName) != 0 && windowJson.getInt(heightJsonName) != 0){
                dialog.setBounds(windowJson.getInt(xJsonName), windowJson.getInt(yJsonName), windowJson.getInt(widthJsonName), windowJson.getInt(heightJsonName));
            }
        }

        for (String splitPaneName: saveSplitPaneMap.keySet()){
            if (windowJson.getInt(splitPaneName) > 0) {
                saveSplitPaneMap.get(splitPaneName).setDividerLocation(windowJson.getInt(splitPaneName));
            }
        }
    }

    void save() {
        JSONObject guiJSON = JsonLoader.loadJSON(guiSettings);
        validateGuiSettings(guiJSON);

        JSONObject windowJson = guiJSON.getJSONObject(windowName);

        if(frame != null){
            boolean maximized;
            if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
                maximized = true;
            }else{
                maximized = false;
            }

            if(windowMaximizedSave){
                windowJson.put(maximizedJsonName, maximized);
            }
            if(!maximized){
                Rectangle rectangle = frame.getBounds();

                windowJson.put(xJsonName, rectangle.x);
                windowJson.put(yJsonName, rectangle.y);
                windowJson.put(widthJsonName, rectangle.width);
                windowJson.put(heightJsonName, rectangle.height);
            }
        }else if(dialog != null){
            Rectangle rectangle = dialog.getBounds();

            windowJson.put(xJsonName, rectangle.x);
            windowJson.put(yJsonName, rectangle.y);
            windowJson.put(widthJsonName, rectangle.width);
            windowJson.put(heightJsonName, rectangle.height);
        }


        for(String splitPaneName: saveSplitPaneMap.keySet()){
            windowJson.put(splitPaneName, saveSplitPaneMap.get(splitPaneName).getDividerLocation());
        }

        JsonLoader.saveJSON(guiSettings, guiJSON);
    }
}