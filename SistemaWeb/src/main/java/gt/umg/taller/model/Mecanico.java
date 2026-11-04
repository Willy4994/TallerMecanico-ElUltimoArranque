package gt.umg.taller.model;

public class Mecanico extends Persona {

    public Mecanico(String nombre) {
        super(nombre);
    }

    @Override
    public String tipo() {
        return "MECANICO";
    }
}
