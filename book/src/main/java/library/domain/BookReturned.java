package library.domain;

import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

@Data
@ToString
public class BookReturned extends AbstractEvent {

    private Long id;
    private String userId;
    private String bookId;
    private Date returnDate;
    private Integer qty;
}
