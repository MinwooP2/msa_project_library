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
public class UserInfoViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private UserInfoRepository userInfoRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookBorrowed_then_CREATE_1(
        @Payload BookBorrowed bookBorrowed
    ) {
        try {
            if (!bookBorrowed.validate()) return;

            // view 객체 생성
            UserInfo userInfo = new UserInfo();
            // view 객체에 이벤트의 Value 를 set 함
            userInfo.setUserId(Long.valueOf(bookBorrowed.getUserId()));
            userInfo.setBorrowCnt(bookBorrowed.getQty());
            userInfo.setExpireFee(0);
            // view 레파지 토리에 save
            userInfoRepository.save(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookReturned_then_UPDATE_1(
        @Payload BookReturned bookReturned
    ) {
        try {
            if (!bookReturned.validate()) return;
            // view 객체 조회
            Optional<UserInfo> userInfoOptional = userInfoRepository.findByUserId(
                Long.valueOf(bookReturned.getUserId())
            );

            if (userInfoOptional.isPresent()) {
                UserInfo userInfo = userInfoOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                userInfo.setBorrowCnt(
                    userInfo.getBorrowCnt() - bookReturned.getQty()
                );
                // view 레파지 토리에 save
                userInfoRepository.save(userInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookExpired_then_UPDATE_2(
        @Payload BookExpired bookExpired
    ) {
        try {
            if (!bookExpired.validate()) return;
            // view 객체 조회
            Optional<UserInfo> userInfoOptional = userInfoRepository.findByUserId(
                Long.valueOf(bookExpired.getUserId())
            );

            if (userInfoOptional.isPresent()) {
                UserInfo userInfo = userInfoOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                userInfo.setExpireFee(userInfo.getExpireFee() + 500);
                // view 레파지 토리에 save
                userInfoRepository.save(userInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
