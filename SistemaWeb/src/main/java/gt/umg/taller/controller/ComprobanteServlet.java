package gt.umg.taller.controller;

import gt.umg.taller.config.Conexion;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.*;
import java.sql.*;

@WebServlet("/comprobante")
public class ComprobanteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (Connection c = Conexion.abrir(); PreparedStatement p = c.prepareStatement("SELECT o.id,o.estado,o.fecha_finalizacion,o.total,o.problema,v.placa,v.marca,v.modelo,cl.nombre AS cliente,m.nombre AS mecanico FROM ordenes_trabajo o JOIN vehiculos v ON v.id=o.vehiculo_id JOIN clientes cl ON cl.id=v.cliente_id JOIN mecanicos m ON m.id=o.mecanico_id WHERE o.id=?")) {
            int id = Integer.parseInt(req.getParameter("id"));
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    resp.sendError(404);
                    return;
                }
                if (!"FINALIZADA".equals(r.getString("estado"))) {
                    resp.sendError(409, "Solo ordenes finalizadas");
                    return;
                }
                resp.setContentType("application/pdf");
                resp.setHeader("Content-Disposition", "attachment; filename=comprobante-" + id + ".pdf");
                Document d = new Document();
                PdfWriter.getInstance(d, resp.getOutputStream());
                d.open();
                d.add(new Paragraph("EL ULTIMO ARRANQUE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                d.add(new Paragraph("COMPROBANTE DE REPARACION #" + id));
                d.add(new Paragraph("Cliente: " + r.getString("cliente")));
                d.add(new Paragraph("Vehiculo: " + r.getString("placa") + " / " + r.getString("marca") + " " + r.getString("modelo")));
                d.add(new Paragraph("Mecanico: " + r.getString("mecanico")));
                d.add(new Paragraph("Problema: " + r.getString("problema")));
                d.add(new Paragraph("Fecha finalizacion: " + r.getString("fecha_finalizacion")));
                d.add(new Paragraph(" "));
                PdfPTable t = new PdfPTable(3);
                t.setWidthPercentage(100);
                t.addCell("CONCEPTO");
                t.addCell("CANTIDAD");
                t.addCell("SUBTOTAL Q");
                try (PreparedStatement q = c.prepareStatement("SELECT s.descripcion,os.costo FROM orden_servicios os JOIN servicios s ON s.id=os.servicio_id WHERE os.orden_id=?")) {
                    q.setInt(1, id);
                    try (ResultSet x = q.executeQuery()) {
                        while (x.next()) {
                            t.addCell(x.getString(1));
                            t.addCell("1");
                            t.addCell(x.getBigDecimal(2).toString());
                        }
                    }
                }
                try (PreparedStatement q = c.prepareStatement("SELECT rp.nombre,dr.cantidad,dr.precio*dr.cantidad AS subtotal FROM orden_repuestos dr JOIN repuestos rp ON rp.id=dr.repuesto_id WHERE dr.orden_id=?")) {
                    q.setInt(1, id);
                    try (ResultSet x = q.executeQuery()) {
                        while (x.next()) {
                            t.addCell(x.getString(1));
                            t.addCell("" + x.getInt(2));
                            t.addCell(x.getBigDecimal(3).toString());
                        }
                    }
                }
                d.add(t);
                d.add(new Paragraph("TOTAL: Q " + r.getBigDecimal("total")));
                d.close();
            }
        } catch (Exception e) {
            if (!resp.isCommitted()) {
                resp.sendError(500, e.getMessage());
            }
        }
    }
}
