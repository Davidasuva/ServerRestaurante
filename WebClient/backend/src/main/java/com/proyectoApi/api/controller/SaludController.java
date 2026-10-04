package com.proyectoApi.api.controller;

import com.proyectoApi.api.exception.ApiException;
import com.proyectoApi.api.rmi.RmiGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** GET /api/salud: comprobación rápida de la cadena navegador → API → RMI (útil al configurar la red). */
@RestController
@RequestMapping("/api")
public class SaludController {

    private final RmiGateway rmi;

    public SaludController(RmiGateway rmi) {
        this.rmi = rmi;
    }

    @GetMapping("/salud")
    public Map<String, Object> salud() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("api", "ok");
        body.put("servidorRmi", rmi.describe());
        try {
            rmi.mesas(stub -> stub.contar());
            body.put("rmi", "ok");
        } catch (ApiException e) {
            body.put("rmi", "no disponible");
            body.put("detalle", e.getMessage());
        }
        return body;
    }
}
