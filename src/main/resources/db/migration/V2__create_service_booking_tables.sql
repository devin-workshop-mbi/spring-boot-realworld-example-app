create table vehicles (
  id varchar(255) primary key,
  user_id varchar(255) not null,
  make varchar(100) not null,
  model varchar(100) not null,
  year integer not null,
  license_plate varchar(20),
  vin varchar(17),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  foreign key (user_id) references users(id)
);

create table service_types (
  id varchar(255) primary key,
  name varchar(100) not null,
  description text,
  estimated_duration_minutes integer not null,
  price decimal(10, 2) not null,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create table service_bookings (
  id varchar(255) primary key,
  user_id varchar(255) not null,
  vehicle_id varchar(255) not null,
  service_type_id varchar(255) not null,
  scheduled_date TIMESTAMP NOT NULL,
  status varchar(20) not null default 'PENDING',
  notes text,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  foreign key (user_id) references users(id),
  foreign key (vehicle_id) references vehicles(id),
  foreign key (service_type_id) references service_types(id)
);

-- Insert some default service types
insert into service_types (id, name, description, estimated_duration_minutes, price) values
  ('st-001', 'Oil Change', 'Standard oil change with filter replacement', 30, 49.99),
  ('st-002', 'Brake Service', 'Brake pad inspection and replacement', 60, 149.99),
  ('st-003', 'Tire Rotation', 'Rotate all four tires for even wear', 30, 29.99),
  ('st-004', 'Full Inspection', 'Comprehensive vehicle inspection', 90, 99.99),
  ('st-005', 'Air Filter Replacement', 'Replace engine air filter', 15, 39.99),
  ('st-006', 'Battery Service', 'Battery test and replacement if needed', 30, 129.99),
  ('st-007', 'Transmission Service', 'Transmission fluid change and inspection', 60, 199.99),
  ('st-008', 'Coolant Flush', 'Complete cooling system flush', 45, 89.99);
