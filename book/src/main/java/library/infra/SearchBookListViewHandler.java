package library.infra;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import library.config.kafka.KafkaProcessor;
import library.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class SearchBookListViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private SearchBookListRepository searchBookListRepository;
    //>>> DDD / CQRS
}
