package gt.umg.taller.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    public static Connection abrir() throws SQLException {

        String url = System.getenv().getOrDefault(
            "TALLER_DB_URL",
            "jdbc:mysql://127.0.0.1:3306/db_taller_mecanico"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Guatemala"
        );

        String usuario = System.getProperty(
            "TALLER_DB_USER",
            System.getenv().getOrDefault("TALLER_DB_USER", "taller_app")
        );

        String password = System.getProperty(
            "TALLER_DB_PASSWORD",
            System.getenv().getOrDefault("TALLER_DB_PASSWORD", "")
        );

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "No se encontro el controlador JDBC de MySQL", e
            );
        }

        return DriverManager.getConnection(
            url, usuario, password
        );
    }
}