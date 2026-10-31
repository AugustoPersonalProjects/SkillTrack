package br.com.fiap.skilltrack_backend_java.skilltrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

// Datas sem hora (prazo das metas, data do progresso) seguem o calendário do Brasil, não o fuso do servidor
@Configuration
public class ClockConfig {

    public static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock clock() {
        return Clock.system(FUSO_HORARIO);
    }
}
