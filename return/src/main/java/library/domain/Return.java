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

    private String userId;

    private String bookId;

    private Date returnDate;

    private Integer qty;

    @PostPersist
    public void onPostPersist() {
        BookReturned bookReturned = new BookReturned(this);
        bookReturned.publishAfterCommit();

        BookExpired bookExpired = new BookExpired(this);
        bookExpired.publishAfterCommit();
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
