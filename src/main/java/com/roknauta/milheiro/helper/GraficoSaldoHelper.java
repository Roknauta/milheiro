package com.roknauta.milheiro.helper;

import com.roknauta.milheiro.domain.Consolidado;
import com.roknauta.milheiro.web.Textos;
import org.primefaces.shaded.json.JSONObject;
import java.util.*;

/** Configuração Chart.js consumida pelo p:chart do PrimeFaces 15. */
public final class GraficoSaldoHelper {
    private static final List<String> CORES = List.of(
        "#237455", "#3b82f6", "#f59e0b", "#8b5cf6", "#ef4444", "#06b6d4", "#ec4899", "#84cc16",
        "#f97316", "#6366f1", "#14b8a6", "#a855f7");

    private GraficoSaldoHelper() { }

    public static String pizza(List<Consolidado> saldos) {
        List<String> cores = new ArrayList<>();
        for (int i = 0; i < saldos.size(); i++) cores.add(CORES.get(i % CORES.size()));
        Map<String, Object> dados = Map.of(
            "labels", saldos.stream().map(c -> c.getPrograma().getNome()).toList(),
            "datasets", List.of(Map.of(
                "label", Textos.get("interface.saldo"),
                "data", saldos.stream().map(Consolidado::getSaldo).toList(),
                "backgroundColor", cores, "borderColor", "#ffffff", "borderWidth", 2)));
        Map<String, Object> opcoes = Map.of(
            "responsive", true, "maintainAspectRatio", false, "animation", false, "locale", "pt-BR",
            "plugins", Map.of("legend", Map.of("position", "bottom",
                "labels", Map.of("color", "#243e34", "usePointStyle", true, "padding", 18))));
        String json = new JSONObject(Map.of("type", "pie", "data", dados, "options", opcoes)).toString();
        return json.replace("<", "\\u003c").replace(">", "\\u003e").replace("&", "\\u0026");
    }
}
