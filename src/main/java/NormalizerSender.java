import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class NormalizerSender {

    private final static String QUEUE_NAME = "normalizer";

    public void sendToNormalizer(String response) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        System.out.println("Sending response to Normalizer.");
        channel.basicPublish("", QUEUE_NAME, null, response.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + response + "'");

        channel.close();
        connection.close();
    }

}
