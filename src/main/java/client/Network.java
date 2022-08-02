package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;
import model.CloudMessage;

import java.io.IOException;
import java.net.Socket;

//����� ����� ������� � �������� ��������� ��� ���� �������

@Slf4j
public class Network {
    protected static ObjectDecoderInputStream is;
    protected static ObjectEncoderOutputStream os;
    protected static ProcessorRegistry processorRegistry;

    public Network() {
        try {
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // read from network
    protected void readLoop() {
        try {
            while (true) {
                CloudMessage message = (CloudMessage) is.readObject();
                log.info("received: {}", message);
                processorRegistry.process(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
