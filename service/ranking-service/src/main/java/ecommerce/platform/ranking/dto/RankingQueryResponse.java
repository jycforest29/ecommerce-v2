package ecommerce.platform.ranking.dto;

public record RankingQueryResponse(int rank, Long productId, String productName, Long imageId) {
    public static RankingQueryResponse of(int rank, RankingEntry rankingEntry) {
        return new RankingQueryResponse(rank, rankingEntry.productId(), rankingEntry.productName(), rankingEntry.imageId());
    }
}
