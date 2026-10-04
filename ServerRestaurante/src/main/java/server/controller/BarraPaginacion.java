package server.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;

public class BarraPaginacion extends HBox {

    public static final List<Integer> TAMANOS = List.of(5, 10);
    private static final int TAMANO_INICIAL = 10;


    @FunctionalInterface
    public interface Consulta<T> {
        List<T> obtener(int inicio, int fin) throws Exception;
    }


    public record Pagina<T>(List<T> items, int pagina, int totalPaginas, int total) {}

    private final Runnable alCambiar;
    private final Button btnAnterior = new Button("‹ Anterior");
    private final Button btnSiguiente = new Button("Siguiente ›");
    private final Label lblPagina = new Label("Página 1 de 1");
    private final ComboBox<Integer> cmbTamano = new ComboBox<>();

    private int pagina = 0;
    private int totalPaginas = 1;


    public BarraPaginacion(Runnable alCambiar) {
        this.alCambiar = alCambiar;

        Label lblFilas = new Label("Filas por página:");
        lblFilas.getStyleClass().add("seccion-subtitulo");
        lblPagina.getStyleClass().add("seccion-subtitulo");
        cmbTamano.getStyleClass().add("combo-campo");
        btnAnterior.getStyleClass().add("btn-link");
        btnSiguiente.getStyleClass().add("btn-link");

        cmbTamano.getItems().setAll(TAMANOS);
        cmbTamano.setValue(TAMANO_INICIAL);
        cmbTamano.valueProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                pagina = 0;
                this.alCambiar.run();
            }
        });

        btnAnterior.setDisable(true);
        btnSiguiente.setDisable(true);
        btnAnterior.setOnAction(e -> {
            if (pagina > 0) {
                pagina--;
                this.alCambiar.run();
            }
        });
        btnSiguiente.setOnAction(e -> {
            if (pagina < totalPaginas - 1) {
                pagina++;
                this.alCambiar.run();
            }
        });

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(10, 0, 0, 0));
        getChildren().addAll(lblFilas, cmbTamano, espacio, btnAnterior, lblPagina, btnSiguiente);
    }


    public void instalarDebajoDe(Node tabla) {
        if (getParent() != null) {
            return;
        }
        Pane padre = (Pane) tabla.getParent();
        padre.getChildren().add(padre.getChildren().indexOf(tabla) + 1, this);
    }


    public int getPagina() {
        return pagina;
    }


    public int getTamano() {
        Integer t = cmbTamano.getValue();
        return t == null ? TAMANO_INICIAL : t;
    }

    public static <T> Pagina<T> leer(int pagina, int tamano, int total, Consulta<T> consulta) throws Exception {
        if (total <= 0) {
            return new Pagina<>(List.of(), 0, 1, 0);
        }
        int totalPaginas = (total + tamano - 1) / tamano;
        int p = Math.max(0, Math.min(pagina, totalPaginas - 1));
        int inicio = p * tamano;
        int fin = Math.min(inicio + tamano, total) - 1;
        return new Pagina<>(consulta.obtener(inicio, fin), p, totalPaginas, total);
    }


    public void mostrar(Pagina<?> resultado) {
        pagina = resultado.pagina();
        totalPaginas = resultado.totalPaginas();
        lblPagina.setText("Página " + (pagina + 1) + " de " + totalPaginas
                + "  ·  " + resultado.total() + (resultado.total() == 1 ? " registro" : " registros"));
        btnAnterior.setDisable(pagina <= 0);
        btnSiguiente.setDisable(pagina >= totalPaginas - 1);
    }
}