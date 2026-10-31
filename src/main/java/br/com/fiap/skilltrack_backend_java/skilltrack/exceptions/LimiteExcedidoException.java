package br.com.fiap.skilltrack_backend_java.skilltrack.exceptions;

import lombok.Getter;

@Getter
public class LimiteExcedidoException extends RuntimeException {

    private final long segundosDeEspera;

    public LimiteExcedidoException(long segundosDeEspera) {
        super("Muitas tentativas. Tente novamente em " + descreverEspera(segundosDeEspera) + ".");
        this.segundosDeEspera = segundosDeEspera;
    }

    private static String descreverEspera(long segundos) {
        long minutos = Math.max(1, (segundos + 59) / 60);
        return minutos == 1 ? "1 minuto" : minutos + " minutos";
    }
}
