-- Insert test drones
INSERT INTO drones (serial_number, model, weight_limit, battery_capacity, state)
VALUES
    ('DRN-001-2023', 'LIGHTWEIGHT', 100, 100, 'IDLE'),
    ('DRN-002-2023', 'MIDDLEWEIGHT', 250, 90, 'IDLE'),
    ('DRN-003-2023', 'CRUISERWEIGHT', 500, 80, 'IDLE'),
    ('DRN-004-2023', 'HEAVYWEIGHT', 1000, 70, 'IDLE'),


-- Insert test medications
INSERT INTO medications (name, weight, code, image_data, drone_id)
VALUES
    ('Paracetamol', 50, 'MED_001', NULL, NULL),
    ('Antibiotics', 80, 'MED_002', NULL, NULL),
    ('Insulin', 30, 'MED_003', NULL, NULL),
    ('Ventolin', 20, 'MED_004', NULL, NULL),
    ('Amoxicillin', 60, 'MED_005', NULL, NULL),