package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class BookReturned extends AbstractEvent {

    private Long id;
    private String userId;
    private String bookId;
    private Date returnDate;
    private Integer qty;

    public BookReturned(Return aggregate) {
        super(aggregate);
    }

    public BookReturned() {
        super();
    }
}
//>>> DDD / Domain Event
