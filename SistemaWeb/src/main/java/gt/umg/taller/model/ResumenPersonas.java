package gt.umg.taller.model;

import java.util.ArrayList;
import java.util.List;

public class ResumenPersonas {

    private final List<Persona> personas;

    public ResumenPersonas() {
        this.personas = new ArrayList<>();
    }

    public void agregar(Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("La persona no puede ser nula");
        }

        personas.add(persona);
    }

    public List<String> obtenerResumen() {
        List<String> resultado = new ArrayList<>();

        for (Persona persona : personas) {
            resultado.add(
                persona.tipo() + ": " + persona.getNombre()
            );
        }

        return resultado;
    }
}