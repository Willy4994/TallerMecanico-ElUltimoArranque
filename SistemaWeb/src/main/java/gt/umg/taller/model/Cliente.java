package gt.umg.taller.model;

public class Cliente extends Persona {

    public Cliente(String nombre) {
        super(nombre);
    }

    @Override
    public String tipo() {
        return "CLIENTE";
    }
}
