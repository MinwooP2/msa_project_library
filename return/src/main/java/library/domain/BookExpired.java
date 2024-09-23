package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class BookExpired extends AbstractEvent {

    private Long id;
    private Long userId;
    private Long bookId;
    private Integer expireDays;
    private Integer qty;

    public BookExpired(Return aggregate) {
        super(aggregate);
    }

    public BookExpired() {
        super();
    }
}
//>>> DDD / Domain Event
