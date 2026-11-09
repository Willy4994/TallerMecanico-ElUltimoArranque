# El Ultimo Arranque — Migracion a Spring Boot MVC

## Arquitectura
- **Modelo:** `model/`, `dao/`, `service/`, `config/` (JDBC/MySQL, sin dependencia de las vistas).
- **Controlador:** `controller/PanelControlador.java` (`@Controller`, `@GetMapping`, `@PostMapping`, `Model`).
- **Vista:** `src/main/resources/templates/panel.html` (HTML/Thymeleaf, sin consultas SQL ni llamadas DAO).
- **API Android:** `controller/InventarioApiControlador.java` (`@RestController` JSON).
- **PDF:** `ReporteControlador.java` y `ComprobanteControlador.java`.

## Ejecutar en NetBeans o Terminal
1. Abrir `SistemaWeb/` como proyecto Maven en NetBeans (JDK 17 o 21).
2. Importar `BaseDatos/crear_base_datos.sql` y, si se desea, `BaseDatos/datos_prueba.sql` en MySQL.
3. Configurar las variables de entorno `TALLER_DB_USER`, `TALLER_DB_PASSWORD` y opcionalmente `TALLER_DB_URL` (se mantienen de la version anterior).
4. Ejecutar `mvn spring-boot:run` desde `SistemaWeb/` (no necesita Tomcat externo).
5. Navegar a `http://localhost:8080/SistemaWeb/app`.
6. API Android: `http://localhost:8080/SistemaWeb/api/inventario`. En emulador con `adb reverse tcp:8080 tcp:8080` se puede usar `127.0.0.1`.
7. Si el Tomcat anterior ocupa el puerto 8080, detenerlo antes de ejecutar Spring Boot, o usar `PORT=8081` y actualizar la URL de Android.

## Importante
- Los archivos JSP/Servlet originales estan respaldados en `Documentacion/VersionAnterior-JSP-Servlets/`, **fuera de la compilacion activa**.
- Se conservaron `BaseDatos/`, `AndroidAppElUltimoArranque/`, `Diagramas/` y el APK original. El APK es un artefacto de la version previa; requiere probar la API migrada.
- Esta migracion no modifica la base de datos existente. Haz respaldo antes de pruebas con operaciones de escritura.
- La estructura corresponde a la presentacion del docente «De Swing a la Web: el patron MVC con Spring Boot».
- La compilacion y las pruebas reales con MySQL/Android deben verificarse en el equipo local.
