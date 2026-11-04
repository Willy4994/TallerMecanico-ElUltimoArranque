package gt.umg.taller.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class DetalleRepuesto {

    private final String nombre;
    private final int cantidad;
    private final BigDecimal precio;

    public DetalleRepuesto(
            String nombre,
            int cantidad,
            BigDecimal precio) {

        this.nombre = Objects.requireNonNull(nombre);
        this.precio = Objects.requireNonNull(precio);

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                "La cantidad debe ser mayor que cero"
            );
        }

        if (precio.signum() < 0) {
            throw new IllegalArgumentException(
                "El precio no puede ser negativo"
            );
        }

        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getSubtotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}