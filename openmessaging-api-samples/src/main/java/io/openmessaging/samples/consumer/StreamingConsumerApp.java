package io.openmessaging.samples.consumer;

import io.openmessaging.Message;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.ResourceManager;
import io.openmessaging.consumer.StreamIterator;
import io.openmessaging.consumer.StreamingConsumer;
import io.openmessaging.exception.OMSResourceNotExistException;
import java.util.List;

public class StreamingConsumerApp {
    public static void main(String[] args) throws OMSResourceNotExistException {
        final MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:rocketmq://alice@rocketmq.apache.org/us-east:default_space");
        messagingAccessPoint.startup();
        System.out.println("MessagingAccessPoint startup OK");
        ResourceManager resourceManager = messagingAccessPoint.resourceManager();

        String targetQueue = "HELLO_QUEUE";
        resourceManager.createQueue(targetQueue, OMS.newKeyValue());

        List<String> streams = resourceManager.listStreams(targetQueue);

        final StreamingConsumer streamingConsumer = messagingAccessPoint.createStreamingConsumer();

        streamingConsumer.startup();

        String targetStream = streams.get(0);

        StreamIterator streamIterator = streamingConsumer.attachStream(targetStream, streamingConsumer.earliest(targetStream));

        while (streamIterator.hasNext()) {
            Message message = streamIterator.next();
            System.out.println("Received one message: " + message);
        }

        //All the messages in the stream has been consumed.

        //Now consume the messages in reverse order
        while (streamIterator.hasPrevious()) {
            Message message = streamIterator.previous();
            System.out.println("Received one message again: " + message);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                streamingConsumer.shutdown();
                messagingAccessPoint.shutdown();
            }
        }));

    }
}