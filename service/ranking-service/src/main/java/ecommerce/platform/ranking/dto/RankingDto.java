package ecommerce.platform.ranking.dto;

public record RankingDto (int rank, Long productId, String productName, Long imageId){
    public static RankingDto of(int rank, RankingEntry rankingEntry) {
        RankingDto rankingDto = new RankingDto(rank, rankingEntry.productId(), rankingEntry.productName(), rankingEntry.imageId());
        return rankingDto;
    }
}
