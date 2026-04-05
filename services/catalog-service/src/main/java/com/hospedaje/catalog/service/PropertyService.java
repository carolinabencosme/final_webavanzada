package com.hospedaje.catalog.service;

import com.hospedaje.catalog.client.ReservationAvailabilityClient;
import com.hospedaje.catalog.document.Property;
import com.hospedaje.catalog.dto.PageResponse;
import com.hospedaje.catalog.dto.PropertyDto;
import com.hospedaje.catalog.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final MongoTemplate mongoTemplate;
    private final ReservationAvailabilityClient reservationAvailabilityClient;

    public PageResponse<PropertyDto> listProperties(
        int page,
        int size,
        String city,
        String propertyType,
        String roomType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String q,
        LocalDate checkIn,
        LocalDate checkOut
    ) {
        Query query = new Query();
        List<Criteria> ands = new ArrayList<>();

        if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
            try {
                List<String> occupied = reservationAvailabilityClient.getOccupiedPropertyIds(checkIn, checkOut);
                if (occupied != null && !occupied.isEmpty()) {
                    ands.add(Criteria.where("_id").nin(occupied));
                }
            } catch (Exception e) {
                log.warn("No se pudo filtrar por disponibilidad (reservation-service): {}", e.getMessage());
            }
        }
        if (StringUtils.hasText(city)) {
            ands.add(Criteria.where("city").regex(safeRegex(city), "i"));
        }
        if (StringUtils.hasText(propertyType)) {
            ands.add(Criteria.where("propertyType").regex(safeRegex(propertyType), "i"));
        }
        if (StringUtils.hasText(roomType)) {
            ands.add(Criteria.where("roomType").regex(safeRegex(roomType), "i"));
        }
        if (minPrice != null) {
            ands.add(Criteria.where("pricePerNight").gte(minPrice));
        }
        if (maxPrice != null) {
            ands.add(Criteria.where("pricePerNight").lte(maxPrice));
        }
        if (StringUtils.hasText(q)) {
            String rx = safeRegex(q);
            ands.add(new Criteria().orOperator(
                Criteria.where("name").regex(rx, "i"),
                Criteria.where("city").regex(rx, "i"),
                Criteria.where("description").regex(rx, "i")
            ));
        }
        if (!ands.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(ands.toArray(new Criteria[0])));
        }
        long total = mongoTemplate.count(query, Property.class);
        query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        List<Property> rows = mongoTemplate.find(query, Property.class);
        return toPageResponse(rows, page, size, total);
    }

    public PropertyDto getById(String id) {
        Property p = propertyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Property not found: " + id));
        return toDto(p);
    }

    public List<PropertyDto> getByIds(List<String> ids) {
        return propertyRepository.findAllById(ids).stream().map(this::toDto).collect(Collectors.toList());
    }

    public void updatePropertyRating(String propertyId, double avgRating, int totalReviews) {
        propertyRepository.findById(propertyId).ifPresent(p -> {
            p.setAverageRating(avgRating);
            p.setTotalReviews(totalReviews);
            propertyRepository.save(p);
        });
    }

    private PageResponse<PropertyDto> toPageResponse(List<Property> rows, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / (double) size);
        return PageResponse.<PropertyDto>builder()
            .content(rows.stream().map(this::toDto).collect(Collectors.toList()))
            .page(page)
            .size(size)
            .totalElements(total)
            .totalPages(totalPages)
            .build();
    }

    private PropertyDto toDto(Property p) {
        return PropertyDto.builder()
            .id(p.getId())
            .name(p.getName())
            .description(p.getDescription())
            .city(p.getCity())
            .country(p.getCountry())
            .address(p.getAddress())
            .propertyType(p.getPropertyType())
            .roomType(p.getRoomType())
            .amenities(p.getAmenities())
            .imageUrl(p.getImageUrl())
            .images(p.getImages())
            .pricePerNight(p.getPricePerNight())
            .maxGuests(p.getMaxGuests())
            .averageRating(p.getAverageRating())
            .totalReviews(p.getTotalReviews())
            .build();
    }

    /** Avoid ReDoS: treat input as literal fragment */
    private static String safeRegex(String raw) {
        return Pattern.quote(raw.trim());
    }
}
