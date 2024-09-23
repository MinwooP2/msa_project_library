package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class BookBorrowed extends AbstractEvent {

    private Long id;
    private String userId;
    private String bookId;
    private Date borrowDate;
    private Date expireDate;
    private Integer qty;
    private String status;

    public BookBorrowed(Borrow aggregate) {
        super(aggregate);
    }

    public BookBorrowed() {
        super();
    }
}
//>>> DDD / Domain Event
