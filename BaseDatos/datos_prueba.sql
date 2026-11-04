USE db_taller_mecanico;
INSERT IGNORE INTO clientes(id,identificador,nombre,telefono,correo) VALUES(1,'CF-001','Juan Perez','5555-1234','juan@ejemplo.com'),(2,'CF-002','Maria Lopez','5555-5678','maria@ejemplo.com');
INSERT IGNORE INTO vehiculos(id,cliente_id,placa,marca,modelo,anio) VALUES(1,1,'P123ABC','Toyota','Corolla',2018),(2,2,'P456DEF','Honda','Civic',2020);
INSERT IGNORE INTO mecanicos(id,nombre,telefono,especialidad) VALUES(1,'Carlos Lopez','5555-1000','Frenos'),(2,'Ana Ruiz','5555-2000','Motor');
INSERT IGNORE INTO repuestos(id,nombre,descripcion,precio,existencia) VALUES(1,'Pastillas de freno','Juego delantero',350,10),(2,'Bateria','12 voltios',850,3),(3,'Filtro de aceite','Universal',75,20);
INSERT IGNORE INTO servicios(id,descripcion,costo) VALUES(1,'Revision de frenos',125),(2,'Cambio de aceite',100),(3,'Cambio de bateria',75);
