package core;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class CphBusinessJSONSender {

    //1.figure out how to send the message at the bank's specified address and exchange
    //2. send the message and set the reply que and correlationId :)

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue_json";
    private String replyQueueName;

    public CphBusinessJSONSender() throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://student:cph@datdb.cphbusiness.dk:5672");

        connection = factory.newConnection();
        channel = connection.createChannel();

        replyQueueName = channel.queueDeclare().getQueue();;
    }

    //method that sends the message to the bank and waits for the response.
    public String call(String message) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("cphbusiness.bankJSON", requestQueueName, props, message.getBytes("UTF-8"));

        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                    response.offer(new String(body, "UTF-8"));
                }
            }
        });

        return response.take();
    }

    public void close() throws IOException {
        connection.close();
    }


    public void sendToBank(String message) {
        CphBusinessJSONSender cphBuisnessJson = null;
        String response = null;
        try {
            cphBuisnessJson = new CphBusinessJSONSender();

            System.out.println(" [x] Requesting response from CPHBusiness JSON bank.");
            //here insert the message generated in json and call this in main

            //System.out.println("Sending the following XML: " + message);
            response = cphBuisnessJson.call(message);
            //response = cphBuisnessJson.call("{\"ssn\":1605789787,\"creditScore\":598,\"loanAmount\":10.0,\"loanDuration\":360}");
            System.out.println(" [.] Got response back '" + response + "'");

            //before sending the message, add the name of the bank to it
            response = addBankNameJSON(response, "BankOfNorrebro");

            //send the response to the normalizer :)
            NormalizerSender nz = new NormalizerSender();
            nz.sendToNormalizer(response);

        }
        catch  (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (cphBuisnessJson!= null) {
                try {
                    cphBuisnessJson.close();
                }
                catch (IOException _ignore) {}
            }
        }
    }

    private String addBankNameJSON(String message, String append) {
        return "{"+"\"bank\":"+"\""+append+"\""+","+message.substring(1, message.length());
    }
}
