<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8"
         import="java.util.*,gt.umg.taller.dao.Tablas" %>

<%!
    // =====================================================
    // FUNCIONES AUXILIARES
    // =====================================================

    private String escaparHtml(Object valor) {
        if (valor == null) {
            return "";
        }

        return String.valueOf(valor)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\u0027")
                .replace("<", "\\u003c")
                .replace(">", "\\u003e")
                .replace("&", "\\u0026")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String jsonFila(
            Map<String, Object> fila,
            Tablas.Definicion def) {

        StringBuilder s = new StringBuilder("{");

        for (String columna : def.columnas()) {

            if (s.length() > 1) {
                s.append(",");
            }

            Object valor = fila.get(columna);

            String texto = valor == null
                    ? ""
                    : String.valueOf(valor);

            s.append("\"")
             .append(escaparJson(columna))
             .append("\":\"")
             .append(escaparJson(texto))
             .append("\"");
        }

        return s.append("}").toString();
    }

    private String boton(
            String ctx,
            int id,
            String accion,
            String texto) {

        return "<form class='inline' method='post' action='"
                + escaparHtml(ctx)
                + "/app'>"
                + "<input type='hidden' name='modulo' value='ordenes'>"
                + "<input type='hidden' name='accion' value='"
                + escaparHtml(accion)
                + "'>"
                + "<input type='hidden' name='id' value='"
                + id
                + "'>"
                + "<button class='mini' type='submit'>"
                + escaparHtml(texto)
                + "</button>"
                + "</form>";
    }

    private String quitar(
            String ctx,
            Map<String, Object> detalle,
            String accion) {

        return "<form class='inline' method='post' action='"
                + escaparHtml(ctx)
                + "/app'>"
                + "<input type='hidden' name='modulo' value='ordenes'>"
                + "<input type='hidden' name='accion' value='"
                + escaparHtml(accion)
                + "'>"
                + "<input type='hidden' name='id' value='"
                + escaparHtml(detalle.get("orden_id"))
                + "'>"
                + "<input type='hidden' name='detalle' value='"
                + escaparHtml(detalle.get("id"))
                + "'>"
                + "<button class='mini danger' type='submit'>"
                + "QUITAR"
                + "</button>"
                + "</form>";
    }
%>

<%
    // =====================================================
    // DATOS DEL CONTROLADOR
    // =====================================================

    String modulo = (String) request.getAttribute("modulo");

    if (modulo == null || modulo.trim().isEmpty()) {
        modulo = "inicio";
    }

    Map<String, Tablas.Definicion> defs =
            (Map<String, Tablas.Definicion>)
            request.getAttribute("definiciones");

    if (defs == null) {
        defs = Collections.emptyMap();
    }

    Tablas.Definicion def =
            (Tablas.Definicion) request.getAttribute("def");

    List<Map<String, Object>> filas =
            (List<Map<String, Object>>)
            request.getAttribute("filas");

    if (filas == null) {
        filas = Collections.emptyList();
    }

    String ctx = request.getContextPath();

    String tituloPagina;

    if (modulo.equals("inicio")) {
        tituloPagina = "EL ULTIMO ARRANQUE";
    } else if (modulo.equals("ordenes")) {
        tituloPagina = "ORDENES DE TRABAJO";
    } else if (modulo.equals("reportes")) {
        tituloPagina = "REPORTES";
    } else if (def != null) {
        tituloPagina = def.titulo().toUpperCase();
    } else {
        tituloPagina = "PANEL DE CONTROL";
    }
%>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>El Ultimo Arranque</title>

    <link rel="stylesheet"
          href="<%= escaparHtml(ctx) %>/assets/estilo.css">
</head>

<body>

<div class="shell">

    <!-- ============================================= -->
    <!-- MENU LATERAL -->
    <!-- ============================================= -->

    <aside class="menu">

        <div class="logo">
            ▣ EL ULTIMO<br>
            ARRANQUE

            <span>GARAGE SYSTEM / 1987</span>
        </div>

        <a href="<%= escaparHtml(ctx) %>/app">
            ⌂ Inicio
        </a>

        <% for (Tablas.Definicion d : defs.values()) { %>

            <a class="<%= modulo.equals(d.tabla()) ? "active" : "" %>"
               href="<%= escaparHtml(ctx) %>/app?modulo=<%= escaparHtml(d.tabla()) %>">

                ▸ <%= escaparHtml(d.titulo()) %>

            </a>

        <% } %>

        <a href="<%= escaparHtml(ctx) %>/app?modulo=ordenes">
            ▸ Ordenes
        </a>

        <a href="<%= escaparHtml(ctx) %>/app?modulo=reportes">
            ▸ Reportes PDF
        </a>

        <div class="sidefoot">
            UMG · PROGRAMACION II<br>
            JAVA / MYSQL / MVC
        </div>

    </aside>

    <!-- ============================================= -->
    <!-- CONTENIDO PRINCIPAL -->
    <!-- ============================================= -->

    <main class="contenido">

        <header>

            <div>
                <small>▸ PANEL DE CONTROL</small>

                <h1>
                    <%= escaparHtml(tituloPagina) %>
                </h1>
            </div>

            <span class="tag">
                ● TALLER ABIERTO
            </span>

        </header>

        <!-- MENSAJES -->

        <%
            String mensaje = request.getParameter("mensaje");

            if (mensaje != null && !mensaje.isEmpty()) {
        %>

            <div class="aviso">
                <%= escaparHtml(mensaje) %>
            </div>

        <% } %>

        <!-- ============================================= -->
        <!-- DASHBOARD -->
        <!-- ============================================= -->

        <% if (modulo.equals("inicio")) {

            List<Map<String, Object>> ordenesInicio =
                    (List<Map<String, Object>>)
                    request.getAttribute("ordenes");

            if (ordenesInicio == null) {
                ordenesInicio = Collections.emptyList();
            }

            int pendientes = 0;
            int proceso = 0;
            int finalizadas = 0;

            for (Map<String, Object> orden : ordenesInicio) {

                String estado =
                        String.valueOf(orden.get("estado"));

                if (estado.equals("PENDIENTE")) {
                    pendientes++;
                }

                if (estado.equals("EN PROCESO")) {
                    proceso++;
                }

                if (estado.equals("FINALIZADA")) {
                    finalizadas++;
                }
            }
        %>

        <div class="cards">

            <div class="card">
                <span>ORDENES</span>
                <strong><%= ordenesInicio.size() %></strong>
            </div>

            <div class="card">
                <span>PENDIENTES</span>
                <strong><%= pendientes %></strong>
            </div>

            <div class="card">
                <span>EN PROCESO</span>
                <strong><%= proceso %></strong>
            </div>

            <div class="card">
                <span>FINALIZADAS</span>
                <strong><%= finalizadas %></strong>
            </div>

        </div>

        <section class="caja">

            <h2>BIENVENIDO AL GARAGE</h2>

            <p>
                Registra clientes, vehiculos y mecanicos;
                luego crea ordenes, asigna servicios y
                repuestos, y genera comprobantes.
            </p>

            <a class="btn"
               href="<%= escaparHtml(ctx) %>/app?modulo=ordenes">

                ABRIR ORDENES →

            </a>

        </section>

        <% } %>

        <!-- ============================================= -->
        <!-- MODULOS CRUD -->
        <!-- ============================================= -->

        <% if (def != null) { %>

        <section class="caja">

            <h2>NUEVO REGISTRO / EDITAR</h2>

            <form method="post"
                  action="<%= escaparHtml(ctx) %>/app"
                  class="formgrid">

                <input type="hidden"
                       name="modulo"
                       value="<%= escaparHtml(modulo) %>">

                <label>
                    ID para editar (vacio = nuevo)

                    <input type="number"
                           name="id"
                           min="1"
                           placeholder="Nuevo registro">
                </label>

                <% for (Tablas.Campo campo : def.campos()) {

                    String tipo = campo.tipo();

                    String tipoHtml =
                            tipo.equals("decimal")
                            ? "number"
                            : tipo;
                %>

                <label>

                    <%= escaparHtml(campo.etiqueta()) %>

                    <input
                        name="<%= escaparHtml(campo.nombre()) %>"
                        type="<%= escaparHtml(tipoHtml) %>"

                        <% if (tipo.equals("decimal")) { %>
                            step="0.01"
                            min="0"
                        <% } else if (tipo.equals("number")) { %>
                            min="0"
                        <% } %>

                        placeholder="<%= escaparHtml(campo.etiqueta()) %>">

                </label>

                <% } %>

                <button class="btn" type="submit">
                    GUARDAR REGISTRO
                </button>

            </form>

        </section>

        <!-- TABLA DE REGISTROS -->

        <section class="caja">

            <h2>
                REGISTROS · <%= filas.size() %>
            </h2>

            <div class="tabla">

                <table>

                    <thead>
                        <tr>

                            <% for (String columna : def.columnas()) { %>

                                <th>
                                    <%= escaparHtml(columna.toUpperCase()) %>
                                </th>

                            <% } %>

                            <th>ACCIONES</th>

                        </tr>
                    </thead>

                    <tbody>

                        <% for (Map<String, Object> fila : filas) { %>

                        <tr>

                            <% for (String columna : def.columnas()) { %>

                                <td>
                                    <%= escaparHtml(fila.get(columna)) %>
                                </td>

                            <% } %>

                            <td>

                                <button class="mini"
                                        type="button"
                                        onclick='editar(<%= jsonFila(fila, def) %>)'>

                                    EDITAR

                                </button>

                                <form class="inline"
                                      method="post"
                                      action="<%= escaparHtml(ctx) %>/app"
                                      onsubmit="return confirm('¿Eliminar registro?')">

                                    <input type="hidden"
                                           name="modulo"
                                           value="<%= escaparHtml(modulo) %>">

                                    <input type="hidden"
                                           name="accion"
                                           value="borrar">

                                    <input type="hidden"
                                           name="id"
                                           value="<%= escaparHtml(fila.get("id")) %>">

                                    <button class="mini danger"
                                            type="submit">
                                        BORRAR
                                    </button>

                                </form>

                            </td>

                        </tr>

                        <% } %>

                    </tbody>

                </table>

            </div>

        </section>

        <!-- INVENTARIO -->

        <% if (modulo.equals("repuestos")) { %>

        <section class="caja">

            <h2>ABASTECER INVENTARIO</h2>

            <form method="post"
                  action="<%= escaparHtml(ctx) %>/app"
                  class="formgrid">

                <input type="hidden"
                       name="modulo"
                       value="repuestos">

                <input type="hidden"
                       name="accion"
                       value="abastecer">

                <label>
                    ID repuesto

                    <input type="number"
                           name="id"
                           min="1"
                           required>
                </label>

                <label>
                    Unidades a sumar

                    <input type="number"
                           name="cantidad"
                           min="1"
                           required>
                </label>

                <button class="btn" type="submit">
                    ABASTECER
                </button>

            </form>

        </section>

        <% } %>

        <% } %>

        <!-- ============================================= -->
        <!-- ORDENES DE TRABAJO -->
        <!-- ============================================= -->

        <% if (modulo.equals("ordenes")) {

            List<Map<String, Object>> os =
                    (List<Map<String, Object>>)
                    request.getAttribute("ordenes");

            List<Map<String, Object>> vs =
                    (List<Map<String, Object>>)
                    request.getAttribute("vehiculos");

            List<Map<String, Object>> ms =
                    (List<Map<String, Object>>)
                    request.getAttribute("mecanicos");

            List<Map<String, Object>> ss =
                    (List<Map<String, Object>>)
                    request.getAttribute("servicios");

            List<Map<String, Object>> rs =
                    (List<Map<String, Object>>)
                    request.getAttribute("repuestos");

            List<Map<String, Object>> ds =
                    (List<Map<String, Object>>)
                    request.getAttribute("detServicios");

            List<Map<String, Object>> dr =
                    (List<Map<String, Object>>)
                    request.getAttribute("detRepuestos");

            if (os == null) os = Collections.emptyList();
            if (vs == null) vs = Collections.emptyList();
            if (ms == null) ms = Collections.emptyList();
            if (ss == null) ss = Collections.emptyList();
            if (rs == null) rs = Collections.emptyList();
            if (ds == null) ds = Collections.emptyList();
            if (dr == null) dr = Collections.emptyList();
        %>

        <!-- CREAR ORDEN -->

        <section class="caja">

            <h2>CREAR ORDEN</h2>

            <form method="post"
                  action="<%= escaparHtml(ctx) %>/app"
                  class="formgrid">

                <input type="hidden"
                       name="modulo"
                       value="ordenes">

                <input type="hidden"
                       name="accion"
                       value="crear">

                <label>
                    Vehiculo

                    <select name="vehiculo_id" required>

                        <% for (Map<String, Object> v : vs) { %>

                        <option value="<%= escaparHtml(v.get("id")) %>">

                            <%= escaparHtml(v.get("placa")) %>
                            ·
                            <%= escaparHtml(v.get("marca")) %>

                        </option>

                        <% } %>

                    </select>
                </label>

                <label>
                    Mecanico

                    <select name="mecanico_id" required>

                        <% for (Map<String, Object> m : ms) { %>

                        <option value="<%= escaparHtml(m.get("id")) %>">
                            <%= escaparHtml(m.get("nombre")) %>
                        </option>

                        <% } %>

                    </select>
                </label>

                <label>
                    Problema

                    <input name="problema"
                           required
                           placeholder="Descripcion del problema">
                </label>

                <button class="btn" type="submit">
                    CREAR ORDEN
                </button>

            </form>

        </section>

        <!-- ORDENES REGISTRADAS -->

        <section class="caja">

            <h2>ORDENES REGISTRADAS</h2>

            <div class="tabla">

                <table>

                    <thead>
                        <tr>
                            <th>#</th>
                            <th>PLACA</th>
                            <th>MECANICO</th>
                            <th>PROBLEMA</th>
                            <th>ESTADO</th>
                            <th>TOTAL</th>
                            <th>ACCIONES</th>
                        </tr>
                    </thead>

                    <tbody>

                        <% for (Map<String, Object> orden : os) {

                            String estado =
                                    String.valueOf(orden.get("estado"));

                            int id =
                                    ((Number) orden.get("id")).intValue();
                        %>

                        <tr>

                            <td><%= id %></td>

                            <td>
                                <%= escaparHtml(orden.get("placa")) %>
                            </td>

                            <td>
                                <%= escaparHtml(orden.get("mecanico")) %>
                            </td>

                            <td>
                                <%= escaparHtml(orden.get("problema")) %>
                            </td>

                            <td>
                                <span class="estado">
                                    <%= escaparHtml(estado) %>
                                </span>
                            </td>

                            <td>
                                Q <%= escaparHtml(orden.get("total")) %>
                            </td>

                            <td>

                                <% if (estado.equals("PENDIENTE")) { %>

                                    <%= boton(ctx, id, "iniciar", "INICIAR") %>

                                <% } %>

                                <% if (estado.equals("EN PROCESO")) { %>

                                    <%= boton(ctx, id, "finalizar", "FINALIZAR") %>

                                <% } %>

                                <% if (estado.equals("PENDIENTE")
                                        || estado.equals("EN PROCESO")) { %>

                                    <%= boton(ctx, id, "cancelar", "CANCELAR") %>

                                <% } %>

                                <% if (estado.equals("FINALIZADA")) { %>

                                    <a class="mini"
                                       href="<%= escaparHtml(ctx) %>/comprobante?id=<%= id %>">

                                        COMPROBANTE

                                    </a>

                                <% } %>

                            </td>

                        </tr>

                        <% } %>

                    </tbody>

                </table>

            </div>

        </section>

        <!-- AGREGAR SERVICIO -->

        <section class="caja">

            <h2>AGREGAR SERVICIO A ORDEN EN PROCESO</h2>

            <form method="post"
                  action="<%= escaparHtml(ctx) %>/app"
                  class="formgrid">

                <input type="hidden"
                       name="modulo"
                       value="ordenes">

                <input type="hidden"
                       name="accion"
                       value="servicio">

                <label>
                    ID orden

                    <input type="number"
                           name="id"
                           min="1"
                           required>
                </label>

                <label>
                    Servicio

                    <select name="servicio_id">

                        <% for (Map<String, Object> servicio : ss) { %>

                        <option value="<%= escaparHtml(servicio.get("id")) %>">

                            <%= escaparHtml(servicio.get("descripcion")) %>
                            · Q<%= escaparHtml(servicio.get("costo")) %>

                        </option>

                        <% } %>

                    </select>
                </label>

                <button class="btn" type="submit">
                    AGREGAR SERVICIO
                </button>

            </form>

        </section>

        <!-- AGREGAR REPUESTO -->

        <section class="caja">

            <h2>AGREGAR REPUESTO A ORDEN EN PROCESO</h2>

            <form method="post"
                  action="<%= escaparHtml(ctx) %>/app"
                  class="formgrid">

                <input type="hidden"
                       name="modulo"
                       value="ordenes">

                <input type="hidden"
                       name="accion"
                       value="repuesto">

                <label>
                    ID orden

                    <input type="number"
                           name="id"
                           min="1"
                           required>
                </label>

                <label>
                    Repuesto

                    <select name="repuesto_id">

                        <% for (Map<String, Object> repuesto : rs) { %>

                        <option value="<%= escaparHtml(repuesto.get("id")) %>">

                            <%= escaparHtml(repuesto.get("nombre")) %>

                            · Disponible:

                            <%= escaparHtml(repuesto.get("existencia")) %>

                        </option>

                        <% } %>

                    </select>
                </label>

                <label>
                    Cantidad

                    <input type="number"
                           name="cantidad"
                           min="1"
                           required>
                </label>

                <button class="btn" type="submit">
                    ASIGNAR Y DESCONTAR
                </button>

            </form>

        </section>

        <!-- DETALLES SERVICIOS -->

        <section class="caja">

            <h2>DETALLES DE SERVICIOS</h2>

            <div class="tabla">

                <table>

                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>ORDEN</th>
                            <th>SERVICIO</th>
                            <th>COSTO</th>
                            <th>ACCION</th>
                        </tr>
                    </thead>

                    <tbody>

                        <% for (Map<String, Object> detalle : ds) { %>

                        <tr>

                            <td>
                                <%= escaparHtml(detalle.get("id")) %>
                            </td>

                            <td>
                                <%= escaparHtml(detalle.get("orden_id")) %>
                            </td>

                            <td>
                                <%= escaparHtml(detalle.get("descripcion")) %>
                            </td>

                            <td>
                                Q <%= escaparHtml(detalle.get("costo")) %>
                            </td>

                            <td>
                                <%= quitar(ctx, detalle, "quitarServicio") %>
                            </td>

                        </tr>

                        <% } %>

                    </tbody>

                </table>

            </div>

        </section>

        <!-- DETALLES REPUESTOS -->

        <section class="caja">

            <h2>DETALLES DE REPUESTOS</h2>

            <div class="tabla">

                <table>

                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>ORDEN</th>
                            <th>REPUESTO</th>
                            <th>CANTIDAD</th>
                            <th>PRECIO</th>
                            <th>ACCION</th>
                        </tr>
                    </thead>

                    <tbody>

                        <% for (Map<String, Object> detalle : dr) { %>

                        <tr>

                            <td>
                                <%= escaparHtml(detalle.get("id")) %>
                            </td>

                            <td>
                                <%= escaparHtml(detalle.get("orden_id")) %>
                            </td>

                            <td>
                                <%= escaparHtml(detalle.get("nombre")) %>
                            </td>

                            <td>
                                <%= escaparHtml(detalle.get("cantidad")) %>
                            </td>

                            <td>
                                Q <%= escaparHtml(detalle.get("precio")) %>
                            </td>

                            <td>
                                <%= quitar(ctx, detalle, "quitarRepuesto") %>
                            </td>

                        </tr>

                        <% } %>

                    </tbody>

                </table>

            </div>

        </section>

        <% } %>

        <!-- ============================================= -->
        <!-- REPORTES PDF -->
        <!-- ============================================= -->

        <% if (modulo.equals("reportes")) {

            Map<String, String> tipos =
                    (Map<String, String>)
                    request.getAttribute("tipos");

            if (tipos == null) {
                tipos = Collections.emptyMap();
            }
        %>

        <section class="caja">

            <h2>EXPORTAR REPORTES PDF</h2>

            <div class="reportes">

                <% for (Map.Entry<String, String> tipo : tipos.entrySet()) { %>

                <form action="<%= escaparHtml(ctx) %>/reporte"
                      method="get"
                      target="_blank">

                    <input type="hidden"
                           name="tipo"
                           value="<%= escaparHtml(tipo.getKey()) %>">

                    <h3>
                        <%= escaparHtml(tipo.getValue()) %>
                    </h3>

                    <% if (tipo.getKey().equals("historial")) { %>

                    <label>
                        Placa

                        <input name="placa"
                               required
                               placeholder="P123ABC">
                    </label>

                    <% } %>

                    <% if (tipo.getKey().equals("ingresos")) { %>

                    <label>
                        Desde

                        <input type="date"
                               name="desde"
                               required>
                    </label>

                    <label>
                        Hasta

                        <input type="date"
                               name="hasta"
                               required>
                    </label>

                    <% } %>

                    <button class="btn" type="submit">
                        DESCARGAR PDF ↗
                    </button>

                </form>

                <% } %>

            </div>

        </section>

        <% } %>

        <!-- ============================================= -->
        <!-- PIE DE PAGINA -->
        <!-- ============================================= -->

        <footer>
            EL ULTIMO ARRANQUE · SISTEMA DE TALLER · UMG
        </footer>

    </main>

</div>

<script src="<%= escaparHtml(ctx) %>/assets/app.js"></script>

</body>
</html>