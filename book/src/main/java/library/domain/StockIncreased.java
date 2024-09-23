package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class StockIncreased extends AbstractEvent {

    private Long id;
    private Integer stock;

    public StockIncreased(Book aggregate) {
        super(aggregate);
    }

    public StockIncreased() {
        super();
    }
}
//>>> DDD / Domain Event
