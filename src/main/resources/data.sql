-- Datos iniciales de demostracion. Solo se insertan si la tabla esta vacia.
INSERT INTO cliente (nombre, correo, telefono)
SELECT * FROM (
    SELECT 'Ana Martinez' AS nombre, 'ana.martinez@example.com' AS correo, '3001112233' AS telefono UNION ALL
    SELECT 'Carlos Gomez', 'carlos.gomez@example.com', '3102223344' UNION ALL
    SELECT 'Lucia Torres', 'lucia.torres@example.com', '3203334455' UNION ALL
    SELECT 'Jorge Ramirez', 'jorge.ramirez@example.com', '3004445566' UNION ALL
    SELECT 'Maria Fernanda Lopez', 'maria.lopez@example.com', '3015556677'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM cliente);

INSERT INTO transaccion (cliente_id, fecha, monto, descripcion)
SELECT * FROM (
    SELECT 1 AS cliente_id, '2024-04-10' AS fecha, 1500.00 AS monto, 'Pago de tarjeta de credito' AS descripcion UNION ALL
    SELECT 1, '2024-04-15', 2200.00, 'Transferencia recibida' UNION ALL
    SELECT 2, '2024-04-11', 340.50, 'Compra en supermercado' UNION ALL
    SELECT 3, '2024-04-12', 875.00, 'Pago de servicios publicos' UNION ALL
    SELECT 4, '2024-04-14', 1200.00, 'Deposito bancario' UNION ALL
    SELECT 5, '2024-04-15', 950.00, 'Pago por servicios de consultoria' UNION ALL
    SELECT 3, '2024-04-16', 300.00, 'Recarga de celular' UNION ALL
    SELECT 2, '2024-04-17', 430.00, 'Pago de restaurante' UNION ALL
    SELECT 5, '2024-04-18', 120.00, 'Retiro en cajero automatico' UNION ALL
    SELECT 4, '2024-04-19', 199.99, 'Compra online'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM transaccion);
