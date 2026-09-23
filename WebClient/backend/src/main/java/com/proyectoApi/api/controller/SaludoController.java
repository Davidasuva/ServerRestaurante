package com.proyectoApi.api.controller;

import com.proyectoApi.api.dto.SaludoResponse;
import com.proyectoApi.api.service.SaludoService;
import org.springframework.web.bind.annotation.*;

import java.rmi.RemoteException;

/**
 * Controller REST: recibe la petición HTTP del cliente web, delega en
 * el service (que llama por RMI) y devuelve el DTO — Spring se encarga
 * de convertirlo a JSON automáticamente gracias a @RestController.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // en producción, restringe esto al dominio real del cliente web
public class SaludoController {

    private final SaludoService saludoService;

    public SaludoController(SaludoService saludoService) {
        this.saludoService = saludoService;
    }

    // GET /api/saludar?nombre=Juan
    @GetMapping("/saludar")
    public SaludoResponse saludar(@RequestParam(defaultValue = "Mundo") String nombre) throws RemoteException {
        String resultado = saludoService.saludar(nombre); // llamada RMI real
        return new SaludoResponse(resultado);
    }

    // Añade aquí un método por cada endpoint nuevo, delegando siempre
    // en un service, nunca hablando con RMI directamente desde aquí.
}
