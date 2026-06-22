-- =====================================================
-- 江南出行智慧服务平台 - 数据库初始化脚本
-- 数据库: smart_travel
-- 版本: 1.0.0
-- 日期: 2026-07-06
-- =====================================================

CREATE DATABASE IF NOT EXISTS smart_travel
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE smart_travel;

-- =====================================================
-- 一、核心业务表（12张）
-- =====================================================

-- 1. 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '用户ID',
    phone           VARCHAR(11)     NOT NULL UNIQUE             COMMENT '手机号',
    password        VARCHAR(128)                                COMMENT 'BCrypt加密密码',
    nickname        VARCHAR(50)     DEFAULT ''                  COMMENT '昵称',
    avatar          VARCHAR(255)    DEFAULT ''                  COMMENT '头像URL',
    status          TINYINT         DEFAULT 1                   COMMENT '0禁用 1正常 2风控冻结',
    preferred_driver_ids VARCHAR(255) DEFAULT ''                COMMENT '优选司机ID JSON数组',
    last_login_time DATETIME                                    COMMENT '最后登录时间',
    deleted         TINYINT         DEFAULT 0                   COMMENT '逻辑删除 0未删 1已删',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '注册时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 车型表
DROP TABLE IF EXISTS t_car_type;
CREATE TABLE t_car_type (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '车型ID',
    name                VARCHAR(20)     NOT NULL                    COMMENT '快车/专车/商务七座',
    base_price          DECIMAL(8,2)    NOT NULL                    COMMENT '起步价(含3公里)',
    mid_per_km          DECIMAL(6,2)    NOT NULL                    COMMENT '3-50公里单价',
    long_per_km         DECIMAL(6,2)    NOT NULL                    COMMENT '50-200公里单价',
    super_long_per_km   DECIMAL(6,2)    NOT NULL                    COMMENT '200公里以上单价',
    per_min_price       DECIMAL(6,2)    NOT NULL DEFAULT 0.00       COMMENT '每分钟时长费',
    max_passengers      TINYINT         DEFAULT 4                   COMMENT '最大载客数',
    status              TINYINT         DEFAULT 1                   COMMENT '0禁用 1启用',
    deleted             TINYINT         DEFAULT 0                   COMMENT '逻辑删除',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车型表';

-- 3. 司机表
DROP TABLE IF EXISTS t_driver;
CREATE TABLE t_driver (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '司机ID',
    user_id         BIGINT                                      COMMENT '账号ID（关联t_user，多角色场景）',
    real_name       VARCHAR(20)     NOT NULL                    COMMENT '真实姓名',
    id_card         VARCHAR(18)                                 COMMENT '身份证号',
    driver_license  VARCHAR(30)                                 COMMENT '驾驶证号',
    car_plate       VARCHAR(10)                                 COMMENT '车牌号',
    car_type_id     BIGINT                                      COMMENT '车型',
    status          TINYINT         DEFAULT 0                   COMMENT '0离线 1在线 2忙碌 3休息中',
    lat             DECIMAL(10,7)   DEFAULT 0                   COMMENT '实时纬度',
    lng             DECIMAL(10,7)   DEFAULT 0                   COMMENT '实时经度',
    avg_rating      DECIMAL(3,2)    DEFAULT 4.00                COMMENT '平均评分',
    total_orders    INT             DEFAULT 0                   COMMENT '累计完成单数',
    verify_status   TINYINT         DEFAULT 0                   COMMENT '0待审核 1通过 2拒绝',
    online_duration INT             DEFAULT 0                   COMMENT '今日在线时长(分钟)',
    deleted         TINYINT         DEFAULT 0                   COMMENT '逻辑删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '注册时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_car_type (car_type_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机表';

-- 4. 订单表
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '订单ID',
    order_no            VARCHAR(32)     NOT NULL UNIQUE             COMMENT '订单编号 JN+时间戳+随机',
    user_id             BIGINT          NOT NULL                    COMMENT '乘客ID',
    driver_id           BIGINT                                      COMMENT '司机ID（接单后填充）',
    car_type_id         BIGINT                                      COMMENT '车型',
    start_address       VARCHAR(255)    NOT NULL                    COMMENT '起点地址',
    start_lat           DECIMAL(10,7)   DEFAULT 0                   COMMENT '起点纬度',
    start_lng           DECIMAL(10,7)   DEFAULT 0                   COMMENT '起点经度',
    end_address         VARCHAR(255)    NOT NULL                    COMMENT '终点地址',
    end_lat             DECIMAL(10,7)   DEFAULT 0                   COMMENT '终点纬度',
    end_lng             DECIMAL(10,7)   DEFAULT 0                   COMMENT '终点经度',
    distance            INT             DEFAULT 0                   COMMENT '预估距离(米)',
    duration            INT             DEFAULT 0                   COMMENT '预估时长(秒)',
    base_price          DECIMAL(10,2)   DEFAULT 0.00                COMMENT '基础价格',
    surge_factor        DECIMAL(4,2)    DEFAULT 1.00                COMMENT '动态加价系数(上限1.5)',
    coupon_discount     DECIMAL(10,2)   DEFAULT 0.00                COMMENT '优惠券抵扣',
    final_price         DECIMAL(10,2)   DEFAULT 0.00                COMMENT '最终实付',
    toll_fee            DECIMAL(8,2)    DEFAULT 0.00                COMMENT '高速过路费',
    status              TINYINT         DEFAULT 0                   COMMENT '0待接单 1已接单 2已到达 3行程中 4已完成 5已取消 6风控拦截',
    cancel_reason       VARCHAR(255)    DEFAULT ''                  COMMENT '取消原因',
    is_safety_share     TINYINT         DEFAULT 0                   COMMENT '是否已分享行程',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '下单时间',
    accept_time         DATETIME                                    COMMENT '接单时间',
    arrive_time         DATETIME                                    COMMENT '司机到达时间',
    start_time          DATETIME                                    COMMENT '行程开始时间',
    end_time            DATETIME                                    COMMENT '行程结束时间',
    cancel_time         DATETIME                                    COMMENT '取消时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id),
    INDEX idx_driver (driver_id),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 5. 订单操作日志
DROP TABLE IF EXISTS t_order_track;
CREATE TABLE t_order_track (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '日志ID',
    order_id        BIGINT          NOT NULL                    COMMENT '订单ID',
    action          VARCHAR(50)     NOT NULL                    COMMENT '操作动作',
    operator_id     BIGINT          NOT NULL                    COMMENT '操作人ID',
    operator_type   VARCHAR(20)     NOT NULL                    COMMENT 'user/driver/system',
    remark          VARCHAR(255)    DEFAULT ''                  COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '操作时间',
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志';

-- 6. 收藏地址
DROP TABLE IF EXISTS t_user_address;
CREATE TABLE t_user_address (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '地址ID',
    user_id     BIGINT          NOT NULL                    COMMENT '用户ID',
    tag         VARCHAR(50)     NOT NULL                    COMMENT '地址标签（家/公司/自定义）',
    address     VARCHAR(255)    NOT NULL                    COMMENT '详细地址',
    lat         DECIMAL(10,7)   DEFAULT 0                   COMMENT '纬度',
    lng         DECIMAL(10,7)   DEFAULT 0                   COMMENT '经度',
    deleted     TINYINT         DEFAULT 0                   COMMENT '逻辑删除',
    create_time DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏地址';

-- 7. 优惠券模板
DROP TABLE IF EXISTS t_coupon;
CREATE TABLE t_coupon (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '优惠券ID',
    name            VARCHAR(50)     NOT NULL                    COMMENT '优惠券名称',
    threshold       DECIMAL(10,2)   NOT NULL                    COMMENT '满减门槛',
    discount        DECIMAL(10,2)   NOT NULL                    COMMENT '优惠金额',
    valid_days      INT             NOT NULL DEFAULT 30         COMMENT '有效天数',
    status          TINYINT         DEFAULT 1                   COMMENT '0禁用 1启用',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板';

-- 8. 用户优惠券
DROP TABLE IF EXISTS t_user_coupon;
CREATE TABLE t_user_coupon (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '记录ID',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    coupon_id       BIGINT          NOT NULL                    COMMENT '优惠券ID',
    status          TINYINT         DEFAULT 0                   COMMENT '0未使用 1已使用 2已过期',
    expire_time     DATETIME        NOT NULL                    COMMENT '过期时间',
    use_order_id    BIGINT                                      COMMENT '使用订单ID',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '领取时间',
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

-- 9. 管理员
DROP TABLE IF EXISTS t_admin;
CREATE TABLE t_admin (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '管理员ID',
    username    VARCHAR(50)     NOT NULL UNIQUE             COMMENT '用户名',
    password    VARCHAR(128)    NOT NULL                    COMMENT 'BCrypt加密密码',
    real_name   VARCHAR(20)                                 COMMENT '真实姓名',
    role        VARCHAR(20)     DEFAULT 'admin'             COMMENT 'admin/super_admin',
    status      TINYINT         DEFAULT 1                   COMMENT '0禁用 1启用',
    create_time DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 10. 评价表
DROP TABLE IF EXISTS t_review;
CREATE TABLE t_review (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '评价ID',
    order_id    BIGINT          NOT NULL UNIQUE             COMMENT '订单ID（一个订单一条评价）',
    user_id     BIGINT          NOT NULL                    COMMENT '评价人ID',
    driver_id   BIGINT          NOT NULL                    COMMENT '被评价司机ID',
    rating      TINYINT         NOT NULL                    COMMENT '评分 1-5',
    tags        VARCHAR(255)    DEFAULT ''                  COMMENT '评价标签 逗号分隔',
    content     VARCHAR(500)    DEFAULT ''                  COMMENT '评价内容',
    create_time DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '评价时间',
    INDEX idx_driver (driver_id),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 11. 班线路线
DROP TABLE IF EXISTS t_schedule_route;
CREATE TABLE t_schedule_route (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '路线ID',
    start_city      VARCHAR(50)     NOT NULL                    COMMENT '出发城市',
    end_city        VARCHAR(50)     NOT NULL                    COMMENT '目的城市',
    depart_time     VARCHAR(20)     NOT NULL                    COMMENT '发车时间 HH:mm',
    price           DECIMAL(8,2)    NOT NULL                    COMMENT '票价',
    total_seats     INT             DEFAULT 30                  COMMENT '总座位数',
    status          TINYINT         DEFAULT 1                   COMMENT '0禁用 1启用',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班线路线（MVP仅建表）';

-- 12. 班线订单
DROP TABLE IF EXISTS t_schedule_order;
CREATE TABLE t_schedule_order (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '订单ID',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    route_id        BIGINT          NOT NULL                    COMMENT '路线ID',
    depart_date     DATE            NOT NULL                    COMMENT '出行日期',
    seat_count      TINYINT         DEFAULT 1                   COMMENT '购票数量',
    total_price     DECIMAL(8,2)    NOT NULL                    COMMENT '总价',
    status          TINYINT         DEFAULT 0                   COMMENT '0待支付 1已支付 2已取消',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_route (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班线订单（MVP仅建表）';

-- =====================================================
-- 二、AI 相关表（4张）
-- =====================================================

-- 13. AI客服对话日志
DROP TABLE IF EXISTS t_ai_chat_log;
CREATE TABLE t_ai_chat_log (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '日志ID',
    user_id         BIGINT                                      COMMENT '用户ID（未登录可为NULL）',
    session_id      VARCHAR(64)     NOT NULL                    COMMENT '会话ID',
    role            VARCHAR(10)     NOT NULL                    COMMENT 'user/assistant',
    content         TEXT            NOT NULL                    COMMENT '消息内容',
    tokens_used     INT             DEFAULT 0                   COMMENT '消耗Token数',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客服对话日志';

-- 14. 风控告警记录
DROP TABLE IF EXISTS t_risk_alert;
CREATE TABLE t_risk_alert (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '告警ID',
    rule_code       VARCHAR(20)     NOT NULL                    COMMENT '规则编码 R1/R2/R3...',
    user_id         BIGINT                                      COMMENT '目标用户ID',
    order_id        BIGINT                                      COMMENT '关联订单ID',
    alert_level     TINYINT         NOT NULL DEFAULT 1          COMMENT '1提示 2警告 3严重',
    title           VARCHAR(100)    NOT NULL                    COMMENT '告警标题',
    detail          TEXT                                        COMMENT '告警详情JSON',
    handled         TINYINT         DEFAULT 0                   COMMENT '0未处理 1已处理',
    handle_remark   VARCHAR(255)    DEFAULT ''                  COMMENT '处理备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '告警时间',
    INDEX idx_user (user_id),
    INDEX idx_rule (rule_code),
    INDEX idx_handled (handled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控告警记录';

-- 15. 用户风险画像
DROP TABLE IF EXISTS t_user_risk_profile;
CREATE TABLE t_user_risk_profile (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '画像ID',
    user_id             BIGINT          NOT NULL UNIQUE             COMMENT '用户ID',
    cancel_count        INT             DEFAULT 0                   COMMENT '近30天取消次数',
    order_count         INT             DEFAULT 0                   COMMENT '近30天订单数',
    complaint_count     INT             DEFAULT 0                   COMMENT '近30天被投诉次数',
    risk_score          INT             DEFAULT 0                   COMMENT '风险评分 0-100',
    risk_level          TINYINT         DEFAULT 0                   COMMENT '0安全 1关注 2风险 3高危',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户风险画像';

-- 16. 需求热力图快照
DROP TABLE IF EXISTS t_demand_hotspot;
CREATE TABLE t_demand_hotspot (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '记录ID',
    lat             DECIMAL(10,7)   NOT NULL                    COMMENT '纬度',
    lng             DECIMAL(10,7)   NOT NULL                    COMMENT '经度',
    radius          INT             DEFAULT 1000                COMMENT '半径(米)',
    demand_count    INT             DEFAULT 0                   COMMENT '需求数量',
    snapshot_time   DATETIME        NOT NULL                    COMMENT '快照时间',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_coord (lat, lng),
    INDEX idx_time (snapshot_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求热力图快照（Redis为主，MySQL为历史备份）';

-- =====================================================
-- 三、文旅表（2张）
-- =====================================================

-- 17. 城市地标
DROP TABLE IF EXISTS t_city_landmark;
CREATE TABLE t_city_landmark (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '地标ID',
    city            VARCHAR(20)     NOT NULL                    COMMENT '所属城市',
    name            VARCHAR(50)     NOT NULL                    COMMENT '地标名称',
    lat             DECIMAL(10,7)   NOT NULL                    COMMENT '纬度',
    lng             DECIMAL(10,7)   NOT NULL                    COMMENT '经度',
    description     VARCHAR(500)    DEFAULT ''                  COMMENT '简介',
    image_url       VARCHAR(255)    DEFAULT ''                  COMMENT '图片URL',
    sort            INT             DEFAULT 0                   COMMENT '排序',
    status          TINYINT         DEFAULT 1                   COMMENT '0禁用 1启用',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市地标';

-- 18. 城市寄语
DROP TABLE IF EXISTS t_city_quote;
CREATE TABLE t_city_quote (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '寄语ID',
    city            VARCHAR(20)     NOT NULL                    COMMENT '城市名',
    content         VARCHAR(200)    NOT NULL                    COMMENT '寄语内容',
    author          VARCHAR(50)     DEFAULT ''                  COMMENT '出处/作者',
    sort            INT             DEFAULT 0                   COMMENT '排序',
    status          TINYINT         DEFAULT 1                   COMMENT '0禁用 1启用',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市寄语';

-- =====================================================
-- 四、支付/发票/推送表（3张）
-- =====================================================

-- 19. 支付记录
DROP TABLE IF EXISTS t_payment;
CREATE TABLE t_payment (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '支付ID',
    order_id        BIGINT          NOT NULL                    COMMENT '关联订单ID',
    user_id         BIGINT          NOT NULL                    COMMENT '支付人ID',
    amount          DECIMAL(10,2)   NOT NULL                    COMMENT '支付金额',
    pay_method      VARCHAR(20)     DEFAULT 'balance'           COMMENT '支付方式 balance/wxpay/alipay',
    pay_no          VARCHAR(64)                                 COMMENT '支付流水号',
    status          TINYINT         DEFAULT 0                   COMMENT '0待支付 1支付成功 2支付失败',
    pay_time        DATETIME                                    COMMENT '支付时间',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_order (order_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';

-- 20. 电子发票
DROP TABLE IF EXISTS t_invoice;
CREATE TABLE t_invoice (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '发票ID',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    order_id        BIGINT          NOT NULL                    COMMENT '关联订单ID',
    invoice_no      VARCHAR(32)                                 COMMENT '发票编号',
    title           VARCHAR(100)    NOT NULL                    COMMENT '发票抬头',
    tax_no          VARCHAR(30)                                 COMMENT '税号',
    amount          DECIMAL(10,2)   NOT NULL                    COMMENT '发票金额',
    status          TINYINT         DEFAULT 0                   COMMENT '0申请中 1已开具',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '申请时间',
    INDEX idx_user (user_id),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子发票';

-- 21. 推送日志
DROP TABLE IF EXISTS t_push_log;
CREATE TABLE t_push_log (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '日志ID',
    user_id         BIGINT          NOT NULL                    COMMENT '目标用户ID',
    title           VARCHAR(100)    NOT NULL                    COMMENT '推送标题',
    content         VARCHAR(500)                                COMMENT '推送内容',
    push_type       VARCHAR(20)     DEFAULT 'system'            COMMENT 'system/coupon/safety/marketing',
    push_channel    VARCHAR(10)     DEFAULT 'in_app'            COMMENT 'in_app/sms',
    status          TINYINT         DEFAULT 0                   COMMENT '0待发送 1已发送 2失败',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_type (push_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送日志';

-- =====================================================
-- 五、种子数据
-- =====================================================

-- 车型种子数据
INSERT INTO t_car_type (name, base_price, mid_per_km, long_per_km, super_long_per_km, per_min_price, max_passengers) VALUES
('快车',  8.00, 1.80, 1.60, 1.40, 0.30, 3),
('专车', 12.00, 2.50, 2.20, 2.00, 0.50, 3),
('商务七座', 15.00, 3.20, 2.80, 2.50, 0.60, 7);

-- 管理员种子数据（默认密码: admin123）
INSERT INTO t_admin (username, password, real_name, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'super_admin');

-- 优惠券种子数据
INSERT INTO t_coupon (name, threshold, discount, valid_days) VALUES
('新用户专享', 10.00, 5.00, 30),
('满50减8',   50.00, 8.00, 30),
('满100减15', 100.00, 15.00, 60);

-- 城市地标种子数据（江西旅游）
INSERT INTO t_city_landmark (city, name, lat, lng, description, sort) VALUES
('南昌', '滕王阁', 28.6842, 115.8759, '落霞与孤鹜齐飞，秋水共长天一色', 1),
('南昌', '八一起义纪念馆', 28.6783, 115.8844, '军旗升起的地方', 2),
('南昌', '南昌之星摩天轮', 28.6615, 115.8529, '世界第三高摩天轮', 3),
('赣州', '古城墙', 25.8456, 114.9356, '中国保存最完好的宋代城墙', 1),
('赣州', '通天岩', 25.8318, 114.9005, '江南第一石窟', 2),
('赣州', '郁孤台', 25.8547, 114.9312, '西北望长安，可怜无数山', 3),
('宁都', '翠微峰', 26.4825, 116.0209, '国家森林公园，丹霞奇观', 1),
('宁都', '中央苏区反围剿纪念馆', 26.4763, 116.0099, '红色记忆，革命精神', 2),
('九江', '庐山', 29.5550, 115.9810, '不识庐山真面目，只缘身在此山中', 1),
('景德镇', '古窑民俗博览区', 29.2909, 117.2068, '千年瓷都，匠心传承', 1);

-- 城市寄语种子数据
INSERT INTO t_city_quote (city, content, author, sort) VALUES
('南昌', '豫章故郡，洪都新府。星分翼轸，地接衡庐。', '王勃《滕王阁序》', 1),
('南昌', '物华天宝，人杰地灵。', '王勃《滕王阁序》', 2),
('赣州', '郁孤台下清江水，中间多少行人泪。', '辛弃疾《菩萨蛮》', 1),
('赣州', '这里是客家摇篮，赣水之滨。', '江南出行', 2),
('宁都', '翠微峰下，客家情深。', '江南出行', 1),
('九江', '飞流直下三千尺，疑是银河落九天。', '李白《望庐山瀑布》', 1),
('景德镇', '白如玉、明如镜、薄如纸、声如磬。', '景德镇瓷器赞', 1),
('江西', '踏遍青山人未老，风景这边独好。', '毛泽东', 1);
