# El Ultimo Arranque — MVC con Spring Boot

Proyecto de Programacion II: sistema web MVC, base de datos MySQL y app Android.

## Arquitectura conforme al material del docente
- **Modelo**: `SistemaWeb/src/main/java/gt/umg/taller/model/`, `dao/`, `service/` y `config/`.
- **Controlador**: `SistemaWeb/src/main/java/gt/umg/taller/controller/` con `@Controller`, `@GetMapping`, `@PostMapping` y `Model`.
- **Vista**: `SistemaWeb/src/main/resources/templates/panel.html` con Thymeleaf.
- **API Android**: `InventarioApiControlador` con `@RestController` (extensión adicional).

## Funcionalidades conservadas
CRUD clientes, vehiculos, mecanicos, repuestos y servicios; ordenes de trabajo, transiciones de estado, servicios y repuestos asociados, devoluciones de inventario, comprobantes y cinco reportes PDF; consulta de inventario bajo desde Android.

## Requisitos
JDK 17 o 21, Maven, MySQL 8 y Android Studio para la app movil. Spring Boot integra Tomcat; **no iniciar el Tomcat externo** al ejecutar esta version en el mismo puerto.

## Inicio rapido
1. En MySQL, disponer de la base `db_taller_mecanico` con las tablas de `BaseDatos/crear_base_datos.sql` y opcionalmente los datos de prueba. **No ejecutar el script de creacion sobre una base con datos importantes sin respaldo**.
2. Configurar `TALLER_DB_USER` y `TALLER_DB_PASSWORD` en el entorno (opcionalmente `TALLER_DB_URL`).
3. Abrir `SistemaWeb/` como proyecto Maven en NetBeans o desde terminal ejecutar `cd SistemaWeb && mvn spring-boot:run`.
4. Abrir `http://localhost:8080/SistemaWeb/app`.
5. Probar `http://localhost:8080/SistemaWeb/api/inventario` (JSON).
6. Para Android emulado, ejecutar `adb reverse tcp:8080 tcp:8080`, abrir `AndroidAppElUltimoArranque/` en Android Studio y ejecutar la app. La URL de `MainActivity.java` ya corresponde al contexto `/SistemaWeb`.

## Documentacion
- `MIGRACION_SPRING_MVC.md`: explicacion detallada y pasos.
- `Diagramas/Arquitectura_Spring_MVC.mmd` y `.png`: flujo de componentes.
- `Documentacion/VersionAnterior-JSP-Servlets/`: respaldo de los archivos anteriores, no incluidos en el codigo activo.

## Verificacion pendiente
Es necesario ejecutar Maven, probar MySQL y Android en el equipo de desarrollo. La presencia de archivos no equivale a una prueba integral completada.
