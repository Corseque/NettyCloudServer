package model;

import lombok.Data;

@Data
public class NewUserRegisteredMessage implements CloudMessage{

    private final boolean userAlreadyExists;

    public NewUserRegisteredMessage(boolean userAlreadyExists) {
        this.userAlreadyExists = userAlreadyExists;
    }

    @Override
    public CommandType getType() {
        return CommandType.NEW_USER_REGISTERED;
    }
}
