import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by antinode on 1/3/2018 AD.
 */
public class Application {

    private final static String REQUEST_QUEUE_NAME = "mdx-test-req";
    private final static String REPLY_QUEUE_NAME = "mdx-test-resp";


    public static void main(String[] args) throws IOException, TimeoutException {

        // Connect to localhost
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");

        // Setting user and password if you want

//        factory.setUsername("secret");
//        factory.setPassword("secret");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String replyQueueName = channel.queueDeclare(REPLY_QUEUE_NAME, false, false, false, null).getQueue();

        while (true){
            System.out.println("Enter your message to send: ");
            Scanner scanner = new Scanner(System.in);

            String message = scanner.nextLine();

            final String corrId = UUID.randomUUID().toString();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();


            channel.basicPublish("", REQUEST_QUEUE_NAME, props, message.getBytes());
            System.out.println("Sent: " + message + " ("+corrId+")");

            // Consumer will counting.
            // In production, consumer must have only one per queue
            channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    // Require to check corrId
//                    if (properties.getCorrelationId().equals(corrId)) {
                        String replyMessage = new String(body, "UTF-8");
                        System.out.println("Receive reply: '" + replyMessage + "'" + " (" + properties.getCorrelationId() + ")");
//                    }
                }
            });
        }

    }

}
