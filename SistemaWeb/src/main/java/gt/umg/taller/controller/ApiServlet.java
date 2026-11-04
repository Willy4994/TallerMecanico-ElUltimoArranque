package gt.umg.taller.controller;

import gt.umg.taller.config.Conexion;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

@WebServlet("/api/inventario-bajo")
public class ApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try (Connection c = Conexion.abrir(); PreparedStatement p = c.prepareStatement("SELECT nombre,existencia FROM repuestos WHERE existencia<=5 ORDER BY existencia,nombre"); ResultSet r = p.executeQuery()) {
            StringBuilder s = new StringBuilder("[");
            boolean primero = true;
            while (r.next()) {
                if (!primero) {
                    s.append(',');
                }
                primero = false;
                s.append("{\"nombre\":\"").append(esc(r.getString(1))).append("\",\"existencia\":").append(r.getInt(2)).append('}');
            }
            s.append(']');
            resp.getWriter().write(s.toString());
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"Error de base de datos\"}");
        }
    }

    private String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
