package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class Template extends AbstractEvent {

    private Long id;

    public Template(Borrow aggregate) {
        super(aggregate);
    }

    public Template() {
        super();
    }
}
//>>> DDD / Domain Event
