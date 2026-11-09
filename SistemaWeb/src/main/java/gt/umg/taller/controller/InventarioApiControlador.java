package gt.umg.taller.controller;

import gt.umg.taller.config.Conexion;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.*;

@RestController
public class InventarioApiControlador {

    @GetMapping("/api/inventario")
    public List<Map<String, Object>> inventario() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        try (Connection c = Conexion.abrir(); PreparedStatement p = c.prepareStatement("SELECT id,nombre,precio,existencia FROM repuestos WHERE existencia<=5 ORDER BY existencia ASC"); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("id", r.getInt("id"));
                fila.put("nombre", r.getString("nombre"));
                fila.put("precio", r.getBigDecimal("precio"));
                fila.put("existencia", r.getInt("existencia"));
                lista.add(fila);
            }
        }
        return lista;
    }

    @GetMapping("/api/inventario-bajo")
    public List<Map<String, Object>> inventarioBajo() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (var fila : inventario()) {
            Map<String, Object> x = new LinkedHashMap<>();
            x.put("nombre", fila.get("nombre"));
            x.put("existencia", fila.get("existencia"));
            lista.add(x);
        }
        return lista;
    }
}
