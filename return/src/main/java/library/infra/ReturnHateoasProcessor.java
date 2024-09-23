package library.infra;

import library.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class ReturnHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Return>> {

    @Override
    public EntityModel<Return> process(EntityModel<Return> model) {
        return model;
    }
}
