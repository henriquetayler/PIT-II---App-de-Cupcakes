-- =====================================================================
-- HU-11 (cupom de desconto) - atualização de um banco JÁ criado com o 01_schema.sql antigo.
-- Execute no HeidiSQL (aba Consulta, F9) ANTES de subir a nova versão do sistema.
-- Banco novo? Basta o 01_schema.sql, que já inclui estas mudanças.
-- =====================================================================
USE cupcakes_db;

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

ALTER TABLE pedido
    ADD COLUMN cupom_id BIGINT NULL AFTER frete,
    ADD COLUMN desconto DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER cupom_id,
    ADD CONSTRAINT fk_pedido_cupom FOREIGN KEY (cupom_id) REFERENCES cupom (id);

-- Os cupons de exemplo (BEMVINDO10, DOCE15 e NATAL2025 vencido) são criados
-- automaticamente pelo sistema na próxima inicialização.
