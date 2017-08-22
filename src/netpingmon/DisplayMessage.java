package netpingmon;

import java.awt.*;

class DisplayMessage {
    //<current settings>================================================================================================
    private String messageText = "";
    private Color backgroundColor = new Color(0, 0, 0);
    private Color textColor = new Color(255, 255, 255);
    //</current settings>===============================================================================================

    //<to apply settings>===============================================================================================
    //новые непримёненные настройки, применятся после вызова applySettings()
    private String messageTextApply = messageText;
    private Color backgroundColorApply = backgroundColor;
    private Color textColorApply = textColor;
    //</to apply settings>==============================================================================================

    DisplayMessage(){
        messageText = "";
        backgroundColor = new Color(255, 255, 255);
        textColor = new Color(0, 0, 0);
    }

    //<set>=============================================================================================================
    void setMessageText(String textIn){
        messageTextApply = textIn;
    }
    void setBackgroundColor(Color backgroundColorIn){
        backgroundColorApply = backgroundColorIn;
    }
    void setTextColor(Color textColorIn){
        textColorApply = textColorIn;
    }
    //</set>============================================================================================================

    //<get>=============================================================================================================
    String getMessageText(){
        return messageText;
    }
    Color getBackgroundColor(){
        return backgroundColor;
    }
    Color getTextColor(){
        return textColor;
    }

    String getNotAppliedMessageText(){
        return messageTextApply;
    }
    Color getNotAppliedBackgroundColor(){
        return backgroundColorApply;
    }
    Color getNotAppliedTextColor(){
        return textColorApply;
    }
    //</get>============================================================================================================

    void copyTo(DisplayMessage displayMessageIn){
        displayMessageIn.setMessageText(getMessageText());
        displayMessageIn.setTextColor(getTextColor());
        displayMessageIn.setBackgroundColor(getBackgroundColor());
    }

    void applySettings(){
        messageText = messageTextApply;
        backgroundColor = backgroundColorApply;
        textColor = textColorApply;
    }
    void discardSettings(){
        messageTextApply = messageText;
        backgroundColorApply = backgroundColor;
        textColorApply = textColor;
    }
}
