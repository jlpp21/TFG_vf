CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL,
    telefono VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS transaccion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    descripcion VARCHAR(255),
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    perfil_financiero VARCHAR(40) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS banco_conectado (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    banco_codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    dominio VARCHAR(100),
    color VARCHAR(20),
    logo_url VARCHAR(500),
    tipo_cuenta VARCHAR(20) NOT NULL DEFAULT 'CORRIENTE',
    numero_final VARCHAR(4),
    fecha_conexion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_banco_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT uk_usuario_banco UNIQUE (usuario_id, banco_codigo, tipo_cuenta)
);

CREATE TABLE IF NOT EXISTS objetivo_usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    objetivo VARCHAR(60) NOT NULL,
    plazo VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_objetivo_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT uk_objetivo_usuario UNIQUE (usuario_id, objetivo)
);

CREATE TABLE IF NOT EXISTS movimiento (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    banco_conectado_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    categoria VARCHAR(80) NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_movimiento_banco FOREIGN KEY (banco_conectado_id) REFERENCES banco_conectado(id) ON DELETE CASCADE,
    INDEX idx_movimiento_fecha (fecha)
);
