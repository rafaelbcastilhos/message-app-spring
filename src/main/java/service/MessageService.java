package service;

import model.MessageModel;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import java.util.*;

@Component
public class MessageService {
    public void purge() {
        GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(SqsService.QUEUE_NAME)
                .build();

        PurgeQueueRequest queueRequest = PurgeQueueRequest.builder()
                .queueUrl(SqsService.getInstance().getQueueUrl(getQueueRequest).queueUrl())
                .build();

        SqsService.getInstance().purgeQueue(queueRequest);
    }

    public List<MessageModel> getMessages() {
        List<MessageModel> allMessages = new ArrayList<>();

        List attr = new ArrayList<String>();
        attr.add("Name");

        try {
            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(SqsService.QUEUE_NAME)
                    .build();

            String queueUrl = SqsService.getInstance().getQueueUrl(getQueueRequest).queueUrl();

            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .messageAttributeNames(attr)
                    .build();
            List<Message> messages = SqsService.getInstance().receiveMessage(receiveRequest).messages();

            MessageModel messageModel;

            // Push the messages to a list
            for (Message message : messages) {
                messageModel = new MessageModel();
                messageModel.setBody(message.body());

                Map<String, MessageAttributeValue> map = message.messageAttributes();
                MessageAttributeValue val = map.get("Name");
                messageModel.setName(val.stringValue());

                allMessages.add(messageModel);
            }
        } catch (SqsException e) {
            e.getStackTrace();
        }
        return allMessages;
    }

    public void processMessage(MessageModel message) {
        try {
            MessageAttributeValue attributeValue = MessageAttributeValue.builder()
                    .stringValue(message.getName())
                    .dataType("String")
                    .build();

            Map<String, MessageAttributeValue> myMap = new HashMap<>();
            myMap.put("Name", attributeValue);

            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(SqsService.QUEUE_NAME)
                    .build();

            String queueUrl = SqsService.getInstance().getQueueUrl(getQueueRequest).queueUrl();

            UUID uuid = UUID.randomUUID();
            String msgId1 = uuid.toString();

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageAttributes(myMap)
                    .messageGroupId("GroupA")
                    .messageDeduplicationId(msgId1)
                    .messageBody(message.getBody())
                    .build();
            SqsService.getInstance().sendMessage(sendMsgRequest);
        } catch (SqsException e) {
            e.getStackTrace();
        }
    }
}
