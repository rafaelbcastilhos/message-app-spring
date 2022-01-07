package service;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsService {
    private static SqsClient INSTANCE;

    public static SqsClient getInstance() {
        if (INSTANCE == null)
            INSTANCE = SqsClient.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();

        return INSTANCE;
    }

    public static final String QUEUE_NAME = "MessageAppSpring.fifo";
}

