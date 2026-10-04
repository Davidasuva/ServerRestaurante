package com.proyectoApi.api.rmi;

import java.rmi.RemoteException;

/** Una operación contra un stub RMI. Mantenerla corta: solo llamadas remotas. */
@FunctionalInterface
public interface RemoteCall<S, R> {
    R apply(S stub) throws RemoteException;
}
