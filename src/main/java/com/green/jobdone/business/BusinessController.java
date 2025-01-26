package com.green.jobdone.business;

import com.green.jobdone.business.model.BusinessLogoPatchReq;
import com.green.jobdone.business.model.BusinessStatePutReq;
import com.green.jobdone.business.model.get.BusinessGetOneReq;
import com.green.jobdone.business.model.get.BusinessGetOneRes;
import com.green.jobdone.business.model.get.BusinessGetReq;
import com.green.jobdone.business.model.get.BusinessGetRes;
import com.green.jobdone.business.phone.BusinessPhonePostReq;
import com.green.jobdone.business.pic.BusinessPicPostRes;
import com.green.jobdone.business.model.BusinessPostSignUpReq;
import com.green.jobdone.business.model.BusinessDetailPutReq;
import com.green.jobdone.common.model.ResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("business")
@RestController
@Slf4j
@Tag(name = "업체")
public class BusinessController {
    private final BusinessService businessService;

    @PostMapping("sign-up")
    @Operation(summary = "업체 등록")
    public ResultResponse<Integer> postBusiness(@RequestPart(required = false) MultipartFile paper,
                                                @Valid @RequestPart BusinessPostSignUpReq p) {
        int result = businessService.insBusiness(paper, p);

        return ResultResponse.<Integer>builder()
                .resultMessage("업체 등록 완료")
                .resultData(result)
                .build();
    }

    @PutMapping("detail")
    @Operation(summary = "업체 상세정보 기입")
    public ResultResponse<Integer> udtBusinessDetail(@RequestPart BusinessDetailPutReq p) {
        int result = businessService.udtBusiness(p);
        return ResultResponse.<Integer>builder()
                .resultData(result)
                .resultMessage(result == 0 ? "업체 정보 수정 실패" : "업체 정보 수정 성공")
                .build();
    }

    @PatchMapping("logo")
    @Operation(summary = "업체 로고사진 변경")
    public ResultResponse<Integer> patchProfilePic(@RequestPart BusinessLogoPatchReq p, @RequestPart(required = false) MultipartFile logo) {
        log.info("UserController > patchProfilePic > p: {}", p);

        int result = businessService.patchBusinessLogo(p, logo);

        return ResultResponse.<Integer>builder()
                .resultMessage("로고 사진 수정 완료")
                .resultData(result)
                .build();
    }


    @PostMapping("phone")
    @Operation(summary = "업체 전화번호 기입")
    public ResultResponse<Integer> postBusinessPhone(BusinessPhonePostReq p) {
        int result = businessService.insBusinessPhone(p);
        return ResultResponse.<Integer>builder()
                .resultMessage("전화번호 추가 완료")
                .resultData(result)
                .build();
    }

    @PostMapping("businessPic")
    @Operation(summary = "업체 사진 등록")
    public ResultResponse<BusinessPicPostRes> postBusinessPic(@RequestPart List<MultipartFile> pics,
                                                              @RequestPart long businessId) {
        BusinessPicPostRes res = businessService.insBusinessPic(pics, businessId);
        return ResultResponse.<BusinessPicPostRes>builder()
                .resultMessage("업체사진등록 완료")
                .resultData(res)
                .build();
    }

    @PutMapping("pic")
    @Operation(summary = "사진 유형 수정")
    public ResultResponse<Integer> putBusinessPic(long businessPicId) {
        int res = businessService.udtBusinessPics(businessPicId);

        return ResultResponse.<Integer>builder()
                .resultMessage("업체 사진 수정 완료")
                .resultData(res)
                .build();
    }

    @PutMapping("state")
    @Operation(summary = "업체 유형 수정")
    public ResultResponse<Integer> putBusinessState(BusinessStatePutReq p) {
        int res = businessService.udtBusinessState(p);

        return ResultResponse.<Integer>builder()
                .resultMessage("업체 유형 수정 완료")
                .resultData(res)
                .build();
    }

    @GetMapping("/{businessId}")
    @Operation(summary = "한 업체 조회")
    public ResultResponse<BusinessGetOneRes> selBusiness(@PathVariable Long businessId) {
        BusinessGetOneReq req = new BusinessGetOneReq(businessId);
        BusinessGetOneRes res = businessService.getBusinessOne(req);

        return ResultResponse.<BusinessGetOneRes>builder().resultData(res).resultMessage("한 업체 조회완료").build();

    }

    @GetMapping
    @Operation(summary = "여러 업체 조회")
    public ResultResponse<List<BusinessGetRes>> selBusinessList(BusinessGetReq p) {
        List<BusinessGetRes> res = businessService.getBusinessList(p);
        return ResultResponse.<List<BusinessGetRes>>builder()
                .resultData(res).resultMessage("업체 리스트 조회 완료")
                .build();
    }
}














