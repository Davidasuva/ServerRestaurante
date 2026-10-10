package com.example.service;

import java.util.List;

import server.model.mesa.Mesa;

/** Acceso a las mesas del restaurante. */
public interface MesaService {

    /** Todas las mesas, ordenadas por id. Lista vacía si no hay ninguna. */
    List<Mesa> listar() throws ServicioException;
}
