-- =====================================================================
-- Cupcakes Gourmet - Modelo físico (MySQL 8 / MariaDB 10.4+)
-- Execute no HeidiSQL: aba "Consulta" > cole tudo > F9
-- =====================================================================
CREATE DATABASE IF NOT EXISTS cupcakes_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cupcakes_db;

CREATE TABLE IF NOT EXISTS usuario (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    nome              VARCHAR(100) NOT NULL,
    email             VARCHAR(150) NOT NULL,
    senha_hash        VARCHAR(100) NOT NULL,
    perfil            VARCHAR(20)  NOT NULL,          -- CLIENTE | ADMIN
    tentativas_falhas INT          NOT NULL DEFAULT 0,
    bloqueado_ate     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)         -- HU-01 RN#1
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categoria (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    nome VARCHAR(60) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categoria_nome UNIQUE (nome)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cupcake (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    nome       VARCHAR(100)  NOT NULL,
    descricao  VARCHAR(500)  NOT NULL,
    preco      DECIMAL(10,2) NOT NULL,
    foto_url   VARCHAR(500)  NULL,
    alergenicos VARCHAR(200) NULL,
    estoque    INT           NOT NULL DEFAULT 0,
    ativo      TINYINT(1)    NOT NULL DEFAULT 1,       -- 0 = removido da vitrine (exclusão lógica)
    PRIMARY KEY (id),
    CONSTRAINT ck_cupcake_preco CHECK (preco > 0),     -- HU-12 RN#2
    CONSTRAINT ck_cupcake_estoque CHECK (estoque >= 0)
) ENGINE=InnoDB;

-- Um cupcake pode pertencer a mais de uma categoria (HU-04 RN#1)
CREATE TABLE IF NOT EXISTS cupcake_categoria (
    cupcake_id   BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    PRIMARY KEY (cupcake_id, categoria_id),
    CONSTRAINT fk_cc_cupcake   FOREIGN KEY (cupcake_id)   REFERENCES cupcake (id),
    CONSTRAINT fk_cc_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id)
) ENGINE=InnoDB;

-- HU-11: cupom de desconto
CREATE TABLE IF NOT EXISTS cupom (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    codigo     VARCHAR(30) NOT NULL,
    percentual INT         NOT NULL,
    validade   DATE        NOT NULL,
    limite_uso INT         NOT NULL,
    usos       INT         NOT NULL DEFAULT 0,
    ativo      TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_cupom_codigo UNIQUE (codigo),
    CONSTRAINT ck_cupom_percentual CHECK (percentual BETWEEN 1 AND 100),
    CONSTRAINT ck_cupom_usos CHECK (usos >= 0 AND limite_uso >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    usuario_id       BIGINT        NOT NULL,
    data_hora        DATETIME(6)   NOT NULL,
    status           VARCHAR(30)   NOT NULL,           -- HU-10 RN#1
    forma_pagamento  VARCHAR(20)   NOT NULL,           -- CARTAO_CREDITO | PIX | BOLETO
    cep              VARCHAR(8)    NOT NULL,
    endereco_entrega VARCHAR(255)  NOT NULL,
    subtotal         DECIMAL(10,2) NOT NULL,
    frete            DECIMAL(10,2) NOT NULL,
    cupom_id         BIGINT        NULL,               -- HU-11 (opcional)
    desconto         DECIMAL(10,2) NOT NULL DEFAULT 0, -- HU-11
    total            DECIMAL(10,2) NOT NULL,
    prazo_entrega_dias INT         NOT NULL,
    vencimento_boleto DATE         NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_pedido_cupom   FOREIGN KEY (cupom_id)   REFERENCES cupom (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS item_pedido (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    pedido_id      BIGINT        NOT NULL,
    cupcake_id     BIGINT        NOT NULL,
    quantidade     INT           NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,             -- preço congelado no momento da compra
    PRIMARY KEY (id),
    CONSTRAINT ck_item_qtd CHECK (quantidade BETWEEN 1 AND 50),   -- HU-06 RN#2 / HU-07 RN#1
    CONSTRAINT fk_item_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedido (id),
    CONSTRAINT fk_item_cupcake FOREIGN KEY (cupcake_id) REFERENCES cupcake (id)
) ENGINE=InnoDB;
