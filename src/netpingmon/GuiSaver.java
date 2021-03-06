package netpingmon;

import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class GuiSaver {
    //<string names>====================================================================================================
    private static final String xJsonName = "x";
    private static final String yJsonName = "y";
    private static final String widthJsonName = "width";
    private static final String heightJsonName = "height";
    private static final String maximizedJsonName = "maximized";
    private static final String columnJsonName = "column";
    private static final String sortOrderJsonName = "sortOrder";
    private static final File guiSettings = new File("guiSettings.json");
    //</string names>===================================================================================================

    private String windowName;
    private JFrame frame = null;
    private JDialog dialog = null;

    private Map<String, JSplitPane> saveSplitPaneMap = new HashMap<>();
    private Map<String, TableRowSorter<? extends TableModel>> tableRowSorterMap = new HashMap<>();

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

    //запоминать сортировку таблицы
    void saveTableSortKeys(TableRowSorter<? extends TableModel> tableRowSorterIn, String nameIn){
        tableRowSorterMap.put(nameIn, tableRowSorterIn);
    }

    //<validation>======================================================================================================
    private void validateJsonKey(JSONObject jsonIn, String nameIn, Object defaultIn){
        if (!jsonIn.has(nameIn)) {
            jsonIn.put(nameIn, defaultIn);
        }
    }
    private void validateGuiSettings(JSONObject jsonIn) {
        validateJsonKey(jsonIn, windowName, new JSONObject());

        JSONObject windowJSON = jsonIn.getJSONObject(windowName);
        validateJsonKey(windowJSON, xJsonName, 0);
        validateJsonKey(windowJSON, yJsonName, 0);
        validateJsonKey(windowJSON, widthJsonName, 0);
        validateJsonKey(windowJSON, heightJsonName, 0);
        validateJsonKey(windowJSON, maximizedJsonName, false);

        for (String splitPaneName: saveSplitPaneMap.keySet()){
            validateJsonKey(windowJSON, splitPaneName, 0);
        }

        for (String tableRowSorterName: tableRowSorterMap.keySet()){
            validateJsonKey(windowJSON, tableRowSorterName, new JSONObject());
            JSONObject sorterJson = windowJSON.getJSONObject(tableRowSorterName);

            validateJsonKey(sorterJson, columnJsonName, 0);
            validateJsonKey(sorterJson, sortOrderJsonName, SortOrder.ASCENDING.name());
        }
    }
    //</validation>=====================================================================================================

    void load() {
        JSONObject guiJSON = JsonLoader.loadJSON(guiSettings);
        validateGuiSettings(guiJSON);

        JSONObject windowJson = guiJSON.getJSONObject(windowName);

        if(frame != null){
            if(windowJson.getInt(widthJsonName) != 0 && windowJson.getInt(heightJsonName) != 0){
                frame.setBounds(windowJson.getInt(xJsonName), windowJson.getInt(yJsonName), windowJson.getInt(widthJsonName), windowJson.getInt(heightJsonName));
            }else{
                frame.pack();
            }

            if(windowMaximizedSave){
                if(windowJson.getBoolean(maximizedJsonName)){
                    frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
                }
            }
        }else if(dialog != null){
            if(windowJson.getInt(widthJsonName) != 0 && windowJson.getInt(heightJsonName) != 0){
                dialog.setBounds(windowJson.getInt(xJsonName), windowJson.getInt(yJsonName), windowJson.getInt(widthJsonName), windowJson.getInt(heightJsonName));
            }else{
                dialog.pack();
            }
        }

        for (String splitPaneName: saveSplitPaneMap.keySet()){
            if (windowJson.getInt(splitPaneName) > 0) {
                saveSplitPaneMap.get(splitPaneName).setDividerLocation(windowJson.getInt(splitPaneName));
            }
        }

        for(String tableRowSorterName: tableRowSorterMap.keySet() ){
            JSONObject sorterJson = windowJson.getJSONObject(tableRowSorterName);
            int column = sorterJson.getInt(columnJsonName);
            SortOrder sortOrder = SortOrder.valueOf(sorterJson.getString(sortOrderJsonName));
            RowSorter.SortKey sortKey = new RowSorter.SortKey(column, sortOrder);

            ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.addAll(tableRowSorterMap.get(tableRowSorterName).getSortKeys());
            sortKeys.add(0, sortKey);
            tableRowSorterMap.get(tableRowSorterName).setSortKeys(sortKeys);
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

        for(String tableRowSorterName: tableRowSorterMap.keySet() ){
            JSONObject sorterJson = windowJson.getJSONObject(tableRowSorterName);

            if(tableRowSorterMap.get(tableRowSorterName).getSortKeys().size() > 0){
                RowSorter.SortKey sortKey = tableRowSorterMap.get(tableRowSorterName).getSortKeys().get(0);
                int column = sortKey.getColumn();
                SortOrder sortOrder = sortKey.getSortOrder();

                sorterJson.put(columnJsonName, column);
                sorterJson.put(sortOrderJsonName, sortOrder.name());
            }

            windowJson.put(tableRowSorterName, sorterJson);
        }

        JsonLoader.saveJSON(guiSettings, guiJSON);
    }
}