package ecommerce.platform.ranking.controller;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.ranking.dto.Period;
import ecommerce.platform.ranking.dto.RankingDto;
import ecommerce.platform.ranking.service.RankingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingQueryService rankingQueryService;

    @GetMapping(value = "/api/v1/products/rankings")
    public ResponseEntity<Set<RankingDto>> getRankingChart(@RequestParam("category") String categoryName, @RequestParam("period") String periodName) {
        Category category = Category.valueOf(categoryName);
        Period period = Period.valueOf(periodName);
        Set<RankingDto> responseDtos = rankingQueryService.getRanking(category, period);
        return ResponseEntity.ok(responseDtos);
    }
}
