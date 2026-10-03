package com.roknauta.milheiro.helper;

import com.roknauta.milheiro.domain.Consolidado;
import com.roknauta.milheiro.web.Msg;
import org.primefaces.shaded.json.JSONObject;
import java.util.*;

/** Configuração Chart.js consumida pelo p:chart do PrimeFaces 15. */
public final class GraficoSaldoHelper {
    private GraficoSaldoHelper() { }

    public static String pizza(List<Consolidado> saldos) {
        Map<String, Object> dados = Map.of(
            "labels", saldos.stream().map(c -> c.getPrograma().getNome()).toList(),
            "datasets", List.of(Map.of(
                "label", Msg.get("interface.saldo"),
                "data", saldos.stream().map(Consolidado::getSaldo).toList())));
        Map<String, Object> opcoes = Map.of("responsive", true, "locale", "pt-BR");
        String json = new JSONObject(Map.of("type", "pie", "data", dados, "options", opcoes)).toString();
        return json.replace("<", "\\u003c").replace(">", "\\u003e").replace("&", "\\u0026");
    }
}
