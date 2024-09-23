package library.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import library.BorrowApplication;
import library.domain.BookBorrowed;
import lombok.Data;

@Entity
@Table(name = "Borrow_table")
@Data
//<<< DDD / Aggregate Root
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    private Date borrowDate;

    private Date expireDate;

    private Integer qty;

    private String status;

    @PostPersist
    public void onPostPersist() {
        BookBorrowed bookBorrowed = new BookBorrowed(this);
        bookBorrowed.publishAfterCommit();
    }

    public static BorrowRepository repository() {
        BorrowRepository borrowRepository = BorrowApplication.applicationContext.getBean(
            BorrowRepository.class
        );
        return borrowRepository;
    }

    public void borrowBook() {
        //implement business logic here:

        BookBorrowed bookBorrowed = new BookBorrowed(this);
        bookBorrowed.publishAfterCommit();
    }

    public static void updateStatus(OutOfStock outOfStock) {
        // outOfStock에서 전달된 bookId로 Borrow 엔티티를 찾습니다.
        repository().findById(outOfStock.getBorrowId()).ifPresent(borrow -> {
        // 해당 Borrow 엔티티의 status를 "canceled"로 변경합니다.
        borrow.setStatus("canceled");
        // 변경된 Borrow 엔티티를 저장합니다.
        repository().save(borrow);
    });
}

}
//>>> DDD / Aggregate Root
