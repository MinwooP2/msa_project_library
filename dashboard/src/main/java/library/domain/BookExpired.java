package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.infra.AbstractEvent;
import lombok.Data;

@Data
public class BookExpired extends AbstractEvent {

    private Long id;
    private String userId;
    private String bookId;
    private Integer expireDays;
    private Integer qty;
}
