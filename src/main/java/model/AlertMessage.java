package model;

import lombok.Data;

import java.io.IOException;

@Data
public class AlertMessage implements CloudMessage{

    private final String alert;

    public AlertMessage(String alert) throws IOException {
        this.alert = alert;
    }

    @Override
    public CommandType getType() {
        return CommandType.ALERT;
    }
}
