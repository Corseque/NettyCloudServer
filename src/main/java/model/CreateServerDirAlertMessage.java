package model;

import lombok.Data;

@Data
public class CreateServerDirAlertMessage implements CloudMessage{

    private final String alert;

    public CreateServerDirAlertMessage(String alert) {
        this.alert = alert;
    }

    @Override
    public CommandType getType() {
        return CommandType.ALERT_CREATE_SERVER_DIR;
    }
}
