-- User 데이터 삽입
INSERT INTO users (user_id, name, provider, role, email, profile_url)
VALUES (1, 'koko', 0, 'USER', '1234@naver.com', 'https://www.naver.com');

-- Product 데이터 삽입
INSERT INTO scrap (user_id, scrap_id, page_url, title, description, site_name, d_type)
VALUES (1, 1, 'https://www.coupang.com', 'Coupang의 맥북 상품', 'MacBook Pro 16인치 2019년형', 'Coupang', 'Product');