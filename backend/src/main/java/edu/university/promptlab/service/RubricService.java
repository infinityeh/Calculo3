package edu.university.promptlab.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class RubricService {

    private static final Pattern REQUEST_SOLVE = Pattern.compile("resolver|calcule|calcular|encontre", Pattern.CASE_INSENSITIVE);
    private static final Pattern REQUEST_EXPLAIN = Pattern.compile("explique|explain|conceito", Pattern.CASE_INSENSITIVE);
    private static final Pattern REQUEST_HINT = Pattern.compile("dica|hint|orienta", Pattern.CASE_INSENSITIVE);
    private static final Pattern REQUEST_VISUAL = Pattern.compile("visualize|gráfico|desenhe|plot", Pattern.CASE_INSENSITIVE);
    private static final Pattern REQUEST_QUIZ = Pattern.compile("quiz|pergunta|teste", Pattern.CASE_INSENSITIVE);

    public Map<String, Object> buildRubric(String prompt) {
        Map<String, Object> rubric = new HashMap<>();
        String lower = prompt == null ? "" : prompt.toLowerCase();
        rubric.put("clarity", evaluateClarity(lower));
        rubric.put("specificity", evaluateSpecificity(lower));
        rubric.put("context", evaluateContext(lower));
        rubric.put("requestType", inferRequestType(lower));
        return rubric;
    }

    private int evaluateClarity(String lower) {
        if (lower.contains("passo a passo") || lower.contains("detalhe") || lower.contains("justifique")) {
            return 3;
        }
        if (lower.contains("?") || lower.split("\\.").length > 1) {
            return 2;
        }
        return 1;
    }

    private int evaluateSpecificity(String lower) {
        if (lower.matches(".*(passo a passo|mostre os passos|detalhe|justifique|teorema|green|stokes|jacobiano|mudança de variáveis|limites).*")) {
            return 3;
        }
        if (lower.matches(".*(integral|campo).*")) {
            return 2;
        }
        return 1;
    }

    private int evaluateContext(String lower) {
        if (lower.matches(".*(dados|equação|intervalo|limites|imagem|figura|código).*")) {
            return 2;
        }
        return 1;
    }

    private String inferRequestType(String lower) {
        if (REQUEST_SOLVE.matcher(lower).find()) {
            return "solve";
        }
        if (REQUEST_EXPLAIN.matcher(lower).find()) {
            return "explain";
        }
        if (REQUEST_HINT.matcher(lower).find()) {
            return "hint";
        }
        if (REQUEST_VISUAL.matcher(lower).find()) {
            return "visualize";
        }
        if (REQUEST_QUIZ.matcher(lower).find()) {
            return "quiz";
        }
        return "explain";
    }
}
