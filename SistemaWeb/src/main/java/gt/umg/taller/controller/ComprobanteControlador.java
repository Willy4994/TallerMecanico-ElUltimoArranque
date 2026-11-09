package gt.umg.taller.controller;
import gt.umg.taller.config.Conexion;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.sql.*;
@Controller
public class ComprobanteControlador {
 @GetMapping("/comprobante")
 public ResponseEntity<byte[]> comprobante(@RequestParam int id) throws Exception {
  String sql="SELECT o.id,o.estado,o.fecha_finalizacion,o.total,o.problema,v.placa,v.marca,v.modelo,cl.nombre AS cliente,m.nombre AS mecanico FROM ordenes_trabajo o JOIN vehiculos v ON v.id=o.vehiculo_id JOIN clientes cl ON cl.id=v.cliente_id JOIN mecanicos m ON m.id=o.mecanico_id WHERE o.id=?";
  ByteArrayOutputStream out=new ByteArrayOutputStream();
  try(Connection c=Conexion.abrir();PreparedStatement p=c.prepareStatement(sql)){
   p.setInt(1,id);
   try(ResultSet r=p.executeQuery()){
    if(!r.next())return ResponseEntity.notFound().build();
    if(!"FINALIZADA".equals(r.getString("estado")))return ResponseEntity.status(HttpStatus.CONFLICT).build();
    Document doc=new Document();PdfWriter.getInstance(doc,out);doc.open();
    doc.add(new Paragraph("EL ULTIMO ARRANQUE",FontFactory.getFont(FontFactory.HELVETICA_BOLD,18)));
    doc.add(new Paragraph("COMPROBANTE DE REPARACION #"+id));
    doc.add(new Paragraph("Cliente: "+r.getString("cliente")));
    doc.add(new Paragraph("Vehiculo: "+r.getString("placa")+" / "+r.getString("marca")+" "+r.getString("modelo")));
    doc.add(new Paragraph("Mecanico: "+r.getString("mecanico")));
    doc.add(new Paragraph("Problema: "+r.getString("problema")));
    doc.add(new Paragraph("Fecha finalizacion: "+r.getString("fecha_finalizacion")));
    doc.add(new Paragraph(" "));
    PdfPTable tabla=new PdfPTable(3);tabla.setWidthPercentage(100);tabla.addCell("CONCEPTO");tabla.addCell("CANTIDAD");tabla.addCell("SUBTOTAL Q");
    try(PreparedStatement q=c.prepareStatement("SELECT s.descripcion,os.costo FROM orden_servicios os JOIN servicios s ON s.id=os.servicio_id WHERE os.orden_id=?")){
     q.setInt(1,id);try(ResultSet x=q.executeQuery()){while(x.next()){tabla.addCell(x.getString(1));tabla.addCell("1");tabla.addCell(x.getBigDecimal(2).toString());}}
    }
    try(PreparedStatement q=c.prepareStatement("SELECT rp.nombre,dr.cantidad,dr.precio*dr.cantidad AS subtotal FROM orden_repuestos dr JOIN repuestos rp ON rp.id=dr.repuesto_id WHERE dr.orden_id=?")){
     q.setInt(1,id);try(ResultSet x=q.executeQuery()){while(x.next()){tabla.addCell(x.getString(1));tabla.addCell(""+x.getInt(2));tabla.addCell(x.getBigDecimal(3).toString());}}
    }
    doc.add(tabla);doc.add(new Paragraph("TOTAL: Q "+r.getBigDecimal("total")));doc.close();
   }
  }
  return ReporteControlador.pdf(out,"comprobante-"+id+".pdf");
 }
}
