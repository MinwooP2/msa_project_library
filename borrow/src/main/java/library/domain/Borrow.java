package library.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import library.BorrowApplication;
import library.domain.BookBorrowed;
import lombok.Data;

@Entity
@Table(name = "Borrow_table")
@Data
//<<< DDD / Aggregate Root
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userId;

    private String bookId;

    private Date borrowDate;

    private Date expireDate;

    private Integer qty;

    private String status;

    @PostPersist
    public void onPostPersist() {
        BookBorrowed bookBorrowed = new BookBorrowed(this);
        bookBorrowed.publishAfterCommit();
    }

    public static BorrowRepository repository() {
        BorrowRepository borrowRepository = BorrowApplication.applicationContext.getBean(
            BorrowRepository.class
        );
        return borrowRepository;
    }

    public void borrowBook() {
        //implement business logic here:

        BookBorrowed bookBorrowed = new BookBorrowed(this);
        bookBorrowed.publishAfterCommit();

        library.external.SearchBookListQuery searchBookListQuery = new library.external.SearchBookListQuery();
        BorrowApplication.applicationContext
            .getBean(library.external.BookService.class)
            .searchBookList(searchBookListQuery);
    }

    //<<< Clean Arch / Port Method
    public static void updateStatus(OutOfStock outOfStock) {
        //implement business logic here:

        /** Example 1:  new item 
        Borrow borrow = new Borrow();
        repository().save(borrow);

        */

        /** Example 2:  finding and process
        
        repository().findById(outOfStock.get???()).ifPresent(borrow->{
            
            borrow // do something
            repository().save(borrow);


         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
