package com.example.service.local;

import java.util.ArrayList;
import java.util.List;

import com.example.service.MesaService;

import server.model.mesa.Mesa;

/** Mesas de prueba (1 a 10) mientras no hay servidor. */
public class LocalMesaService implements MesaService {

    private static final int NUM_MESAS = 10;

    @Override
    public List<Mesa> listar() {
        List<Mesa> mesas = new ArrayList<>();
        for (int i = 1; i <= NUM_MESAS; i++) {
            mesas.add(new Mesa(i));
        }
        return mesas;
    }
}
