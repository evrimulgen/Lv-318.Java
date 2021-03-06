package org.uatransport.config.modelmapperconfig;

import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;
import org.uatransport.entity.Feedback;
import org.uatransport.entity.dto.FeedbackDTO;

@Component
@RequiredArgsConstructor
public class FeedbackMapper implements Converter<Feedback, FeedbackDTO> {

    @Override
    public FeedbackDTO convert(MappingContext<Feedback, FeedbackDTO> mappingContext) {
        Feedback source = mappingContext.getSource();
        FeedbackDTO destination = mappingContext.getDestination();

        destination.setUserId(source.getUser().getId()).setCriteriaId(source.getFeedbackCriteria().getId())
                .setTransitId(source.getTransit().getId()).setAnswer(source.getAnswer()).setId(source.getId());

        return destination;
    }
}
