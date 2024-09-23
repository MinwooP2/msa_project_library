package library.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import library.ReturnApplication;
import library.domain.BookExpired;
import library.domain.BookReturned;
import lombok.Data;

@Entity
@Table(name = "Return_table")
@Data
//<<< DDD / Aggregate Root
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    private Date returnDate;

    private Date expireDate;

    private Integer qty;

    @PostPersist
    public void onPostPersist() {
        // returned는 무조건 실행되고
        BookReturned bookReturned = new BookReturned(this);
        bookReturned.publishAfterCommit();

        // bookExpired만 연체 시기 판단해서 조건적으로 해주기
        if (returnDate.after(expireDate)) {
            // 연체된 경우 연체 일수를 계산
            long diffInMillies = Math.abs(returnDate.getTime() - expireDate.getTime());
            long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);

            // BookExpired 이벤트 생성 및 설정
            BookExpired bookExpired = new BookExpired(this);
            bookExpired.setExpireDays((int) diffInDays);  // 연체 일수 설정
            bookExpired.setUserId(this.getUserId());      // userId 설정
            bookExpired.setBookId(this.getBookId());      // bookId 설정
            bookExpired.setQty(this.getQty());            // qty 설정

            // BookExpired 이벤트 발행
            bookExpired.publishAfterCommit();
        }
    }

    public static ReturnRepository repository() {
        ReturnRepository returnRepository = ReturnApplication.applicationContext.getBean(
            ReturnRepository.class
        );
        return returnRepository;
    }

    public void returnBook() {
        //implement business logic here:

        BookReturned bookReturned = new BookReturned(this);
        bookReturned.publishAfterCommit();
    }
}
//>>> DDD / Aggregate Root
