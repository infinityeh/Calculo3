package edu.university.promptlab.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TopicGuard {

    private static final List<String> KEYWORDS = Arrays.asList(
            "cálculo 3","integral dupla","integral tripla","jacobiano","coordenadas cilíndricas",
            "coordenadas esféricas","campo vetorial","gradiente","divergência","rotacional",
            "teorema de green","teorema de stokes","teorema da divergência","gauss","superfície",
            "fluxo","curva","orientação","parametrização","região de integração"
    );

    public boolean isAllowed(String prompt) {
        String lower = prompt == null ? "" : prompt.toLowerCase();
        return KEYWORDS.stream().anyMatch(lower::contains);
    }

    public String buildBlockedMessage() {
        return "Seu prompt precisa estar relacionado ao conteúdo de Cálculo 3. Tente incluir termos do curso.";
    }
}
