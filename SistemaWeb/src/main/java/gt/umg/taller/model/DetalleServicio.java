package gt.umg.taller.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class DetalleServicio {

    private final String descripcion;
    private final BigDecimal costo;

    public DetalleServicio(String descripcion, BigDecimal costo) {
        this.descripcion = Objects.requireNonNull(descripcion);
        this.costo = Objects.requireNonNull(costo);

        if (costo.signum() < 0) {
            throw new IllegalArgumentException(
                "El costo del servicio no puede ser negativo"
            );
        }
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getCosto() {
        return costo;
    }
}