import com.rabbitmq.client.*;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class CphBusinessJSONReceiver {

    //uses the Routing strategy

    private static final String EXCHANGE_NAME = "recipientList_translator";
    private static final String bank = "BankOfNorrebro";

    public static void main(String[] argv) throws Exception {

        //receives message like: {"ssn":"123456-6543","creditScore":774,"loanAmount":1234567.0,"loanDuration":"6"}
        // which is binded with the name of the bank

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queueName = channel.queueDeclare().getQueue();

        //create a binding for CphBusinessXMLTranslator
        channel.queueBind(queueName, EXCHANGE_NAME, bank);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {

                String message = new String(body, "UTF-8");

                System.out.println(" [x] Received on key binding'" + envelope.getRoutingKey() + "':'" + message + "'");

                String jsonFinal = "";
                MessageConverter mc = new MessageConverter();
                try {
                    jsonFinal = mc.processMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // send the JSON loan request to the bank using a correlation id :)
                try {
                    CphBusinessJSONSender sender = new CphBusinessJSONSender();
                    sender.sendToBank(jsonFinal);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

}
