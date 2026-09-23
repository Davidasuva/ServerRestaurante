package com.proyectoApi.api.dto;

/**
 * DTO (Data Transfer Object) de respuesta. Spring/Jackson lo convierte
 * a JSON automáticamente ({"resultado": "..."}), sin que tengas que
 * escribir serialización manual como en el bridge anterior.
 */
public class SaludoResponse {
    private String resultado;

    public SaludoResponse() {
    }

    public SaludoResponse(String resultado) {
        this.resultado = resultado;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
