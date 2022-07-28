package client;


import model.CloudMessage;

import java.io.IOException;

public interface MessageProcessor {

    void processMessage(CloudMessage msg) throws IOException;

}
