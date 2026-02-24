MERGE INTO apartment (id, name, address, unit_count, created_at)
KEY(id)
VALUES (1, 'Demo Apartmanı', 'Demo Mah. No:1', 12, NOW());
