package org.uatransport.service.implementation;

import com.google.common.collect.Range;
import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uatransport.entity.Feedback;
import org.uatransport.entity.FeedbackCriteria;
import org.uatransport.entity.Stop;
import org.uatransport.entity.dto.FeedbackDTO;
import org.uatransport.entity.dto.HeatMapDTO;
import org.uatransport.exception.ResourceNotFoundException;
import org.uatransport.repository.FeedbackRepository;
import org.uatransport.service.FeedbackService;
import org.uatransport.service.StopService;
import org.uatransport.service.converter.impl.ConflictTypeConverter;
import org.uatransport.service.converter.impl.FeedbackTypeConverter;
import org.uatransport.service.converter.impl.RatingConverter;
import org.uatransport.service.converter.model.CapacityHourFeedback;
import org.uatransport.service.converter.model.CapacityRouteFeedback;
import org.uatransport.service.converter.model.QuantityLoadFeedback;
import org.uatransport.service.converter.model.SimpleFeedback;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StopService stopService;

    @Override
    public Feedback addFeedback(FeedbackDTO feedbackDTO) {
        if (feedbackDTO == null) {
            throw new IllegalArgumentException("Parameter should not be null");
        }
        return feedbackRepository.save(feedbackDTO.convertToEntity());
    }

    @Override
    public List<Feedback> addAll(List<FeedbackDTO> feedbackDTOList) {

        return Streams.stream(feedbackRepository.saveAll(FeedbackDTO.toEntity(feedbackDTOList)))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Feedback getById(Integer id) {

        return feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("Feedback with id '%s' not found", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getByTransitId(Integer id) {
        return feedbackRepository.findByTransitId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getByCriteriaId(Integer id) {
        return feedbackRepository.findByFeedbackCriteriaId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getByUserId(Integer id) {
        return feedbackRepository.findByUserIdOrderByDateDesc(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getByTransitIdAndFeedbackCriteriaType(Integer transitId,
                                                                FeedbackCriteria.FeedbackType feedbackType) {
        return feedbackRepository.findByTransitIdAndFeedbackCriteriaType(transitId, feedbackType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getByTransitCategoryIdAndFeedbackCriteriaType(Integer transitCategoryId,
                                                                        FeedbackCriteria.FeedbackType feedbackType) {
        return feedbackRepository.findByTransitCategoryIdAndFeedbackCriteriaType(transitCategoryId, feedbackType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getByTransitIdAndFeedbackCriteriaTypeAndUserId(Integer transitId,
                                                                         FeedbackCriteria.FeedbackType feedbackType,
                                                                         Integer userId) {
        return feedbackRepository.findByTransitIdAndFeedbackCriteriaTypeAndUserId(transitId, feedbackType, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getRatingByTransitId(Integer transitId) {
        List<Feedback> feedbackList = getByTransitIdAndFeedbackCriteriaType(transitId,
            FeedbackCriteria.FeedbackType.RATING);
        List<Feedback> quantityFeedbackList = getByTransitIdAndFeedbackCriteriaType(transitId,
            FeedbackCriteria.FeedbackType.QUANTITY_LOAD);
        List<Feedback> conflictFeedbackList = getByTransitIdAndFeedbackCriteriaType(transitId,
            FeedbackCriteria.FeedbackType.CONFLICT);

        return DoubleStream.of(getAverageRate(feedbackList), getAverageQualityRate(quantityFeedbackList),
            getAverageConflictFeedBacksRate(conflictFeedbackList)).average().orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getRatingByTransitCategoryId(Integer transitCategoryId) {

        return getAverageRate(
            getByTransitCategoryIdAndFeedbackCriteriaType(transitCategoryId, FeedbackCriteria.FeedbackType.RATING));
    }

    @Override
    @Transactional(readOnly = true)
    public Double getRatingByTransitIdAndUserId(Integer transitId, Integer userId) {
        List<Feedback> feedbackList = getByTransitIdAndFeedbackCriteriaTypeAndUserId(transitId,
            FeedbackCriteria.FeedbackType.RATING, userId);
        List<Feedback> quantityFeedbackList = getByTransitIdAndFeedbackCriteriaTypeAndUserId(transitId,
            FeedbackCriteria.FeedbackType.QUANTITY_LOAD, userId);
        List<Feedback> conflictFeedbackList = getByTransitIdAndFeedbackCriteriaTypeAndUserId(transitId,
            FeedbackCriteria.FeedbackType.CONFLICT, userId);

        return DoubleStream
            .of(getAverageRate(feedbackList), getAverageQualityRate(quantityFeedbackList),
                getAverageConflictFeedBacksRate(conflictFeedbackList))
            .filter(rate -> rate > 0)
            .average()
            .orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Stop, Double> getStopCapacityMap(Integer transitId, Stop.Direction direction, Stop... stops) {
        List<Stop> stopList = stops.length > 0 ? Arrays.asList(stops)
            : stopService.getByTransitIdAndDirection(transitId, direction);
        List<CapacityRouteFeedback> capacityRouteFeedbackList = convertCapacityRouteFeedBacks(transitId);
        Map<Stop, Double> capacityMap = new TreeMap<>(Comparator.comparingInt(stopList::indexOf));

        for (Stop stop : stopList) {
            capacityMap.put(stop, getCapacityByTransitAndStops(stop, capacityRouteFeedbackList, stopList));
        }
        return capacityMap;
    }

    @Override
    @Transactional(readOnly = true)
    public EnumMap<SimpleFeedback, Double> getSimpleAnswerPercentageMap(Integer transitId) {
        EnumMap<SimpleFeedback, Double> simpleFeedbackDoubleEnumMap = new EnumMap<>(SimpleFeedback.class);

        for (SimpleFeedback simpleFeedback : SimpleFeedback.values()) {
            double percentValue = 100 * safeDivision(countByValue(simpleFeedback, transitId),
                (double) countAllAccepterFeedBacks(transitId));
            simpleFeedbackDoubleEnumMap.put(simpleFeedback, percentValue);
        }

        return simpleFeedbackDoubleEnumMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Double> getHourCapacityMap(Integer transitId) {
        int hoursInDay = 24;
        Map<Integer, Double> capacityMap = new TreeMap<>();
        List<CapacityHourFeedback> capacityHourFeedbackList = convertCapacityHourFeedBacks(transitId);

        for (int hour = 0; hour < hoursInDay; hour++) {
            capacityMap.put(hour, getAverageCapacityByHour(hour, capacityHourFeedbackList));
        }
        return capacityMap;
    }

    /**
     * Method to return map for HeatMap diagram on UI.
     *
     * @param transitId id of specified transit
     */
    @Override
    public HeatMapDTO getHeatMap(Integer transitId, Stop... stops) {
        Map<Stop, Double> stopCapacityMap = getStopCapacityMap(transitId, Stop.Direction.FORWARD, stops);
        Map<Integer, Double> hourCapacityMap = getHourCapacityMap(transitId);

        return new HeatMapDTO(stopCapacityMap, hourCapacityMap);
    }

    /**
     * Method to divide divided by divider with avoiding dividing by zero.
     */
    private double safeDivision(Number divided, double divider) {
        if (divider == 0) {
            return 0;
        }
        return divided.doubleValue() / divider;
    }

    private Double getAverageCapacityByHour(Integer feedbackHour, List<CapacityHourFeedback> capacityHourFeedbackList) {

        return capacityHourFeedbackList.stream()
            .filter(capacityHourFeedback -> capacityHourFeedback.containsHour(feedbackHour))
            .mapToInt(CapacityHourFeedback::getCapacity)
            .average()
            .orElse(0.0);
    }

    private Double getAverageRate(List<Feedback> feedbackList) {

        return feedbackList.stream()
            .mapToDouble(new RatingConverter()::convert)
            .average()
            .orElse(0.0);
    }

    private List<CapacityHourFeedback> convertCapacityHourFeedBacks(Integer transitId) {

        return getByTransitIdAndFeedbackCriteriaType(transitId, FeedbackCriteria.FeedbackType.HOURS_CAPACITY).stream()
            .map(feedback -> new FeedbackTypeConverter<>(CapacityHourFeedback.class).convert(feedback))
            .collect(Collectors.toList());
    }

    private boolean existInStopIndexesRange(Integer stopIndex, Integer fromStopIndex, Integer toStopIndex) {

        return (fromStopIndex < toStopIndex) ? Range.closed(fromStopIndex, toStopIndex).contains(stopIndex)
            : Range.closed(toStopIndex, fromStopIndex).contains(stopIndex);
    }

    private Double getCapacityByTransitAndStops(Stop stop, List<CapacityRouteFeedback> capacityRouteFeedbackList,
                                                List<Stop> stopList) {
        Integer stopIndex = stopList.indexOf(stop);

        return capacityRouteFeedbackList.stream()
            .filter(feedback -> existInStopIndexesRange(stopIndex, stopList.indexOf(feedback.getFrom()),
                stopList.indexOf(feedback.getTo())))
            .mapToInt(CapacityRouteFeedback::getCapacity).average().orElse(0.0);
    }

    private List<CapacityRouteFeedback> convertCapacityRouteFeedBacks(Integer transitId) {

        return getByTransitIdAndFeedbackCriteriaType(transitId, FeedbackCriteria.FeedbackType.ROUTE_CAPACITY).stream()
            .map(feedback -> new FeedbackTypeConverter<>(CapacityRouteFeedback.class).convert(feedback))
            .collect(Collectors.toList());
    }

    private Long countByValue(SimpleFeedback answer, Integer transitId) {

        return convertSimpleFeedBacks(transitId)
            .stream()
            .filter(simpleFeedback -> simpleFeedback == answer)
            .count();
    }

    private List<SimpleFeedback> convertSimpleFeedBacks(Integer transitId) {

        return getByTransitIdAndFeedbackCriteriaType(transitId, FeedbackCriteria.FeedbackType.SIMPLE).stream()
            .map(feedback -> new FeedbackTypeConverter<>(SimpleFeedback.class).convert(feedback))
            .collect(Collectors.toList());
    }

    private Integer countAllAccepterFeedBacks(Integer transitId) {
        return convertSimpleFeedBacks(transitId).size();
    }

    private Double getAverageQualityRate(List<Feedback> feedbackList) {

        return feedbackList.stream()
            .map(feedback -> new FeedbackTypeConverter<>(QuantityLoadFeedback.class).convert(feedback))
            .mapToInt(QuantityLoadFeedback::getRate)
            .average()
            .orElse(0.0);
    }

    private Double getAverageConflictFeedBacksRate(List<Feedback> feedbackList) {

        return feedbackList.stream().mapToDouble(new ConflictTypeConverter()::convert)
            .average()
            .orElse(0.0);
    }

}
