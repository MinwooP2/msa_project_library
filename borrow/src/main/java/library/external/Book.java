package library.external;

import java.util.Date;
import lombok.Data;

@Data
public class Book {

    private Long id;
    private String title;
    private String author;
    private Integer stock;
}
