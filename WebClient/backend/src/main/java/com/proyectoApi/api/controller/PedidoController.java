package com.proyectoApi.api.controller;

import com.proyectoApi.api.dto.EstadoPedidoDto;
import com.proyectoApi.api.dto.PedidoCreadoDto;
import com.proyectoApi.api.dto.PedidoRequest;
import com.proyectoApi.api.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final OrderService orders;

    public PedidoController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<PedidoCreadoDto> crear(@RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orders.create(request));
    }

    @GetMapping("/{id}/estado")
    public EstadoPedidoDto estado(@PathVariable int id) {
        return orders.status(id);
    }
}
