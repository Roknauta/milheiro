package com.roknauta.milheiro.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.roknauta.milheiro.web.Textos;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Migração idempotente, usando JDBC enquanto os subtipos ainda estão sendo preenchidos. */
@Service
public class MigracaoOperacoes {
    private final JdbcTemplate jdbc;

    public MigracaoOperacoes(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public void normalizar() {
        jdbc.update("update programa set categoria=upper(categoria) where lower(categoria) in ('pontos','milhas')");
        if (!coluna("DESEMBOLSO")) return;
        jdbc.execute("create table if not exists migracao_modelo (nome varchar(80) primary key)");
        if (jdbc.queryForObject("select count(*) from migracao_modelo where nome='valor_adicional'", Long.class) > 0)
            return;
        List<Map<String, Object>> linhas = jdbc.queryForList("select * from operacao order by data, id");
        Map<Long, BigDecimal[]> posicoes = new HashMap<>();
        linhas.sort(Comparator.comparing((Map<String, Object> l) -> l.get("DATA").toString())
            .thenComparing(l -> numero(l.get("TRANSFERENCIA_ORIGEM_ID") == null
                ? l.get("ID") : l.get("TRANSFERENCIA_ORIGEM_ID")))
            .thenComparing(l -> numero(l.get("ID"))));
        for (Map<String, Object> l : linhas) {
            long id = numero(l.get("ID"));
            String tipo = l.get("TIPO").toString();
            long programa = numero(l.get("PROGRAMA_ID"));
            BigDecimal[] saldo = posicoes.computeIfAbsent(programa,
                k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            String status = l.get("STATUS") == null
                ? (Boolean.TRUE.equals(l.get("CANCELADA")) ? "CANCELADO" : "CONFIRMADO")
                : l.get("STATUS").toString();
            BigDecimal quantidade = decimal(l, "QUANTIDADE");
            BigDecimal valor = decimal(l, "VALOR");
            if (l.get("DESEMBOLSO") == null) {
                BigDecimal taxas = decimal(l, "TAXAS");
                BigDecimal desembolso = taxas;
                if (tipo.equals("ACUMULO")) {
                    valor = valor.add(taxas);
                    desembolso = l.get("TRANSFERENCIA_ORIGEM_ID") == null ? valor : BigDecimal.ZERO;
                } else if (tipo.equals("TRANSFERENCIA")) {
                    BigDecimal total = quantidade;
                    boolean carrinho = Boolean.TRUE.equals(l.get("COM_CARRINHO"));
                    quantidade = carrinho ? decimal(l, "PONTOS_DEBITAR_SALDO") : total;
                    BigDecimal compra = carrinho ? decimal(l, "VALOR_CARRINHO") : BigDecimal.ZERO;
                    desembolso = compra.add(taxas);
                    if (!Boolean.TRUE.equals(l.get("CREDITOS_GERADOS"))) {
                        valor = quantidade.signum() == 0 ? BigDecimal.ZERO
                            : Calculos.proporcional(quantidade, saldo[0], saldo[1]);
                        valor = valor.add(compra).add(taxas).setScale(2, RoundingMode.HALF_UP);
                        BigDecimal base = Calculos.transferirProporcao(total, decimal(l, "PONTOS_ORIGEM"),
                            decimal(l, "PONTOS_DESTINO"), BigDecimal.ZERO, 2, RoundingMode.DOWN);
                        BigDecimal creditos = Calculos.transferirProporcao(total, decimal(l, "PONTOS_ORIGEM"),
                            decimal(l, "PONTOS_DESTINO"), decimal(l, "BONUS"), 2, RoundingMode.DOWN);
                        inserirCredito(l, status, "BASE", base, valor);
                        if (creditos.compareTo(base) > 0)
                            inserirCredito(l, status, "BONUS", creditos.subtract(base), BigDecimal.ZERO);
                        if (status.equals("CONFIRMADO")) {
                            BigDecimal[] destino = posicoes.computeIfAbsent(numero(l.get("DESTINO_ID")),
                                k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                            destino[0] = destino[0].add(creditos);
                            destino[1] = destino[1].add(valor);
                        }
                    } else {
                        valor = valor.add(taxas);
                    }
                }
                jdbc.update("update operacao set quantidade=?, valor=?, desembolso=?, status=?, "
                    + "versao=coalesce(versao,0) where id=?", quantidade, valor, desembolso, status, id);
            }
            if (status.equals("CONFIRMADO") && tipo.equals("ACUMULO")) {
                saldo[0] = saldo[0].add(quantidade);
                saldo[1] = saldo[1].add(valor);
            }
        }
        for (String tipo : List.of("TRANSFERENCIA", "ACUMULO", "VENDA", "RESGATE")) {
            if (jdbc.queryForObject("select count(*) from operacao o where tipo=? and not exists (select 1 from "
                + tipo + " t where t.id=o.id)", Long.class, tipo) == 0) continue;
            if (tipo.equals("TRANSFERENCIA") && coluna("DESTINO_ID")) {
                jdbc.update("insert into transferencia (id,destino_id) select id,destino_id from operacao o "
                    + "where tipo='TRANSFERENCIA' and not exists (select 1 from transferencia t where t.id=o.id)");
            } else if (tipo.equals("ACUMULO") && coluna("TRANSFERENCIA_ORIGEM_ID")) {
                jdbc.update("insert into acumulo (id,transferencia_origem_id,parcela_transferencia) "
                    + "select id,transferencia_origem_id,parcela_transferencia from operacao o "
                    + "where tipo='ACUMULO' and not exists (select 1 from acumulo a where a.id=o.id)");
            } else {
                jdbc.update("insert into " + tipo + " (id) select id from operacao o where tipo=? "
                    + "and not exists (select 1 from " + tipo + " t where t.id=o.id)", tipo);
            }
        }
        migrarValoresFinanceiros();
    }

    private void migrarValoresFinanceiros() {
        if (jdbc.queryForObject("select count(*) from operacao where desembolso is null", Long.class) != 0)
            throw new IllegalStateException(Textos.get("operacao.migracao.incompleta"));
        jdbc.update("update transferencia t set valor_adicional=(select o.desembolso from operacao o where o.id=t.id)");
        jdbc.update("update operacao set valor=valor-desembolso where tipo='VENDA'");
        jdbc.update("update operacao set valor=desembolso where tipo='RESGATE'");
        jdbc.update("update operacao o set valor=(select original.valor from estorno e "
            + "join operacao original on original.id=e.operacao_original_id where e.id=o.id) where o.tipo='ESTORNO'");
        jdbc.update("insert into migracao_modelo(nome) values ('valor_adicional')");
    }

    private void inserirCredito(Map<String, Object> origem, String status, String parcela,
        BigDecimal pontos, BigDecimal custo) {
        Map<String, Object> campos = new LinkedHashMap<>();
        campos.put("DATA", origem.get("DATA"));
        campos.put("TIPO", "ACUMULO");
        campos.put("STATUS", status);
        campos.put("VERSAO", 0L);
        campos.put("PROGRAMA_ID", origem.get("DESTINO_ID"));
        campos.put("QUANTIDADE", pontos);
        campos.put("VALOR", custo);
        campos.put("DESEMBOLSO", BigDecimal.ZERO);
        campos.put("TRANSFERENCIA_ORIGEM_ID", origem.get("ID"));
        campos.put("PARCELA_TRANSFERENCIA", parcela);
        for (String nome : List.of("TAXAS", "BONUS", "PONTOS_ORIGEM", "PONTOS_DESTINO"))
            if (origem.containsKey(nome)) campos.put(nome, BigDecimal.ZERO);
        for (String nome : List.of("CANCELADA", "COM_CARRINHO", "CREDITOS_GERADOS"))
            if (origem.containsKey(nome)) campos.put(nome, false);
        jdbc.update("insert into operacao (" + String.join(",", campos.keySet()) + ") values ("
            + String.join(",", Collections.nCopies(campos.size(), "?")) + ")", campos.values().toArray());
    }

    /** Só remove a estrutura antiga depois da conversão transacional e reconstrução do consolidado. */
    public void concluir() {
        if (coluna("DESEMBOLSO") && jdbc.queryForObject(
            "select count(*) from migracao_modelo where nome='valor_adicional'", Long.class) == 0)
            throw new IllegalStateException(Textos.get("operacao.migracao.incompleta"));
        var restricoes = jdbc.queryForList("select distinct tc.constraint_name, cc.check_clause "
            + "from information_schema.table_constraints tc join information_schema.check_constraints cc "
            + "on tc.constraint_name=cc.constraint_name and tc.constraint_schema=cc.constraint_schema "
            + "where tc.table_name='OPERACAO' and tc.constraint_type='CHECK'");
        boolean atualizarTipo = false;
        boolean tipoAtualizado = false;
        for (var c : restricoes) {
            String regra = c.get("CHECK_CLAUSE").toString().toUpperCase(Locale.ROOT);
            if (!regra.contains("TIPO") || !regra.contains("ACUMULO") || !regra.contains("TRANSFERENCIA"))
                continue;
            if (regra.contains("ESTORNO")) { tipoAtualizado = true; continue; }
            String nome = c.get("CONSTRAINT_NAME").toString().replace("\"", "\"\"");
            jdbc.execute("alter table operacao drop constraint \"" + nome + "\"");
            atualizarTipo = true;
        }
        if (atualizarTipo && !tipoAtualizado)
            jdbc.execute("alter table operacao add constraint operacao_tipo_valido "
                + "check (tipo in ('ACUMULO','TRANSFERENCIA','VENDA','RESGATE','ESTORNO'))");
        for (String coluna : List.of("COM_CARRINHO", "PONTOS_DEBITAR_SALDO", "VALOR_CARRINHO", "CREDITOS_GERADOS",
            "BONUS", "CANCELADA", "PONTOS_ORIGEM", "PONTOS_DESTINO", "TAXAS", "DESTINO_ID",
            "TRANSFERENCIA_ORIGEM_ID", "PARCELA_TRANSFERENCIA", "CHAVE_IMPORTACAO", "DESEMBOLSO")) {
            if (coluna(coluna)) {
                var vinculos = jdbc.queryForList("select tc.constraint_name "
                    + "from information_schema.table_constraints tc join information_schema.key_column_usage ku "
                    + "on tc.constraint_name=ku.constraint_name and tc.constraint_schema=ku.constraint_schema "
                    + "where tc.table_name='OPERACAO' and tc.constraint_type='FOREIGN KEY' and ku.column_name=?",
                    String.class, coluna);
                for (String vinculo : vinculos)
                    jdbc.execute("alter table operacao drop constraint \"" + vinculo.replace("\"", "\"\"") + "\"");
                jdbc.execute("alter table operacao drop column " + coluna + " cascade");
            }
        }
        // Estes atributos já foram removidos das respectivas entidades.
        for (String tabela : List.of("PROGRAMA", "FATOR_CONVERSAO")) {
            if (coluna(tabela, "VERSAO")) jdbc.execute("alter table " + tabela + " drop column versao");
        }
        if (coluna("FATOR_CONVERSAO", "BONUS")) jdbc.execute("alter table fator_conversao drop column bonus");
    }

    private boolean coluna(String nome) { return coluna("OPERACAO", nome); }
    private boolean coluna(String tabela, String nome) {
        return jdbc.queryForObject("select count(*) from information_schema.columns "
            + "where table_schema='PUBLIC' and table_name=? and column_name=?", Integer.class, tabela, nome) > 0;
    }
    private long numero(Object valor) { return ((Number) valor).longValue(); }
    private BigDecimal decimal(Map<String, Object> linha, String campo) {
        return linha.get(campo) == null ? BigDecimal.ZERO : new BigDecimal(linha.get(campo).toString());
    }
}
