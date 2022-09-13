package model;

import lombok.Data;

@Data
public class ReplaceFileAlertMessage implements CloudMessage{

    private final String alert;

    public ReplaceFileAlertMessage(String alert) {
        this.alert = alert;
    }

    @Override
    public CommandType getType() {
        return CommandType.ALERT_REPLACE_FILE;
    }
}
