import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * Created by antinode on 1/3/2018 AD.
 */
public class Application {

    private final static String REQUEST_QUEUE_NAME = "mdx-test-req";

    public static void main(String[] args) throws IOException, TimeoutException {

        // Connect to localhost
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");

        // Setting user and password if you want
//        factory.setUsername("secret");
//        factory.setPassword("secret");

        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);


        // Run a consumer
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {

                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message + "");

                try{
                    System.out.println("Processing tasks");
                } finally {

                    // Reply back

                    channel.basicPublish( "", properties.getReplyTo(), replyProps, (message+"!").getBytes());

                    // If get some error or something wrong send negative ack

                    System.out.println("Process tasks complete");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        boolean autoAck = false;
        channel.basicConsume(REQUEST_QUEUE_NAME, autoAck, consumer);

    }

}
