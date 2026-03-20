package ecommerce.platform.ranking.controller;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.ranking.dto.Period;
import ecommerce.platform.ranking.dto.RankingQueryResponse;
import ecommerce.platform.ranking.service.RankingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingQueryService rankingQueryService;

    @GetMapping(value = "/api/v1/products/rankings")
    public ResponseEntity<List<RankingQueryResponse>> getRankingChart(@RequestParam("category") Category category, @RequestParam("period") Period period) {
        List<RankingQueryResponse> responses = rankingQueryService.getRanking(category, period);
        return ResponseEntity.ok(responses);
    }
}
