package gt.umg.taller.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OrdenTrabajo {

    private final int id;
    private final String estado;

    private final List<DetalleServicio> servicios;
    private final List<DetalleRepuesto> repuestos;

    public OrdenTrabajo(int id, String estado) {

        if (id <= 0) {
            throw new IllegalArgumentException("ID de orden invalido");
        }

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("Estado invalido");
        }

        this.id = id;
        this.estado = estado;

        this.servicios = new ArrayList<>();
        this.repuestos = new ArrayList<>();
    }

    public void agregarServicio(String descripcion, BigDecimal costo) {
        servicios.add(new DetalleServicio(descripcion, costo));
    }

    public void agregarRepuesto(
            String nombre,
            int cantidad,
            BigDecimal precio) {

        repuestos.add(
            new DetalleRepuesto(nombre, cantidad, precio)
        );
    }

    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public List<DetalleServicio> getServicios() {
        return Collections.unmodifiableList(servicios);
    }

    public List<DetalleRepuesto> getRepuestos() {
        return Collections.unmodifiableList(repuestos);
    }

    public boolean tieneServicios() {
        return !servicios.isEmpty();
    }

    public BigDecimal calcularTotal() {

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleServicio servicio : servicios) {
            total = total.add(servicio.getCosto());
        }

        for (DetalleRepuesto repuesto : repuestos) {
            total = total.add(repuesto.getSubtotal());
        }

        return total;
    }
}