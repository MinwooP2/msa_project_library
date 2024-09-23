package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class OutOfStock extends AbstractEvent {

    private Long id;

    public OutOfStock(Book aggregate) {
        super(aggregate);
    }

    public OutOfStock() {
        super();
    }
}
//>>> DDD / Domain Event
