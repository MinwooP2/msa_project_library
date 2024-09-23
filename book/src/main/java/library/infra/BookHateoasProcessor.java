package library.infra;

import library.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class BookHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Book>> {

    @Override
    public EntityModel<Book> process(EntityModel<Book> model) {
        return model;
    }
}
