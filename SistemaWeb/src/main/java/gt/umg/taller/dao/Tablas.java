package gt.umg.taller.dao;

import java.util.*;

public class Tablas {

    public record Campo(String nombre, String etiqueta, String tipo) {

    }

    public record Definicion(String tabla, String titulo, String[] columnas, List<Campo> campos) {

    }
    public static final Map<String, Definicion> MAPA = new LinkedHashMap<>();

    static {
        agregar("clientes", "Clientes", new String[]{"id", "identificador", "nombre", "telefono", "correo"}, new Campo("identificador", "Identificador", "text"), new Campo("nombre", "Nombre", "text"), new Campo("telefono", "Telefono", "text"), new Campo("correo", "Correo", "email"));
        agregar("vehiculos", "Vehiculos", new String[]{"id", "cliente_id", "placa", "marca", "modelo", "anio"}, new Campo("cliente_id", "ID cliente", "number"), new Campo("placa", "Placa", "text"), new Campo("marca", "Marca", "text"), new Campo("modelo", "Modelo", "text"), new Campo("anio", "Anio", "number"));
        agregar("mecanicos", "Mecanicos", new String[]{"id", "nombre", "telefono", "especialidad"}, new Campo("nombre", "Nombre", "text"), new Campo("telefono", "Telefono", "text"), new Campo("especialidad", "Especialidad", "text"));
        agregar("repuestos", "Repuestos", new String[]{"id", "nombre", "descripcion", "precio", "existencia"}, new Campo("nombre", "Nombre", "text"), new Campo("descripcion", "Descripcion", "text"), new Campo("precio", "Precio Q", "decimal"), new Campo("existencia", "Existencia", "number"));
        agregar("servicios", "Servicios", new String[]{"id", "descripcion", "costo"}, new Campo("descripcion", "Descripcion", "text"), new Campo("costo", "Mano de obra Q", "decimal"));
    }

    static void agregar(String tabla, String titulo, String[] columnas, Campo... campos) {
        MAPA.put(tabla, new Definicion(tabla, titulo, columnas, List.of(campos)));
    }
}
