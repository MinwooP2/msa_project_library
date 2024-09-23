package library.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import library.BookApplication;
import library.domain.OutOfStock;
import library.domain.StockDecreased;
import library.domain.StockIncreased;
import lombok.Data;

@Entity
@Table(name = "Book_table")
@Data
//<<< DDD / Aggregate Root
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    private String author;

    private Integer stock;

    @PostPersist
    public void onPostPersist() {
        StockIncreased stockIncreased = new StockIncreased(this);
        stockIncreased.publishAfterCommit();

        StockDecreased stockDecreased = new StockDecreased(this);
        stockDecreased.publishAfterCommit();

        OutOfStock outOfStock = new OutOfStock(this);
        outOfStock.publishAfterCommit();
    }

    public static BookRepository repository() {
        BookRepository bookRepository = BookApplication.applicationContext.getBean(
            BookRepository.class
        );
        return bookRepository;
    }

    //<<< Clean Arch / Port Method
    public static void decreaseStock(BookBorrowed bookBorrowed) {
        //implement business logic here:

        /** Example 1:  new item 
        Book book = new Book();
        repository().save(book);

        StockIncreased stockIncreased = new StockIncreased(book);
        stockIncreased.publishAfterCommit();
        OutOfStock outOfStock = new OutOfStock(book);
        outOfStock.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(bookBorrowed.get???()).ifPresent(book->{
            
            book // do something
            repository().save(book);

            StockIncreased stockIncreased = new StockIncreased(book);
            stockIncreased.publishAfterCommit();
            OutOfStock outOfStock = new OutOfStock(book);
            outOfStock.publishAfterCommit();

         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void increaseStock(BookReturned bookReturned) {
        //implement business logic here:

        /** Example 1:  new item 
        Book book = new Book();
        repository().save(book);

        StockDecreased stockDecreased = new StockDecreased(book);
        stockDecreased.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(bookReturned.get???()).ifPresent(book->{
            
            book // do something
            repository().save(book);

            StockDecreased stockDecreased = new StockDecreased(book);
            stockDecreased.publishAfterCommit();

         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
