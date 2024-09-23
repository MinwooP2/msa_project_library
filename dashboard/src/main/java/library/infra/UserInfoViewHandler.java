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

        // view 객체 조회
        Optional<UserInfo> userInfoOptional = userInfoRepository.findById(
            Long.valueOf(bookBorrowed.getUserId())
        );

        if (userInfoOptional.isPresent()) {
            // 기존 객체가 있을 경우 borrowCnt 증가
            UserInfo userInfo = userInfoOptional.get();
            userInfo.setBorrowCnt(userInfo.getBorrowCnt() + bookBorrowed.getQty());
            userInfoRepository.save(userInfo);
        } else {
            // 객체가 없을 경우 새로 생성하여 저장
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(Long.valueOf(bookBorrowed.getUserId()));
            userInfo.setBorrowCnt(bookBorrowed.getQty());
            userInfo.setExpireFee(0);
            userInfoRepository.save(userInfo);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    // 반납 시, 대출 중인 책 수량을 감소
    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookReturned_then_UPDATE_1(
        @Payload BookReturned bookReturned
    ) {
        try {
            if (!bookReturned.validate()) return;
            // view 객체 조회
            Optional<UserInfo> userInfoOptional = userInfoRepository.findById(
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

    // 연체 시, 연체료를 추가
    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookExpired_then_UPDATE_2(
        @Payload BookExpired bookExpired
    ) {
        try {
            if (!bookExpired.validate()) return;
            // view 객체 조회
            Optional<UserInfo> userInfoOptional = userInfoRepository.findById(
                Long.valueOf(bookExpired.getUserId())
            );
            

            if (userInfoOptional.isPresent()) {
                UserInfo userInfo = userInfoOptional.get();

                // 연체 기간에 따른 벌금 계산
                int expireDays = bookExpired.getExpireDays();
                int fee = 0;

                if (expireDays >= 1 && expireDays <= 7) {
                    fee = 500;
                } else if (expireDays >= 8 && expireDays <= 14) {
                    fee = 1000;
                } else if (expireDays > 14) {
                    fee = 2000;
                }


                //  // 벌금을 현재 값에 더함
                userInfo.setExpireFee(userInfo.getExpireFee() + fee);

                // view 레파지 토리에 save
                userInfoRepository.save(userInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
