package gt.umg.taller.dao;

import gt.umg.taller.config.Conexion;
import java.sql.*;
import java.util.*;

public class RegistroDAO {

    public List<Map<String, Object>> listar(String tabla) throws SQLException {
        Tablas.Definicion d = validar(tabla);
        List<Map<String, Object>> datos = new ArrayList<>();
        try (Connection c = Conexion.abrir(); PreparedStatement p = c.prepareStatement("SELECT * FROM " + d.tabla() + " ORDER BY id DESC"); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                for (String col : d.columnas()) {
                    fila.put(col, r.getObject(col));
                }
                datos.add(fila);
            }
        }
        return datos;
    }

    public void guardar(String tabla, Map<String, String> datos, String id) throws SQLException {
        Tablas.Definicion d = validar(tabla);
        boolean editar = id != null && !id.isBlank();
        String campos = String.join(",", d.campos().stream().map(Tablas.Campo::nombre).toList());
        String placeholders = String.join(",", Collections.nCopies(d.campos().size(), "?"));
        String sql = editar ? "UPDATE " + tabla + " SET " + String.join(",", d.campos().stream().map(f -> f.nombre() + "=?").toList()) + " WHERE id=?" : "INSERT INTO " + tabla + " (" + campos + ") VALUES (" + placeholders + ")";
        try (Connection c = Conexion.abrir(); PreparedStatement p = c.prepareStatement(sql)) {
            int i = 1;
            for (Tablas.Campo f : d.campos()) {
                String valor = datos.getOrDefault(f.nombre(), "").trim();
                if (valor.isEmpty()) {
                    p.setNull(i++, Types.NULL);
                } else if (f.tipo().equals("number")) {
                    p.setInt(i++, Integer.parseInt(valor));
                } else if (f.tipo().equals("decimal")) {
                    p.setBigDecimal(i++, new java.math.BigDecimal(valor));
                } else {
                    p.setString(i++, valor);
                }
            }
            if (editar) {
                p.setInt(i, Integer.parseInt(id));
            }
            p.executeUpdate();
        }
    }

    public void borrar(String tabla, int id) throws SQLException {
        validar(tabla);
        try (Connection c = Conexion.abrir(); PreparedStatement p = c.prepareStatement("DELETE FROM " + tabla + " WHERE id=?")) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }

    public void abastecer(int id, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        try (Connection c = Conexion.abrir(); PreparedStatement p = c.prepareStatement("UPDATE repuestos SET existencia=existencia+? WHERE id=?")) {
            p.setInt(1, cantidad);
            p.setInt(2, id);
            if (p.executeUpdate() != 1) {
                throw new SQLException("Repuesto no encontrado");
            }
        }
    }

    private Tablas.Definicion validar(String tabla) {
        Tablas.Definicion d = Tablas.MAPA.get(tabla);
        if (d == null) {
            throw new IllegalArgumentException("Modulo no permitido");
        }
        return d;
    }
}
