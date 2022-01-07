package service;

import model.MessageModel;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
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

    public String getMessages() {
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

            List<MessageModel> allMessages = new ArrayList<>();

            // Push the messages to a list
            for (Message message : messages) {
                messageModel = new MessageModel();
                messageModel.setBody(message.body());

                Map<String, MessageAttributeValue> map = message.messageAttributes();
                MessageAttributeValue val = map.get("Name");
                messageModel.setName(val.stringValue());

                allMessages.add(messageModel);
            }

            return convertToString(toXml(allMessages));

        } catch (SqsException e) {
            e.getStackTrace();
        }
        return "";
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

    private Document toXml(List<MessageModel> itemList) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Start building the XML
            Element root = doc.createElement( "Messages" );
            doc.appendChild( root );

            // Iterate through the collection
            for (MessageModel messageModel : itemList) {
                // Get the WorkItem object from the collection
                Element item = doc.createElement("Message");
                root.appendChild(item);

                // Set Id
                Element id = doc.createElement("Data");
                id.appendChild(doc.createTextNode(messageModel.getBody()));
                item.appendChild(id);

                // Set Name
                Element name = doc.createElement("User");
                name.appendChild(doc.createTextNode(messageModel.getName()));
                item.appendChild(name);
            }

            return doc;
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String convertToString(Document xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(xml);

            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch(TransformerException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
