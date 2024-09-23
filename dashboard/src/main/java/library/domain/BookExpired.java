package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.infra.AbstractEvent;
import lombok.Data;

@Data
public class BookExpired extends AbstractEvent {

    private Long id;
    private Long userId;
    private Long bookId;
    private Integer expireDays;
    private Integer qty;
}
