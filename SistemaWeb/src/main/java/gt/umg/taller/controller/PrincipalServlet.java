package gt.umg.taller.controller;

import gt.umg.taller.config.Conexion;
import gt.umg.taller.dao.*;
import gt.umg.taller.service.OrdenService;
import gt.umg.taller.model.Cliente;
import gt.umg.taller.model.Mecanico;
import gt.umg.taller.model.ResumenPersonas;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet("/app")
public class PrincipalServlet extends HttpServlet {

    private final RegistroDAO dao = new RegistroDAO();
    private final OrdenService orden = new OrdenService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String modulo = req.getParameter("modulo");

        if (modulo == null) {
            modulo = "inicio";
        }

        try {

            req.setAttribute("modulo", modulo);
            req.setAttribute("definiciones", Tablas.MAPA);

            if (Tablas.MAPA.containsKey(modulo)) {

                req.setAttribute(
                    "def",
                    Tablas.MAPA.get(modulo)
                );

                req.setAttribute(
                    "filas",
                    dao.listar(modulo)
                );
            }

            if (modulo.equals("ordenes") || modulo.equals("inicio")) {

                req.setAttribute(
                    "ordenes",
                    consulta(
                        "SELECT o.id, v.placa, m.nombre AS mecanico, " +
                        "o.problema, o.estado, o.fecha_creacion, o.total " +
                        "FROM ordenes_trabajo o " +
                        "JOIN vehiculos v ON v.id = o.vehiculo_id " +
                        "JOIN mecanicos m ON m.id = o.mecanico_id " +
                        "ORDER BY o.id DESC"
                    )
                );
            }

            if (modulo.equals("ordenes")) {

                req.setAttribute(
                    "vehiculos",
                    dao.listar("vehiculos")
                );

                req.setAttribute(
                    "mecanicos",
                    dao.listar("mecanicos")
                );

                req.setAttribute(
                    "servicios",
                    dao.listar("servicios")
                );

                req.setAttribute(
                    "repuestos",
                    dao.listar("repuestos")
                );

                req.setAttribute(
                    "detServicios",
                    consulta(
                        "SELECT os.id, os.orden_id, s.descripcion, os.costo " +
                        "FROM orden_servicios os " +
                        "JOIN servicios s ON s.id = os.servicio_id " +
                        "ORDER BY os.id DESC"
                    )
                );

                req.setAttribute(
                    "detRepuestos",
                    consulta(
                        "SELECT d.id, d.orden_id, r.nombre, d.cantidad, d.precio " +
                        "FROM orden_repuestos d " +
                        "JOIN repuestos r ON r.id = d.repuesto_id " +
                        "ORDER BY d.id DESC"
                    )
                );
            }

            if (modulo.equals("reportes")) {

                req.setAttribute(
                    "tipos",
                    ReportesServlet.TIPOS
                );
            }

            // FUNCIONALIDAD POO:
            // HERENCIA, POLIMORFISMO Y COLECCIONES

            if (modulo.equals("inicio")) {

                ResumenPersonas resumen = new ResumenPersonas();

                List<Map<String, Object>> clientes =
                        dao.listar("clientes");

                List<Map<String, Object>> mecanicos =
                        dao.listar("mecanicos");

                for (Map<String, Object> fila : clientes) {

                    String nombre = String.valueOf(
                        fila.get("nombre")
                    );

                    Cliente cliente = new Cliente(nombre);

                    resumen.agregar(cliente);
                }

                for (Map<String, Object> fila : mecanicos) {

                    String nombre = String.valueOf(
                        fila.get("nombre")
                    );

                    Mecanico mecanico = new Mecanico(nombre);

                    resumen.agregar(mecanico);
                }

                req.setAttribute(
                    "resumenPersonas",
                    resumen.obtenerResumen()
                );
            }

            req.getRequestDispatcher(
                "/WEB-INF/vistas/panel.jsp"
            ).forward(req, resp);

        } catch (Exception e) {

            req.setAttribute(
                "error",
                e.getMessage()
            );

            req.getRequestDispatcher(
                "/WEB-INF/vistas/error.jsp"
            ).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");

        String modulo = req.getParameter("modulo");

        if (modulo == null) {
            modulo = "inicio";
        }

        String accion = req.getParameter("accion");

        String mensaje = "Operacion completada";

        try {

            if (Tablas.MAPA.containsKey(modulo)) {

                if ("borrar".equals(accion)) {

                    dao.borrar(
                        modulo,
                        num(req, "id")
                    );

                } else if (
                    "abastecer".equals(accion)
                    && modulo.equals("repuestos")
                ) {

                    dao.abastecer(
                        num(req, "id"),
                        num(req, "cantidad")
                    );

                } else {

                    Map<String, String> datos = new HashMap<>();

                    for (Tablas.Campo f :
                            Tablas.MAPA.get(modulo).campos()) {

                        datos.put(
                            f.nombre(),
                            req.getParameter(f.nombre())
                        );
                    }

                    dao.guardar(
                        modulo,
                        datos,
                        req.getParameter("id")
                    );
                }

            } else if (modulo.equals("ordenes")) {

                switch (accion) {

                    case "crear" ->
                        orden.crear(
                            num(req, "vehiculo_id"),
                            num(req, "mecanico_id"),
                            req.getParameter("problema")
                        );

                    case "iniciar" ->
                        orden.iniciar(
                            num(req, "id")
                        );

                    case "servicio" ->
                        orden.servicio(
                            num(req, "id"),
                            num(req, "servicio_id")
                        );

                    case "repuesto" ->
                        orden.repuesto(
                            num(req, "id"),
                            num(req, "repuesto_id"),
                            num(req, "cantidad")
                        );

                    case "quitarRepuesto" ->
                        orden.quitarRepuesto(
                            num(req, "id"),
                            num(req, "detalle")
                        );

                    case "quitarServicio" ->
                        orden.quitarServicio(
                            num(req, "id"),
                            num(req, "detalle")
                        );

                    case "finalizar" ->
                        orden.finalizarOrden(
                            num(req, "id")
                        );

                    case "cancelar" ->
                        orden.cancelar(
                            num(req, "id")
                        );

                    default ->
                        throw new IllegalArgumentException(
                            "Accion desconocida"
                        );
                }
            }

        } catch (Exception e) {

            mensaje = "Error: " + e.getMessage();
        }

        resp.sendRedirect(
            req.getContextPath()
            + "/app?modulo="
            + java.net.URLEncoder.encode(modulo, "UTF-8")
            + "&mensaje="
            + java.net.URLEncoder.encode(mensaje, "UTF-8")
        );
    }

    private int num(HttpServletRequest r, String k) {

        return Integer.parseInt(
            r.getParameter(k)
        );
    }

    private List<Map<String, Object>> consulta(String sql)
            throws SQLException {

        List<Map<String, Object>> lista = new ArrayList<>();

        try (
            Connection c = Conexion.abrir();
            PreparedStatement p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery()
        ) {

            ResultSetMetaData m = r.getMetaData();

            while (r.next()) {

                Map<String, Object> fila = new LinkedHashMap<>();

                for (int i = 1; i <= m.getColumnCount(); i++) {

                    fila.put(
                        m.getColumnLabel(i),
                        r.getObject(i)
                    );
                }

                lista.add(fila);
            }
        }

        return lista;
    }
}