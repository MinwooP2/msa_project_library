package library.domain;

import java.util.Date;
import java.util.List;
import library.domain.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "books", path = "books")
public interface BookRepository
    extends PagingAndSortingRepository<Book, Long> {}
