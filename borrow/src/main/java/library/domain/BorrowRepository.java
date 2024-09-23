package library.domain;

import library.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "borrows", path = "borrows")
public interface BorrowRepository
    extends PagingAndSortingRepository<Borrow, Long> {}
