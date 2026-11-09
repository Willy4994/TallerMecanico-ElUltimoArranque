package gt.umg.taller.controller;
import gt.umg.taller.config.Conexion;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@Controller
public class ReporteControlador {
 public static final Map<String,String> TIPOS=new LinkedHashMap<>();
 static { TIPOS.put("estados","Ordenes por estado");TIPOS.put("historial","Historial por placa");TIPOS.put("bajo","Inventario bajo (5 o menos)");TIPOS.put("mecanicos","Ordenes por mecanico");TIPOS.put("ingresos","Ingresos por fechas"); }
 @GetMapping("/reporte")
 public ResponseEntity<byte[]> reporte(@RequestParam String tipo,@RequestParam(required=false) String placa,@RequestParam(required=false) String desde,@RequestParam(required=false) String hasta) throws Exception {
  if(!TIPOS.containsKey(tipo)) return ResponseEntity.badRequest().build();
  String sql=switch(tipo){
   case "estados" -> "SELECT id,estado,fecha_creacion,total FROM ordenes_trabajo ORDER BY estado,id";
   case "historial" -> "SELECT v.placa,o.id,o.problema,o.estado,o.total FROM ordenes_trabajo o JOIN vehiculos v ON v.id=o.vehiculo_id WHERE v.placa=? ORDER BY o.id DESC";
   case "bajo" -> "SELECT nombre,precio,existencia FROM repuestos WHERE existencia<=5 ORDER BY existencia,nombre";
   case "mecanicos" -> "SELECT m.nombre AS mecanico,o.id,o.estado,o.total FROM ordenes_trabajo o JOIN mecanicos m ON m.id=o.mecanico_id ORDER BY m.nombre,o.id";
   default -> "SELECT id,fecha_finalizacion,total FROM ordenes_trabajo WHERE estado='FINALIZADA' AND DATE(fecha_finalizacion) BETWEEN ? AND ? ORDER BY fecha_finalizacion";
  };
  ByteArrayOutputStream out=new ByteArrayOutputStream();
  try(Connection c=Conexion.abrir();PreparedStatement p=c.prepareStatement(sql)){
   if("historial".equals(tipo))p.setString(1,placa);
   if("ingresos".equals(tipo)){p.setString(1,desde);p.setString(2,hasta);}
   try(ResultSet r=p.executeQuery()){
    Document doc=new Document(PageSize.A4);PdfWriter.getInstance(doc,out);doc.open();
    doc.add(new Paragraph("EL ULTIMO ARRANQUE",FontFactory.getFont(FontFactory.HELVETICA_BOLD,18)));
    doc.add(new Paragraph(TIPOS.get(tipo)));doc.add(new Paragraph(" "));
    ResultSetMetaData md=r.getMetaData();PdfPTable tabla=new PdfPTable(md.getColumnCount());tabla.setWidthPercentage(100);
    for(int i=1;i<=md.getColumnCount();i++)tabla.addCell(md.getColumnLabel(i));
    java.math.BigDecimal total=java.math.BigDecimal.ZERO;int n=0;
    while(r.next()){n++;for(int i=1;i<=md.getColumnCount();i++){Object v=r.getObject(i);tabla.addCell(v==null?"":v.toString());}if("ingresos".equals(tipo))total=total.add(r.getBigDecimal("total"));}
    doc.add(tabla);doc.add(new Paragraph("Registros: "+n));if("ingresos".equals(tipo))doc.add(new Paragraph("Ingresos totales: Q "+total));doc.close();
   }
  }
  return pdf(out,"reporte-"+tipo+".pdf");
 }
 static ResponseEntity<byte[]> pdf(ByteArrayOutputStream out,String nombre){return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+nombre+"\"").body(out.toByteArray());}
}
