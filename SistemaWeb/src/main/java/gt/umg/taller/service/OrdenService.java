package gt.umg.taller.service;

import gt.umg.taller.config.Conexion;
import gt.umg.taller.model.OrdenTrabajo;

import java.sql.*;
import java.math.BigDecimal;

public class OrdenService {

    private String estado(Connection c, int id) throws SQLException {

        try (PreparedStatement p = c.prepareStatement(
                "SELECT estado FROM ordenes_trabajo WHERE id=? FOR UPDATE")) {

            p.setInt(1, id);

            try (ResultSet r = p.executeQuery()) {

                if (!r.next()) {
                    throw new SQLException("Orden no encontrada");
                }

                return r.getString(1);
            }
        }
    }

    private void exigir(String estado, String esperado) throws SQLException {

        if (!estado.equals(esperado)) {
            throw new SQLException(
                "La orden debe estar en estado " + esperado
            );
        }
    }

    public void crear(int vehiculo, int mecanico, String problema)
            throws SQLException {

        if (problema == null || problema.isBlank()) {
            throw new SQLException("Indique el problema");
        }

        String sql =
            "INSERT INTO ordenes_trabajo" +
            "(vehiculo_id,mecanico_id,problema) VALUES(?,?,?)";

        try (
            Connection c = Conexion.abrir();
            PreparedStatement p = c.prepareStatement(sql)
        ) {

            p.setInt(1, vehiculo);
            p.setInt(2, mecanico);
            p.setString(3, problema);

            p.executeUpdate();
        }
    }

    public void iniciar(int id) throws SQLException {

        try (Connection c = Conexion.abrir()) {

            c.setAutoCommit(false);

            try {

                exigir(estado(c, id), "PENDIENTE");

                actualizarEstado(c, id, "EN PROCESO");

                c.commit();

            } catch (Exception e) {

                c.rollback();

                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    public void servicio(int id, int servicio) throws SQLException {

        try (Connection c = Conexion.abrir()) {

            c.setAutoCommit(false);

            try {

                exigir(estado(c, id), "EN PROCESO");

                String sql =
                    "INSERT INTO orden_servicios" +
                    "(orden_id,servicio_id,costo) " +
                    "SELECT ?,id,costo FROM servicios WHERE id=?";

                try (PreparedStatement p = c.prepareStatement(sql)) {

                    p.setInt(1, id);
                    p.setInt(2, servicio);

                    if (p.executeUpdate() != 1) {
                        throw new SQLException(
                            "Servicio no encontrado"
                        );
                    }
                }

                c.commit();

            } catch (Exception e) {

                c.rollback();

                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    public void repuesto(int id, int repuesto, int cantidad)
            throws SQLException {

        if (cantidad <= 0) {
            throw new SQLException("Cantidad invalida");
        }

        try (Connection c = Conexion.abrir()) {

            c.setAutoCommit(false);

            try {

                exigir(estado(c, id), "EN PROCESO");

                BigDecimal precio;

                String sqlConsulta =
                    "SELECT precio,existencia FROM repuestos " +
                    "WHERE id=? FOR UPDATE";

                try (PreparedStatement p =
                        c.prepareStatement(sqlConsulta)) {

                    p.setInt(1, repuesto);

                    try (ResultSet r = p.executeQuery()) {

                        if (!r.next()) {
                            throw new SQLException(
                                "Repuesto no encontrado"
                            );
                        }

                        precio = r.getBigDecimal(1);

                        if (r.getInt(2) < cantidad) {
                            throw new SQLException(
                                "Inventario insuficiente"
                            );
                        }
                    }
                }

                String sqlActualizar =
                    "UPDATE repuestos " +
                    "SET existencia=existencia-? WHERE id=?";

                try (PreparedStatement p =
                        c.prepareStatement(sqlActualizar)) {

                    p.setInt(1, cantidad);
                    p.setInt(2, repuesto);

                    p.executeUpdate();
                }

                String sqlInsertar =
                    "INSERT INTO orden_repuestos" +
                    "(orden_id,repuesto_id,cantidad,precio) " +
                    "VALUES(?,?,?,?)";

                try (PreparedStatement p =
                        c.prepareStatement(sqlInsertar)) {

                    p.setInt(1, id);
                    p.setInt(2, repuesto);
                    p.setInt(3, cantidad);
                    p.setBigDecimal(4, precio);

                    p.executeUpdate();
                }

                c.commit();

            } catch (Exception e) {

                c.rollback();

                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    public void quitarRepuesto(int orden, int detalle)
            throws SQLException {

        try (Connection c = Conexion.abrir()) {

            c.setAutoCommit(false);

            try {

                exigir(estado(c, orden), "EN PROCESO");

                int repuesto;
                int cantidad;

                String sql =
                    "SELECT repuesto_id,cantidad " +
                    "FROM orden_repuestos " +
                    "WHERE id=? AND orden_id=? FOR UPDATE";

                try (PreparedStatement p = c.prepareStatement(sql)) {

                    p.setInt(1, detalle);
                    p.setInt(2, orden);

                    try (ResultSet r = p.executeQuery()) {

                        if (!r.next()) {
                            throw new SQLException(
                                "Detalle no encontrado"
                            );
                        }

                        repuesto = r.getInt(1);
                        cantidad = r.getInt(2);
                    }
                }

                String sqlDevolver =
                    "UPDATE repuestos " +
                    "SET existencia=existencia+? WHERE id=?";

                try (PreparedStatement p =
                        c.prepareStatement(sqlDevolver)) {

                    p.setInt(1, cantidad);
                    p.setInt(2, repuesto);

                    p.executeUpdate();
                }

                String sqlEliminar =
                    "DELETE FROM orden_repuestos WHERE id=?";

                try (PreparedStatement p =
                        c.prepareStatement(sqlEliminar)) {

                    p.setInt(1, detalle);

                    p.executeUpdate();
                }

                c.commit();

            } catch (Exception e) {

                c.rollback();

                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    public void quitarServicio(int orden, int detalle)
            throws SQLException {

        try (Connection c = Conexion.abrir()) {

            c.setAutoCommit(false);

            try {

                exigir(estado(c, orden), "EN PROCESO");

                String sql =
                    "DELETE FROM orden_servicios " +
                    "WHERE id=? AND orden_id=?";

                try (PreparedStatement p = c.prepareStatement(sql)) {

                    p.setInt(1, detalle);
                    p.setInt(2, orden);

                    if (p.executeUpdate() != 1) {
                        throw new SQLException(
                            "Servicio no encontrado"
                        );
                    }
                }

                c.commit();

            } catch (Exception e) {

                c.rollback();

                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    private OrdenTrabajo cargarOrdenTrabajo(Connection c, int id)
            throws SQLException {

        OrdenTrabajo orden = new OrdenTrabajo(
            id,
            estado(c, id)
        );

        String sqlServicios =
            "SELECT s.descripcion, os.costo " +
            "FROM orden_servicios os " +
            "JOIN servicios s ON s.id = os.servicio_id " +
            "WHERE os.orden_id = ?";

        try (PreparedStatement p =
                c.prepareStatement(sqlServicios)) {

            p.setInt(1, id);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {

                    orden.agregarServicio(
                        r.getString("descripcion"),
                        r.getBigDecimal("costo")
                    );
                }
            }
        }

        String sqlRepuestos =
            "SELECT r.nombre, d.cantidad, d.precio " +
            "FROM orden_repuestos d " +
            "JOIN repuestos r ON r.id = d.repuesto_id " +
            "WHERE d.orden_id = ?";

        try (PreparedStatement p =
                c.prepareStatement(sqlRepuestos)) {

            p.setInt(1, id);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {

                    orden.agregarRepuesto(
                        r.getString("nombre"),
                        r.getInt("cantidad"),
                        r.getBigDecimal("precio")
                    );
                }
            }
        }

        return orden;
    }

    public void finalizarOrden(int id) throws SQLException {

        try (Connection c = Conexion.abrir()) {

            c.setAutoCommit(false);

            try {

                exigir(estado(c, id), "EN PROCESO");

                OrdenTrabajo orden = cargarOrdenTrabajo(c, id);

                if (!orden.tieneServicios()) {
                    throw new SQLException(
                        "Debe registrar al menos un servicio"
                    );
                }

                BigDecimal total = orden.calcularTotal();

                String sql =
                    "UPDATE ordenes_trabajo " +
                    "SET estado='FINALIZADA', " +
                    "fecha_finalizacion=NOW(), " +
                    "total=? " +
                    "WHERE id=?";

                try (PreparedStatement p = c.prepareStatement(sql)) {

                    p.setBigDecimal(1, total);
                    p.setInt(2, id);

                    if (p.executeUpdate() != 1) {
                        throw new SQLException(
                            "No se pudo finalizar la orden"
                        );
                    }
                }

                c.commit();

            } catch (Exception e) {

                c.rollback();

                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    public void cancelar(int id) throws SQLException {

        try (Connection c = Conexion.abrir()) {

            c.setAutoCommit(false);

            try {

                String e = estado(c, id);

                if (!e.equals("PENDIENTE")
                        && !e.equals("EN PROCESO")) {

                    throw new SQLException(
                        "No se puede cancelar esta orden"
                    );
                }

                String sql =
                    "SELECT repuesto_id,cantidad " +
                    "FROM orden_repuestos WHERE orden_id=?";

                try (PreparedStatement p = c.prepareStatement(sql)) {

                    p.setInt(1, id);

                    try (ResultSet r = p.executeQuery()) {

                        while (r.next()) {

                            String sqlDevolver =
                                "UPDATE repuestos " +
                                "SET existencia=existencia+? " +
                                "WHERE id=?";

                            try (PreparedStatement q =
                                    c.prepareStatement(sqlDevolver)) {

                                q.setInt(1, r.getInt(2));
                                q.setInt(2, r.getInt(1));

                                q.executeUpdate();
                            }
                        }
                    }
                }

                actualizarEstado(c, id, "CANCELADA");

                c.commit();

            } catch (Exception ex) {

                c.rollback();

                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    private void actualizarEstado(
            Connection c,
            int id,
            String nuevo) throws SQLException {

        String sql =
            "UPDATE ordenes_trabajo SET estado=? WHERE id=?";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, nuevo);
            p.setInt(2, id);

            p.executeUpdate();
        }
    }
}