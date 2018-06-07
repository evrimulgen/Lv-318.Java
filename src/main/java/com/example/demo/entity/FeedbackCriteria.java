package com.example.demo.entity;

import com.example.demo.service.converter.ConversionStrategy;
import com.example.demo.service.converter.impl.BusyHoursDurationConversionStrategy;
import com.example.demo.service.converter.impl.RatingConversionStrategy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Data
@Inheritance
@DiscriminatorColumn(name = "CRITERIA_TYPE")
@DiscriminatorValue("FEEDBACK_CRITERIA")
@Accessors(chain = true)
@Table(name = "feedback_criteria")
@EqualsAndHashCode(of = "id")
public class FeedbackCriteria {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer groupId;

    private String question;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", updatable = false)
    private FeedbackType type;

    @RequiredArgsConstructor
    public enum FeedbackType {
        RATING(new RatingConversionStrategy()),
        BUSY_HOURS(new BusyHoursDurationConversionStrategy()),
        ROUTE(new RatingConversionStrategy()),
        AXCEPTER(new RatingConversionStrategy()),
        CAPACITY(new RatingConversionStrategy());

        private final ConversionStrategy<?> conversionStrategy;

        @SuppressWarnings("unchecked")
        public <T> T convertFeedback(Feedback feedback) {
            return (T) conversionStrategy.convert(feedback);
        }
    }
}
