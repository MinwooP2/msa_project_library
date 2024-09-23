package library.infra;

import java.util.List;
import library.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "userInfos", path = "userInfos")
public interface UserInfoRepository
    extends PagingAndSortingRepository<UserInfo, Long> {}
