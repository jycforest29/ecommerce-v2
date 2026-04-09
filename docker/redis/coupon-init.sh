#!/bin/bash
# 쿠폰 발급 테스트용 Redis 재고 초기화
# promotion_id 1: 고정 할인 100장
# promotion_id 2: 랜덤 할인 50장
# promotion_id 3: 플래시 세일 10장

REDIS_HOST=${1:-localhost}
REDIS_PORT=${2:-6379}

echo "=== Redis 쿠폰 재고 초기화 ==="

# 고정 할인: DECR 방식 재고
redis-cli -h $REDIS_HOST -p $REDIS_PORT SET "ALL::ALL::FIXED_10_PERCENT" 100
echo "[OK] FIXED_10_PERCENT: 100장"

# 플래시 세일: DECR 방식 재고
redis-cli -h $REDIS_HOST -p $REDIS_PORT SET "ALL::SHOES::FLASH_SALE" 10
echo "[OK] FLASH_SALE: 10장"

# 랜덤 할인: RPUSH 방식 (할인율 리스트)
redis-cli -h $REDIS_HOST -p $REDIS_PORT DEL "ALL::OUTER::RANDOM_WINTER::random"
for i in $(seq 1 50); do
    rate=$((RANDOM % 26 + 5))  # 5~30
    redis-cli -h $REDIS_HOST -p $REDIS_PORT RPUSH "ALL::OUTER::RANDOM_WINTER::random" $rate > /dev/null
done
echo "[OK] RANDOM_WINTER: 50장 (랜덤 할인율 적재)"

echo ""
echo "=== 재고 확인 ==="
echo "FIXED_10_PERCENT: $(redis-cli -h $REDIS_HOST -p $REDIS_PORT GET 'ALL::ALL::FIXED_10_PERCENT')"
echo "FLASH_SALE: $(redis-cli -h $REDIS_HOST -p $REDIS_PORT GET 'ALL::SHOES::FLASH_SALE')"
echo "RANDOM_WINTER(list len): $(redis-cli -h $REDIS_HOST -p $REDIS_PORT LLEN 'ALL::OUTER::RANDOM_WINTER::random')"
echo ""
echo "=== 초기화 완료 ==="
