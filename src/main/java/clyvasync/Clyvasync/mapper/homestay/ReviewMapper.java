package clyvasync.Clyvasync.mapper.homestay;

import clyvasync.Clyvasync.dto.response.ReviewResponse;
import clyvasync.Clyvasync.modules.homestay.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewResponse toReviewResponse(Review review);

    List<ReviewResponse> toReviewResponseList(List<Review> reviews);
}