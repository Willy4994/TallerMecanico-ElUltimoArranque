package gt.umg.taller.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/inventario")
public class InventarioApiServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String usuario = System.getenv("TALLER_DB_USER");
        String password = System.getenv("TALLER_DB_PASSWORD");

        String url =
            "jdbc:mysql://127.0.0.1:3306/db_taller_mecanico"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Guatemala";

        if (usuario == null || password == null) {
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Configuracion de base de datos incompleta"
            );
            return;
        }

        String sql =
            "SELECT id, nombre, precio, existencia "
            + "FROM repuestos "
            + "WHERE existencia <= ? "
            + "ORDER BY existencia ASC";

        try (
            Connection conexion = DriverManager.getConnection(
                url, usuario, password
            );

            PreparedStatement ps =
                conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, 5);

            try (
                ResultSet rs = ps.executeQuery();
                PrintWriter out = response.getWriter()
            ) {

                StringBuilder json = new StringBuilder("[");

                boolean primero = true;

                while (rs.next()) {

                    if (!primero) {
                        json.append(",");
                    }

                    primero = false;

                    json.append("{")
                        .append("\"id\":")
                        .append(rs.getInt("id"))
                        .append(",")

                        .append("\"nombre\":\"")
                        .append(escapar(rs.getString("nombre")))
                        .append("\",")

                        .append("\"precio\":")
                        .append(rs.getBigDecimal("precio"))
                        .append(",")

                        .append("\"existencia\":")
                        .append(rs.getInt("existencia"))

                        .append("}");
                }

                json.append("]");

                out.print(json);
            }

        } catch (Exception e) {

            getServletContext().log(
                "Error al consultar inventario", e
            );

            if (!response.isCommitted()) {
                response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al consultar inventario"
                );
            }
        }
    }

    private String escapar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}