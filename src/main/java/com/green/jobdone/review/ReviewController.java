package com.green.jobdone.review;

import com.green.jobdone.common.model.ResultResponse;
import com.green.jobdone.review.model.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "리뷰 등록", description = "필수: 사진리스트 || 옵션: 별점, 내용")
    public ResultResponse<ReviewPostRes> postFeed(@RequestPart List<MultipartFile> pics
            , @Valid @RequestPart ReviewPostReq p) {
        ReviewPostRes res = reviewService.postReview(pics, p);
        return ResultResponse.<ReviewPostRes>builder()
                .resultMessage("리뷰 등록 완료")
                .resultData(res)
                .build();
    }

}
