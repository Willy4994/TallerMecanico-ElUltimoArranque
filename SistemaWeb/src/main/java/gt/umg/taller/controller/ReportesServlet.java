package gt.umg.taller.controller;

import gt.umg.taller.config.Conexion;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet("/reporte")
public class ReportesServlet extends HttpServlet {

    public static final Map<String, String> TIPOS = new LinkedHashMap<>();

    static {
        TIPOS.put("estados", "Ordenes por estado");
        TIPOS.put("historial", "Historial por placa");
        TIPOS.put("bajo", "Inventario bajo (5 o menos)");
        TIPOS.put("mecanicos", "Ordenes por mecanico");
        TIPOS.put("ingresos", "Ingresos por fechas");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String tipo = req.getParameter("tipo");
        if (!TIPOS.containsKey(tipo)) {
            resp.sendError(400, "Reporte desconocido");
            return;
        }
        String sql = switch (tipo) {
            case "estados" ->
                "SELECT id,estado,fecha_creacion,total FROM ordenes_trabajo ORDER BY estado,id";
            case "historial" ->
                "SELECT v.placa,o.id,o.problema,o.estado,o.total FROM ordenes_trabajo o JOIN vehiculos v ON v.id=o.vehiculo_id WHERE v.placa=? ORDER BY o.id DESC";
            case "bajo" ->
                "SELECT nombre,precio,existencia FROM repuestos WHERE existencia<=5 ORDER BY existencia,nombre";
            case "mecanicos" ->
                "SELECT m.nombre AS mecanico,o.id,o.estado,o.total FROM ordenes_trabajo o JOIN mecanicos m ON m.id=o.mecanico_id ORDER BY m.nombre,o.id";
            default ->
                "SELECT id,fecha_finalizacion,total FROM ordenes_trabajo WHERE estado='FINALIZADA' AND DATE(fecha_finalizacion) BETWEEN ? AND ? ORDER BY fecha_finalizacion";
        };
        try (Connection c = Conexion.abrir(); PreparedStatement p = c.prepareStatement(sql)) {
            if (tipo.equals("historial")) {
                p.setString(1, req.getParameter("placa"));
            }
            if (tipo.equals("ingresos")) {
                p.setString(1, req.getParameter("desde"));
                p.setString(2, req.getParameter("hasta"));
            }
            try (ResultSet r = p.executeQuery()) {
                resp.setContentType("application/pdf");
                resp.setHeader("Content-Disposition", "attachment; filename=reporte-" + tipo + ".pdf");
                Document doc = new Document(PageSize.A4);
                PdfWriter.getInstance(doc, resp.getOutputStream());
                doc.open();
                doc.add(new Paragraph("EL ULTIMO ARRANQUE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                doc.add(new Paragraph(TIPOS.get(tipo)));
                doc.add(new Paragraph(" "));
                ResultSetMetaData md = r.getMetaData();
                PdfPTable t = new PdfPTable(md.getColumnCount());
                t.setWidthPercentage(100);
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    t.addCell(md.getColumnLabel(i));
                }
                java.math.BigDecimal total = java.math.BigDecimal.ZERO;
                int n = 0;
                while (r.next()) {
                    n++;
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        Object v = r.getObject(i);
                        t.addCell(v == null ? "" : v.toString());
                    }
                    if (tipo.equals("ingresos")) {
                        total = total.add(r.getBigDecimal("total"));
                    }
                }
                doc.add(t);
                doc.add(new Paragraph("Registros: " + n));
                if (tipo.equals("ingresos")) {
                    doc.add(new Paragraph("Ingresos totales: Q " + total));
                }
                doc.close();
            }
        } catch (Exception e) {
            if (!resp.isCommitted()) {
                resp.sendError(500, e.getMessage());
            }
        }
    }
}
