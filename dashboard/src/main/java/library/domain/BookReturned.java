package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.infra.AbstractEvent;
import lombok.Data;

@Data
public class BookReturned extends AbstractEvent {

    private Long id;
    private String userId;
    private String bookId;
    private Date returnDate;
    private Integer qty;
}
