package library.external;

import java.util.Date;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "book", url = "${api.url.book}")
public interface SearchBookListService {
    @GetMapping(path = "/searchBookLists")
    public List<SearchBookList> getSearchBookList();

    @GetMapping(path = "/searchBookLists/{id}")
    public SearchBookList getSearchBookList(@PathVariable("id") Long id);
}
