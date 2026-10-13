package com.proyectoApi.api.controller;

import com.proyectoApi.api.dto.AdicionalDto;
import com.proyectoApi.api.dto.MesaDto;
import com.proyectoApi.api.dto.ProductoDto;
import com.proyectoApi.api.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Datos de solo lectura que necesita la pantalla del cliente. */
@RestController
@RequestMapping("/api")
public class MenuController {

    private final MenuService menu;

    public MenuController(MenuService menu) {
        this.menu = menu;
    }

    @GetMapping("/mesas")
    public List<MesaDto> mesas() {
        return menu.tables();
    }

    @GetMapping("/productos")
    public List<ProductoDto> productos() {
        return menu.products();
    }

    @GetMapping("/adicionales")
    public List<AdicionalDto> adicionales() {
        return menu.extras();
    }
}
