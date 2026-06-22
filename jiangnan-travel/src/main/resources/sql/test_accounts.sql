INSERT IGNORE INTO t_user (phone, password, nickname, status) VALUES ('13810000001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','南昌张师傅',1);
INSERT IGNORE INTO t_user (phone, password, nickname, status) VALUES ('13810000002','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','赣州李师傅',1);
INSERT IGNORE INTO t_user (phone, password, nickname, status) VALUES ('13810000003','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','宁都王师傅',1);
INSERT IGNORE INTO t_user (phone, password, nickname, status) VALUES ('13920000001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','南昌旅客',1);
INSERT IGNORE INTO t_user (phone, password, nickname, status) VALUES ('13920000002','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','赣州游客',1);
INSERT IGNORE INTO t_user (phone, password, nickname, status) VALUES ('13920000003','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','宁都通勤族',1);

INSERT INTO t_driver (user_id, real_name, id_card, driver_license, car_plate, car_type_id, status, verify_status, avg_rating, lat, lng)
SELECT id, '张师傅', '360100199001011234', '360100199001011234', '赣A12345', 1, 1, 1, 4.80, 28.6842, 115.8759
FROM t_user WHERE phone='13810000001';

INSERT INTO t_driver (user_id, real_name, id_card, driver_license, car_plate, car_type_id, status, verify_status, avg_rating, lat, lng)
SELECT id, '李师傅', '360700199002022345', '360700199002022345', '赣B67890', 2, 1, 1, 4.90, 25.8456, 114.9356
FROM t_user WHERE phone='13810000002';

INSERT INTO t_driver (user_id, real_name, id_card, driver_license, car_plate, car_type_id, status, verify_status, avg_rating, lat, lng)
SELECT id, '王师傅', '360800199003033456', '360800199003033456', '赣C11111', 3, 1, 1, 4.70, 26.4825, 116.0209
FROM t_user WHERE phone='13810000003';
