package com.example.service;

import java.util.List;

import server.model.producto.Producto;

/** Acceso al catálogo de productos. */
public interface ProductoService {

    /** Todos los productos, ordenados por id. Lista vacía si no hay ninguno. */
    List<Producto> listar() throws ServicioException;
}
