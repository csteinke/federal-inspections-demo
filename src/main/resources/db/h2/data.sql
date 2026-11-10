-- Sample data only. These facilities, people, and visits are fictional.

INSERT INTO facilities (id, name, address, region) VALUES
  (1, 'North Ridge Records Annex', '100 Civic Plaza, North Ridge', 'Region 4'),
  (2, 'Harbor Light Supply Depot', '22 Pier Road, Harbor Light', 'Region 1'),
  (3, 'Cedar Bend Training Hall', '88 Oak Lane, Cedar Bend', 'Region 3'),
  (4, 'Red Mesa Equipment Yard', '15 Dust Road, Red Mesa', 'Region 7'),
  (5, 'Lakeview Public Works Shop', '400 Shore Drive, Lakeview', 'Region 2');

INSERT INTO inspectors (id, name) VALUES
  (1, 'Avery Quinn'),
  (2, 'Blake Nguyen'),
  (3, 'Cameron Ortiz'),
  (4, 'Drew Shah');

-- COMPLETED only when every safety check is done.
-- CANCELLED when the visit did not go ahead.
-- SCHEDULED for every other inspection, including an open visit with a missing check.
INSERT INTO inspections (id, facility_id, inspector_id, inspection_date, findings, status) VALUES
  (1,  1, 1, '2026-09-02', 'Records room is clear. Aisles are open.', 'COMPLETED'),
  (2,  1, 2, '2026-11-20', '', 'SCHEDULED'),
  (3,  1, 3, '2026-06-14', 'Training files are stored off the floor.', 'COMPLETED'),
  (4,  2, 1, '2026-08-19', 'Depot visit was called off.', 'CANCELLED'),
  (5,  2, 4, '2026-12-01', 'Winter visit is booked.', 'SCHEDULED'),
  (6,  3, 2, '2026-04-03', 'Hall exits and panels are in order.', 'COMPLETED'),
  (7,  3, 3, '2026-10-28', 'Panel label is missing.', 'SCHEDULED'),
  (8,  4, 4, '2026-07-22', 'Yard is tidy. Storage is labeled.', 'COMPLETED'),
  (9,  4, 1, '2026-11-18', 'Visit is not started.', 'SCHEDULED'),
  (10, 5, 2, '2026-05-09', 'Shop floor is clear.', 'COMPLETED'),
  (11, 5, 3, '2026-03-16', 'Visit was called off.', 'CANCELLED'),
  (12, 5, 4, '2026-12-15', 'Year-end visit is booked.', 'SCHEDULED');

-- Each inspection has three required safety checks.
-- A FALSE value means the check is missing.
INSERT INTO safety_checks (id, inspection_id, name, done) VALUES
  (1,  1, 'Fire exits', TRUE),
  (2,  1, 'Electrical panel', TRUE),
  (3,  1, 'Hazardous storage', TRUE),
  (4,  2, 'Fire exits', FALSE),
  (5,  2, 'Electrical panel', TRUE),
  (6,  2, 'Hazardous storage', TRUE),
  (7,  3, 'Fire exits', TRUE),
  (8,  3, 'Electrical panel', TRUE),
  (9,  3, 'Hazardous storage', TRUE),
  (10, 4, 'Fire exits', TRUE),
  (11, 4, 'Electrical panel', TRUE),
  (12, 4, 'Hazardous storage', FALSE),
  (13, 5, 'Fire exits', TRUE),
  (14, 5, 'Electrical panel', FALSE),
  (15, 5, 'Hazardous storage', TRUE),
  (16, 6, 'Fire exits', TRUE),
  (17, 6, 'Electrical panel', TRUE),
  (18, 6, 'Hazardous storage', TRUE),
  (19, 7, 'Fire exits', TRUE),
  (20, 7, 'Electrical panel', FALSE),
  (21, 7, 'Hazardous storage', TRUE),
  (22, 8, 'Fire exits', TRUE),
  (23, 8, 'Electrical panel', TRUE),
  (24, 8, 'Hazardous storage', TRUE),
  (25, 9, 'Fire exits', FALSE),
  (26, 9, 'Electrical panel', FALSE),
  (27, 9, 'Hazardous storage', FALSE),
  (28, 10, 'Fire exits', TRUE),
  (29, 10, 'Electrical panel', TRUE),
  (30, 10, 'Hazardous storage', TRUE),
  (31, 11, 'Fire exits', TRUE),
  (32, 11, 'Electrical panel', TRUE),
  (33, 11, 'Hazardous storage', FALSE),
  (34, 12, 'Fire exits', FALSE),
  (35, 12, 'Electrical panel', TRUE),
  (36, 12, 'Hazardous storage', TRUE);

ALTER TABLE facilities ALTER COLUMN id RESTART WITH 6;
ALTER TABLE inspectors ALTER COLUMN id RESTART WITH 5;
ALTER TABLE inspections ALTER COLUMN id RESTART WITH 13;
ALTER TABLE safety_checks ALTER COLUMN id RESTART WITH 37;
