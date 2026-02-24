-- Database Schema for FindBoardGame Project

-- MEMBER Table
CREATE TABLE IF NOT EXISTS MEMBER (
    idx BIGINT AUTO_INCREMENT PRIMARY KEY,
    userid VARCHAR(255) NOT NULL UNIQUE,
    userpwd VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(50),
    birth VARCHAR(50),
    email VARCHAR(255) NOT NULL,
    regdate DATETIME DEFAULT CURRENT_TIMESTAMP,
    profile_image VARCHAR(255),
    level INT DEFAULT 0
);

-- board Table
CREATE TABLE IF NOT EXISTS board (
    idx BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    userid VARCHAR(255),
    views BIGINT DEFAULT 0,
    type VARCHAR(50) NOT NULL,
    regdate DATETIME DEFAULT CURRENT_TIMESTAMP,
    likeCount INT DEFAULT 0,
    dislikeCount INT DEFAULT 0,
    FOREIGN KEY (userid) REFERENCES MEMBER(userid)
);

-- coment Table
CREATE TABLE IF NOT EXISTS coment (
    idx BIGINT AUTO_INCREMENT PRIMARY KEY,
    boardidx BIGINT,
    userid VARCHAR(255),
    ment TEXT NOT NULL,
    regdate DATETIME DEFAULT CURRENT_TIMESTAMP,
    parentidx BIGINT,
    FOREIGN KEY (boardidx) REFERENCES board(idx) ON DELETE CASCADE,
    FOREIGN KEY (userid) REFERENCES MEMBER(userid)
);

-- board_like Table
CREATE TABLE IF NOT EXISTS board_like (
    idx BIGINT AUTO_INCREMENT PRIMARY KEY,
    boardIdx BIGINT,
    userid VARCHAR(255),
    type ENUM('like', 'dislike'),
    regdate DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- notification Table
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receiver_idx BIGINT,
    board_idx BIGINT,
    comment_idx BIGINT,
    message VARCHAR(255),
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    isRead BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (receiver_idx) REFERENCES MEMBER(idx),
    FOREIGN KEY (board_idx) REFERENCES board(idx),
    FOREIGN KEY (comment_idx) REFERENCES coment(idx)
);

-- image Table
CREATE TABLE IF NOT EXISTS image (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    boardidx INT,
    userid VARCHAR(255),
    filename VARCHAR(200),
    originalname VARCHAR(200),
    fileurl VARCHAR(200),
    filesize INT,
    regdate DATETIME DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(20),
    FOREIGN KEY (userid) REFERENCES MEMBER(userid)
);

-- search_lank Table
CREATE TABLE IF NOT EXISTS search_lank (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255),
    count INT DEFAULT 0,
    updated_at DATETIME
);
