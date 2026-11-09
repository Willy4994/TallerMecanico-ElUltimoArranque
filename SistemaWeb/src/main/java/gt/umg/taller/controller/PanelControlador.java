package gt.umg.taller.controller;

import gt.umg.taller.config.Conexion;
import gt.umg.taller.dao.*;
import gt.umg.taller.model.*;
import gt.umg.taller.service.OrdenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.sql.*;
import java.util.*;

@Controller
public class PanelControlador {
 private final RegistroDAO dao = new RegistroDAO();
 private final OrdenService orden = new OrdenService();

 @GetMapping("/") public String inicio(){return "redirect:/app";}

 @GetMapping("/app")
 public String mostrar(@RequestParam(defaultValue="inicio") String modulo, Model model,
                       @RequestParam(required=false) String mensaje) {
  try {
   if (!Set.of("inicio","ordenes","reportes").contains(modulo) && !Tablas.MAPA.containsKey(modulo))
    throw new IllegalArgumentException("Modulo no permitido");
   model.addAttribute("modulo",modulo);
   model.addAttribute("definiciones",Tablas.MAPA);
   model.addAttribute("mensaje",mensaje);
   model.addAttribute("titulo", switch(modulo){case "inicio" -> "EL ULTIMO ARRANQUE";case "ordenes" -> "ORDENES DE TRABAJO";case "reportes" -> "REPORTES";default -> Tablas.MAPA.get(modulo).titulo().toUpperCase();});
   if (Tablas.MAPA.containsKey(modulo)) {
    model.addAttribute("def",Tablas.MAPA.get(modulo));
    model.addAttribute("filas",dao.listar(modulo));
   }
   if (modulo.equals("inicio") || modulo.equals("ordenes")) {
    var ordenes=consulta("SELECT o.id,v.placa,m.nombre AS mecanico,o.problema,o.estado,o.fecha_creacion,o.total FROM ordenes_trabajo o JOIN vehiculos v ON v.id=o.vehiculo_id JOIN mecanicos m ON m.id=o.mecanico_id ORDER BY o.id DESC");
    model.addAttribute("ordenes",ordenes);
    model.addAttribute("pendientes",ordenes.stream().filter(o->"PENDIENTE".equals(o.get("estado"))).count());
    model.addAttribute("proceso",ordenes.stream().filter(o->"EN PROCESO".equals(o.get("estado"))).count());
    model.addAttribute("finalizadas",ordenes.stream().filter(o->"FINALIZADA".equals(o.get("estado"))).count());
   }
   if (modulo.equals("ordenes")) {
    for(String t:List.of("vehiculos","mecanicos","servicios","repuestos")) model.addAttribute(t,dao.listar(t));
    model.addAttribute("detServicios",consulta("SELECT os.id,os.orden_id,s.descripcion,os.costo FROM orden_servicios os JOIN servicios s ON s.id=os.servicio_id ORDER BY os.id DESC"));
    model.addAttribute("detRepuestos",consulta("SELECT d.id,d.orden_id,r.nombre,d.cantidad,d.precio FROM orden_repuestos d JOIN repuestos r ON r.id=d.repuesto_id ORDER BY d.id DESC"));
   }
   if(modulo.equals("reportes")) model.addAttribute("tipos",ReporteControlador.TIPOS);
   if(modulo.equals("inicio")) {
    ResumenPersonas resumen=new ResumenPersonas();
    for(var fila:dao.listar("clientes")) resumen.agregar(new Cliente(String.valueOf(fila.get("nombre"))));
    for(var fila:dao.listar("mecanicos")) resumen.agregar(new Mecanico(String.valueOf(fila.get("nombre"))));
    model.addAttribute("resumenPersonas",resumen.obtenerResumen());
   }
   return "panel";
  }catch(Exception e){model.addAttribute("error",e.getMessage());return "error";}
 }

 @PostMapping("/app")
 public String accion(@RequestParam Map<String,String> datos, RedirectAttributes redirect) {
  String modulo=datos.getOrDefault("modulo","inicio");
  String accion=datos.getOrDefault("accion","");
  String mensaje="Operacion completada";
  try {
   if(Tablas.MAPA.containsKey(modulo)) {
    if("borrar".equals(accion)) dao.borrar(modulo,num(datos,"id"));
    else if("abastecer".equals(accion) && "repuestos".equals(modulo)) dao.abastecer(num(datos,"id"),num(datos,"cantidad"));
    else dao.guardar(modulo,datos,datos.get("id"));
   } else if("ordenes".equals(modulo)) {
    switch(accion) {
     case "crear" -> orden.crear(num(datos,"vehiculo_id"),num(datos,"mecanico_id"),datos.get("problema"));
     case "iniciar" -> orden.iniciar(num(datos,"id"));
     case "servicio" -> orden.servicio(num(datos,"id"),num(datos,"servicio_id"));
     case "repuesto" -> orden.repuesto(num(datos,"id"),num(datos,"repuesto_id"),num(datos,"cantidad"));
     case "quitarRepuesto" -> orden.quitarRepuesto(num(datos,"id"),num(datos,"detalle"));
     case "quitarServicio" -> orden.quitarServicio(num(datos,"id"),num(datos,"detalle"));
     case "finalizar" -> orden.finalizarOrden(num(datos,"id"));
     case "cancelar" -> orden.cancelar(num(datos,"id"));
     default -> throw new IllegalArgumentException("Accion desconocida");
    }
   }else throw new IllegalArgumentException("Modulo no permitido");
  }catch(Exception e){mensaje="Error: "+e.getMessage();}
  redirect.addAttribute("modulo",modulo).addAttribute("mensaje",mensaje);
  return "redirect:/app";
 }
 private int num(Map<String,String> datos,String campo){return Integer.parseInt(datos.get(campo));}
 private List<Map<String,Object>> consulta(String sql) throws SQLException {
  List<Map<String,Object>> lista=new ArrayList<>();
  try(Connection c=Conexion.abrir();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()) {
   ResultSetMetaData md=r.getMetaData();
   while(r.next()){Map<String,Object> fila=new LinkedHashMap<>();for(int i=1;i<=md.getColumnCount();i++)fila.put(md.getColumnLabel(i),r.getObject(i));lista.add(fila);}
  }
  return lista;
 }
}
