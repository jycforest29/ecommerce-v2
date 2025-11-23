package ecommerce.platform.ranking.dto;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.ranking.service.RankingQueryService;

import java.util.Set;
import java.util.function.BiFunction;

public enum Period {
    REALTIME(10),
    DAILY(20),
    WEEKLY(30),
    MONEHLY(40);

    private int increment;
    private final BiFunction<RankingQueryService, Category, Set<RankingDto>> queryViewer = (rankingQueryService, category) -> rankingQueryService.getRanking(category, this);

    Period(int increment) {
        this.increment = increment;
    }

    public int getIncrement() {
        return increment;
    }

    public Set<RankingDto> getRanking(RankingQueryService rankingQueryService, Category category) {
        return queryViewer.apply(rankingQueryService, category);
    }
}
