# El Último Arranque — Proyecto Final de Programación II

Sistema de gestión para taller mecánico, compuesto por una aplicación web Java y una aplicación Android que consulta el reporte de repuestos con inventario bajo.

## Funcionalidades

- Gestión de clientes, vehículos, mecánicos, repuestos y servicios.
- Órdenes de trabajo: PENDIENTE → EN PROCESO → FINALIZADA; cancelación de órdenes pendientes o en proceso.
- Asignación de servicios y repuestos, descuento de inventario y devolución de existencias al retirar repuestos o cancelar órdenes.
- Cálculo de total al finalizar y generación de comprobantes.
- Cinco reportes PDF: órdenes por estado, historial por placa, inventario bajo, órdenes por mecánico/estado e ingresos por rango de fechas.
- Android: consulta del inventario bajo (existencia ≤ 5) mediante API HTTP y la misma base MySQL del sistema web.

## Tecnologías y requisitos

- Java 17, Maven, JSP/Servlets Jakarta, JDBC, MySQL 8, Apache Tomcat 10.1 y NetBeans.
- Android Studio, Android SDK y emulador Android con ADB.
- Internet para descargar dependencias Maven y Gradle la primera vez.

## Estructura del proyecto

```text
TallerMecanico-ElUltimoArranque/
├── SistemaWeb/                 Aplicación web Maven (WAR)
├── AndroidAppElUltimoArranque/ Aplicación Android Studio
├── BaseDatos/
│   ├── crear_base_datos.sql
│   └── datos_prueba.sql
├── Diagramas/                  Diagramas UML y DER, Mermaid y PNG
├── ElUltimoArranque.apk        APK de demostración
└── README.md
```

## 1. Preparar MySQL

1. Iniciar MySQL y abrir MySQL Workbench.
2. Ejecutar `BaseDatos/crear_base_datos.sql`.
3. Ejecutar `BaseDatos/datos_prueba.sql` (opcional si se desea una base con registros de ejemplo).
4. Crear o configurar un usuario MySQL con permisos sobre `db_taller_mecanico`.
5. Configurar las credenciales para el proceso que ejecuta Tomcat:
   - `TALLER_DB_USER`: usuario de MySQL (por defecto en `Conexion.java`: `taller_app`).
   - `TALLER_DB_PASSWORD`: contraseña del usuario.
   - `TALLER_DB_URL`: opcional, por defecto `jdbc:mysql://127.0.0.1:3306/db_taller_mecanico?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guatemala`.

`Conexion.java` también admite `TALLER_DB_USER` y `TALLER_DB_PASSWORD` como propiedades Java (`-D...`). No subir credenciales a Git.

**Importante:** el endpoint `/api/inventario` exige específicamente las variables de entorno `TALLER_DB_USER` y `TALLER_DB_PASSWORD`, incluso si el resto del sitio se conecta mediante propiedades Java. Asegurarse de definirlas en el proceso de Tomcat.

## 2. Ejecutar la aplicación web

1. Abrir NetBeans → File → Open Project → `SistemaWeb`.
2. Confirmar JDK 17 y configurar Apache Tomcat 10.1 como servidor.
3. Ejecutar Clean and Build y luego Run.
4. Abrir la URL mostrada por NetBeans. En un despliegue estándar del WAR `ultimo-arranque.war`, la ruta es `http://localhost:8080/ultimo-arranque/app`. Si NetBeans configuró el contexto raíz `/`, la ruta es `http://localhost:8080/app`.
5. Confirmar que los módulos de clientes, vehículos, mecánicos, servicios, repuestos y órdenes cargan correctamente.

**Alternativa Maven:** desde `SistemaWeb`, ejecutar `mvn clean package`. El WAR se genera en `target/ultimo-arranque.war` y puede desplegarse en Tomcat 10.1.

## 3. Probar una orden de trabajo

1. Registrar o seleccionar un cliente, vehículo y mecánico.
2. Crear una orden con problema descrito; comprobar estado PENDIENTE.
3. Iniciar la orden; comprobar estado EN PROCESO.
4. Agregar al menos un servicio y, si corresponde, repuestos con existencia suficiente.
5. Finalizar y comprobar estado FINALIZADA, total y comprobante.
6. En otra orden de prueba, comprobar devolución de inventario al retirar un repuesto o cancelar la orden.
7. Desde Reportes, descargar y revisar los cinco PDF.

## 4. Probar la API de inventario

Con Tomcat y MySQL ejecutándose, abrir en el navegador:

- Si el contexto web es raíz `/`: `http://localhost:8080/api/inventario`.
- Si el contexto web es `/ultimo-arranque`: `http://localhost:8080/ultimo-arranque/api/inventario`.

La respuesta es un arreglo JSON de repuestos con `existencia <= 5`.

## 5. Ejecutar Android

1. Abrir `AndroidAppElUltimoArranque` en Android Studio y sincronizar Gradle.
2. Iniciar el emulador Android.
3. La versión actual de `MainActivity.java` utiliza `http://127.0.0.1:8080/api/inventario`.
4. Si la aplicación web está desplegada en el contexto raíz `/`, ejecutar en Terminal:

   ```bash
   adb reverse tcp:8080 tcp:8080
   ```

   Esto redirige el puerto 8080 del emulador al equipo anfitrión. Es necesario mantener el emulador conectado y Tomcat en ejecución.
5. Ejecutar la app desde Android Studio o instalar el APK de demostración. Pulsar **Actualizar** para consultar la API.
6. Si el WAR se despliega bajo `/ultimo-arranque`, se debe cambiar la URL en `MainActivity.java` a `http://127.0.0.1:8080/ultimo-arranque/api/inventario` y recompilar el APK; `adb reverse` por sí solo no cambia el contexto de Tomcat.

**Limitación:** el APK entregado está configurado para la demostración en emulador con `adb reverse`. Para un dispositivo físico se requiere configurar la URL de una IP accesible o un servidor HTTPS; no funcionará automáticamente con `127.0.0.1`.

## 6. Programación orientada a objetos

- **Encapsulamiento:** campos privados y métodos de acceso.
- **Herencia:** `Cliente` y `Mecanico` extienden `Persona`.
- **Polimorfismo:** `ResumenPersonas` utiliza `Persona.tipo()` con implementaciones de `Cliente` y `Mecanico`.
- **Composición:** `OrdenTrabajo` crea y administra detalles `DetalleServicio` y `DetalleRepuesto`.
- **Colecciones:** `List`, `ArrayList`, `Map`.
- **Excepciones y transacciones:** validaciones, `SQLException`, `commit()` y `rollback()` en `OrdenService`.
- **Acceso a datos:** `PreparedStatement` con JDBC.

## 7. Diagramas

La carpeta `Diagramas` contiene el diagrama entidad-relación y el diagrama UML de clases. Conservar tanto los PNG como los archivos editables Mermaid (`.mmd`).

## 8. Seguridad y alcance

Proyecto académico para ejecución local. No dispone de autenticación de usuarios en el panel ni en la API; no publicarlo directamente en Internet. El tráfico HTTP Android se permite únicamente para las pruebas locales. No incluir contraseñas ni archivos `local.properties` en el repositorio.

## 9. Control de versiones

El repositorio debe contener los archivos fuente, SQL, diagramas y documentación. Excluir archivos generados (`target/`, `build/`, `.gradle/`, configuraciones locales del IDE y `local.properties`). Los tres integrantes deben realizar contribuciones auténticas y commits en fechas reales de trabajo.

## 10. Lista de verificación

- [ ] Compilación Maven satisfactoria.
- [ ] Conexión MySQL verificada.
- [ ] CRUD de los cinco catálogos.
- [ ] Flujo de órdenes, inventario y comprobantes.
- [ ] Cinco reportes PDF.
- [ ] API JSON con existencia ≤ 5.
- [ ] Android consulta datos actualizados.
- [ ] Diagramas UML y DER actualizados.
- [ ] Instalación probada en otro equipo.
