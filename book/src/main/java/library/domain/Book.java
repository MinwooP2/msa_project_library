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
        StockDecreased stockDecreased = new StockDecreased(this);
        stockDecreased.publishAfterCommit();

        StockIncreased stockIncreased = new StockIncreased(this);
        stockIncreased.publishAfterCommit();

        OutOfStock outOfStock = new OutOfStock(this);
        outOfStock.publishAfterCommit();
    }

    public static BookRepository repository() {
        BookRepository bookRepository = BookApplication.applicationContext.getBean(
            BookRepository.class
        );
        return bookRepository;
    }

    public static void decreaseStock(BookBorrowed bookBorrowed) {
        // 사용자가 book을 borrow했을 때, 해당 book에 대한 stock을 차감시키고 
        // 만약 차감시키지 못할 만큼 많은 양을 대출한다면 OutOfStcok 이벤트 발행 
        repository().findById(bookBorrowed.getBookId()).ifPresent(book->{
             if(book.getStock() >= bookBorrowed.getQty()){
                // 재고가 충분하면 차감하고 저장 후 StockDecreased 이벤트 publish 
                book.setStock(book.getStock() - bookBorrowed.getQty());
                repository().save(book);

                StockDecreased stockDecreased = new StockDecreased(book);
                stockDecreased.setBookId(bookBorrowed.getBookId());
                stockDecreased.setStock(bookBorrowed.getQty());
                stockDecreased.publishAfterCommit();
                System.out.println("StockDecreased Event Published!!");
            }else{
                // 재고가 충분하지 않으면 차감하지 않고outOfStock 이벤트 publish 
                OutOfStock outOfStock = new OutOfStock(book);
                outOfStock.setBookId(bookBorrowed.getBookId());
                outOfStock.publishAfterCommit();
                System.out.println("OutOfStock Event Published!!");
            }
         });

    }

    public static void increaseStock(BookReturned bookReturned) {
       repository().findById(bookReturned.getBookId()).ifPresent(book->{
            // 재고가 충분하면 차감하고 저장 후 StockDecreased 이벤트 publish 
            book.setStock(book.getStock() + bookReturned.getQty());
            repository().save(book);

            StockIncreased stockIncreased = new StockIncreased(book);
            stockIncreased.setBookId(bookReturned.getBookId());
            stockIncreased.setStock(bookReturned.getQty());
            stockIncreased.publishAfterCommit();
            
            System.out.println("StockIncreased Event Published!!");
         });
    }

}
//>>> DDD / Aggregate Root
