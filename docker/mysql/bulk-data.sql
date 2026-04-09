-- =============================================
-- Ecommerce Platform Bulk Data
-- Member 200, Product 1000, Promotion 3
-- =============================================

-- =============================================
-- 1. MEMBERS (user_db) - 200명
-- =============================================
USE user_db;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM member;
SET FOREIGN_KEY_CHECKS = 1;

DELIMITER //
CREATE PROCEDURE generate_members()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 200 DO
        INSERT INTO member (user_name, password, email, phone_number, member_status, joined_at)
        VALUES (
            CONCAT('user', LPAD(i, 3, '0')),
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
            CONCAT('user', LPAD(i, 3, '0'), '@test.com'),
            CONCAT('010-1000-', LPAD(i, 4, '0')),
            'ACTIVE',
            DATE_SUB(NOW(), INTERVAL (200 - i) DAY)
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_members();
DROP PROCEDURE generate_members;


-- =============================================
-- 2. PRODUCTS (product_db) - 1000개
--    카테고리(9) x 브랜드(14) x 색상(11) x 사이즈 균등 분포
-- =============================================
USE product_db;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM product_option;
DELETE FROM product;
SET FOREIGN_KEY_CHECKS = 1;

DELIMITER //
CREATE PROCEDURE generate_products()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_category VARCHAR(20);
    DECLARE v_brand VARCHAR(5);
    DECLARE v_color VARCHAR(10);
    DECLARE v_clothing_size VARCHAR(5);
    DECLARE v_shoe_size VARCHAR(10);
    DECLARE v_cat_idx INT;
    DECLARE v_brand_idx INT;
    DECLARE v_color_idx INT;
    DECLARE v_size_idx INT;
    DECLARE v_base_price INT;
    DECLARE v_price INT;
    DECLARE v_product_id BIGINT;

    WHILE i < 1000 DO
        SET v_cat_idx   = (i % 9) + 1;
        SET v_brand_idx = (i % 14) + 1;
        SET v_color_idx = (i % 11) + 1;

        SET v_category = ELT(v_cat_idx,
            'OUTER','KNIT','SHIRTS','PANTS','SKIRT','SHOES','ACCESSORY','COSMETICS','BAGS');
        SET v_brand = ELT(v_brand_idx,
            'A','B','C','D','E','F','G','H','K','I','L','M','N','O');
        SET v_color = ELT(v_color_idx,
            'BLACK','WHITE','RED','PINK','ORANGE','YELLOW','GREEN','PURPLE','BROWN','BLUE','NAVY');

        SET v_base_price = ELT(v_cat_idx,
            120000, 55000, 45000, 55000, 48000, 89000, 25000, 35000, 95000);
        SET v_price = v_base_price + ((i * 1777) % 30000) - 15000;

        INSERT INTO product (seller_id, name, image_id, brand, category, price, description, review_count, created_at, modified_at, deleted_at, in_sale)
        VALUES (
            (i % 200) + 1,
            CONCAT(v_brand, '_', v_category, '_', v_color, '_', LPAD(i + 1, 4, '0')),
            NULL,
            v_brand,
            v_category,
            v_price,
            CONCAT(v_brand, ' brand ', v_category, ' product'),
            0,
            DATE_SUB(NOW(), INTERVAL (i % 180) DAY),
            NULL,
            NULL,
            1
        );

        SET v_product_id = LAST_INSERT_ID();
        SET v_clothing_size = NULL;
        SET v_shoe_size = NULL;

        IF v_category = 'SHOES' THEN
            SET v_size_idx = (i % 6) + 1;
            SET v_shoe_size = ELT(v_size_idx,
                'SIZE_230','SIZE_240','SIZE_250','SIZE_260','SIZE_270','SIZE_280');
        ELSE
            SET v_size_idx = (i % 5) + 1;
            SET v_clothing_size = ELT(v_size_idx, 'XS','S','M','L','XL');
        END IF;

        INSERT INTO product_option (product_id, clothing_size, shoe_size, color, additional_price, stock, available)
        VALUES (v_product_id, v_clothing_size, v_shoe_size, v_color, 0, 50 + (i % 51), 1);

        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_products();
DROP PROCEDURE generate_products;


-- =============================================
-- 3. PROMOTIONS (coupon_db) - 3개
-- =============================================
USE coupon_db;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM coupon_log;
DELETE FROM coupon;
DELETE FROM promotion;
SET FOREIGN_KEY_CHECKS = 1;

-- 1) 회원가입 쿠폰: 모든 브랜드/카테고리, 10% 고정 할인
INSERT INTO promotion (
    promotion_name, quantity, expire_days,
    discount_rate, random_discount, min_discount_rate, max_discount_rate,
    min_purchase_amount, max_discount_amount,
    started_at, ended_at, category, brand
) VALUES (
    'ALL::ALL::WELCOME', 10000, 365,
    10, false, 0, 0,
    10000, 10000,
    DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_ADD(NOW(), INTERVAL 365 DAY),
    'ALL', 'ALL'
);

-- 2) 랜덤 쿠폰: 모든 브랜드/카테고리, 랜덤 할인율
INSERT INTO promotion (
    promotion_name, quantity, expire_days,
    discount_rate, random_discount, min_discount_rate, max_discount_rate,
    min_purchase_amount, max_discount_amount,
    started_at, ended_at, category, brand
) VALUES (
    'ALL::ALL::RANDOM', 10000, 30,
    0, true, 5, 30,
    40000, 10000,
    NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY),
    'ALL', 'ALL'
);

-- 3) A 브랜드 SHOES 전용 20% 할인 쿠폰
INSERT INTO promotion (
    promotion_name, quantity, expire_days,
    discount_rate, random_discount, min_discount_rate, max_discount_rate,
    min_purchase_amount, max_discount_amount,
    started_at, ended_at, category, brand
) VALUES (
    'A::SHOES::20_PERCENT', 5000, 60,
    20, false, 0, 0,
    100000, 20000,
    NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY),
    'SHOES', 'A'
);
