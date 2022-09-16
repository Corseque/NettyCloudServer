package model;

import lombok.Data;

@Data
public class DeleteServerDirAlertMessage implements CloudMessage{

    private final String alert;

    public DeleteServerDirAlertMessage(String alert) {
        this.alert = alert;
    }

    @Override
    public CommandType getType() {
        return CommandType.ALERT_DELETE_SERVER_DIR;
    }
}
