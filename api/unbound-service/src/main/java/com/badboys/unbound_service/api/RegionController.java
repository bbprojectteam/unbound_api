package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.RegionService;
import com.badboys.unbound_service.model.Region;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/region")
public class RegionController {

    private final RegionService regionService;

    @Autowired
    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @Operation(summary = "지역 목록 조회", description = "모든 지역 가져오기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Region.class)))),
            @ApiResponse(responseCode = "500", description = "서버에러", content = @Content)
    })
    @GetMapping("/list")
    public ResponseEntity<List<Region>> getAllRegionList() {
        List<Region> allRegionList = regionService.getAllRegions();
        return ResponseEntity.ok(allRegionList);
    }

}
