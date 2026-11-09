package gt.umg.taller.mobile;

import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    // Dirección de nuestra API REST en Tomcat.
    // Con adb reverse, 127.0.0.1:8080 apunta al servidor Spring Boot de la Mac
    // desde el emulador Android.

    private static final String API_URL =
            "http://127.0.0.1:8080/SistemaWeb/api/inventario";

    private Button btnActualizar;
    private TextView txtEstado;
    private LinearLayout contenedorInventario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnActualizar = findViewById(R.id.btnActualizar);
        txtEstado = findViewById(R.id.txtEstado);
        contenedorInventario =
                findViewById(R.id.contenedorInventario);

        btnActualizar.setOnClickListener(v -> cargarInventario());

        // Consultar automáticamente al abrir la aplicación
        cargarInventario();
    }

    private void cargarInventario() {

        txtEstado.setText("Conectando con el taller...");
        btnActualizar.setEnabled(false);

        new Thread(() -> {

            HttpURLConnection conexion = null;

            try {

                URL url = new URL(API_URL);

                conexion =
                        (HttpURLConnection) url.openConnection();

                conexion.setRequestMethod("GET");
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(10000);
                conexion.setRequestProperty(
                        "Accept", "application/json"
                );

                int codigo = conexion.getResponseCode();

                if (codigo != HttpURLConnection.HTTP_OK) {
                    throw new Exception(
                            "Error HTTP: " + codigo
                    );
                }

                BufferedReader lector =
                        new BufferedReader(
                                new InputStreamReader(
                                        conexion.getInputStream(),
                                        "UTF-8"
                                )
                        );

                StringBuilder respuesta =
                        new StringBuilder();

                String linea;

                while ((linea = lector.readLine()) != null) {
                    respuesta.append(linea);
                }

                lector.close();

                JSONArray inventario =
                        new JSONArray(respuesta.toString());

                runOnUiThread(() -> {

                    mostrarInventario(inventario);

                    btnActualizar.setEnabled(true);

                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    txtEstado.setText(
                            "No se pudo conectar con el servidor.\n"
                                    + e.getMessage()
                    );

                    btnActualizar.setEnabled(true);

                });

            } finally {

                if (conexion != null) {
                    conexion.disconnect();
                }

            }

        }).start();
    }

    private void mostrarInventario(JSONArray inventario) {

        contenedorInventario.removeAllViews();

        txtEstado.setText(
                "Repuestos encontrados: " + inventario.length()
        );

        if (inventario.length() == 0) {

            TextView vacio = new TextView(this);

            vacio.setText(
                    "No hay repuestos registrados."
            );

            vacio.setTextColor(Color.WHITE);
            vacio.setTextSize(16);
            vacio.setPadding(10, 30, 10, 10);

            contenedorInventario.addView(vacio);

            return;
        }

        for (int i = 0; i < inventario.length(); i++) {

            try {

                JSONObject repuesto =
                        inventario.getJSONObject(i);

                int id = repuesto.optInt("id");

                String nombre =
                        repuesto.optString("nombre");

                double precio =
                        repuesto.optDouble("precio");

                int existencia =
                        repuesto.optInt("existencia");

                // Crear tarjeta visual

                LinearLayout tarjeta =
                        new LinearLayout(this);

                tarjeta.setOrientation(
                        LinearLayout.VERTICAL
                );

                tarjeta.setPadding(
                        35, 30, 35, 30
                );

                tarjeta.setBackgroundColor(
                        Color.rgb(35, 45, 45)
                );

                LinearLayout.LayoutParams parametros =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                parametros.setMargins(
                        0, 0, 0, 20
                );

                tarjeta.setLayoutParams(parametros);

                // Nombre del repuesto

                TextView txtNombre =
                        new TextView(this);

                txtNombre.setText(
                        nombre.toUpperCase()
                );

                txtNombre.setTextColor(
                        Color.rgb(240, 185, 75)
                );

                txtNombre.setTextSize(19);

                txtNombre.setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                );

                tarjeta.addView(txtNombre);

                // ID

                TextView txtId =
                        new TextView(this);

                txtId.setText(
                        "Código: " + id
                );

                txtId.setTextColor(
                        Color.LTGRAY
                );

                txtId.setTextSize(13);

                tarjeta.addView(txtId);

                // Precio

                TextView txtPrecio =
                        new TextView(this);

                txtPrecio.setText(
                        String.format(
                                java.util.Locale.US,
                                "Precio: Q %.2f",
                                precio
                        )
                );

                txtPrecio.setTextColor(
                        Color.WHITE
                );

                txtPrecio.setTextSize(16);

                tarjeta.addView(txtPrecio);

                // Existencia

                TextView txtExistencia =
                        new TextView(this);

                txtExistencia.setText(
                        "Unidades disponibles: " + existencia
                );

                if (existencia <= 5) {

                    txtExistencia.setTextColor(
                            Color.rgb(229, 107, 80)
                    );

                } else {

                    txtExistencia.setTextColor(
                            Color.rgb(130, 200, 150)
                    );

                }

                txtExistencia.setTextSize(15);

                tarjeta.addView(txtExistencia);

                // Agregar tarjeta a la pantalla

                contenedorInventario.addView(tarjeta);

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }

}