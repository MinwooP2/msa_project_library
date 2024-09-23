package library.domain;

import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

@Data
@ToString
public class OutOfStock extends AbstractEvent {

    private Long id;
}
