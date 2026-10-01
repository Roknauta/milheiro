package com.roknauta.milheiro.service;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.*;
import java.time.*;
import java.time.format.*;
import java.text.Normalizer;
import java.util.*;

@Service
public class PlanilhaService {

    public static final int LIMITE_BYTES = 5 * 1024 * 1024;
    private static final String[] CABECALHO = {com.roknauta.milheiro.web.Textos.get("planilha.coluna.data"), com.roknauta.milheiro.web.Textos.get("planilha.coluna.tipo"), com.roknauta.milheiro.web.Textos.get("planilha.coluna.quantidade"), com.roknauta.milheiro.web.Textos.get("planilha.coluna.destino"), com.roknauta.milheiro.web.Textos.get("planilha.coluna.valor"), com.roknauta.milheiro.web.Textos.get("planilha.coluna.observacoes")};
    private final OperacaoService carteira;
    private final ProgramaFidelidadeRepository programas;

    public PlanilhaService(OperacaoService carteira, ProgramaFidelidadeRepository programas) {
        this.carteira = carteira;
        this.programas = programas;
    }

    public record Linha(int numero, LocalDate data, TipoOperacao tipo, BigDecimal quantidade, String programa,
                        BigDecimal valor, String observacoes) {

    }


    public record Resultado(int importadas, int programasCriados, int transferencias) {

    }


    private record Movimento(Linha origem, Linha credito) {

    }

    private static String normal(String s) {
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ");
    }

    private static String nome(String s) {
        return switch (normal(s)) {
            case "azul", "tudoazul" -> "azul fidelidade";
            case "latam" -> "latam pass";
            default -> normal(s);
        };
    }

    private static IllegalArgumentException erro(int linha, String texto) {
        return new IllegalArgumentException(com.roknauta.milheiro.web.Textos.formatar("planilha.erro.linha", linha, texto));
    }

    public List<Linha> ler(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > LIMITE_BYTES)
            throw new IllegalArgumentException(
                com.roknauta.milheiro.web.Textos.get("mensagem.selecione.um.arquivo.xlsx.de.ate.5.mb"));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            if (wb.getNumberOfSheets() == 0)
                throw new IllegalArgumentException(
                    com.roknauta.milheiro.web.Textos.get("mensagem.a.planilha.nao.possui.abas"));
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            for (int c = 0; c < 6; c++)
                if (header == null || !normal(texto(header.getCell(c))).equals(normal(CABECALHO[c])))
                    throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                        "mensagem.a.primeira.linha.deve.conter.data.tipo.quantidade.destino.valor.observacoes.a.a."));
            List<Linha> linhas = new ArrayList<>();
            for (Row row : sheet) {
                int i = row.getRowNum();
                if (i == 0)
                    continue;
                boolean vazio = true;
                for (int c = 0; c < 6; c++)
                    if (!texto(row.getCell(c)).isBlank())
                        vazio = false;
                if (vazio)
                    continue;
                if (linhas.size() >= 10000)
                    throw new IllegalArgumentException(
                        com.roknauta.milheiro.web.Textos.get("mensagem.limite.de.10.000.operacoes.na.primeira.aba"));
                int n = i + 1;
                try {
                    Cell date = row.getCell(0);
                    LocalDate data;
                    if (tipo(date) == CellType.NUMERIC) {
                        double serial = date.getNumericCellValue();
                        if (!DateUtil.isValidExcelDate(serial))
                            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("planilha.data.invalida"));
                        data = DateUtil.getLocalDateTime(serial, wb.isDate1904()).toLocalDate();
                    } else
                        data = LocalDate.parse(texto(date),
                            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
                    TipoOperacao t = switch (normal(texto(row.getCell(1)))) {
                        case "acumulo" -> TipoOperacao.ACUMULO;
                        case "venda" -> TipoOperacao.VENDA;
                        case "resgate" -> TipoOperacao.RESGATE;
                        case "transferencia" -> TipoOperacao.TRANSFERENCIA;
                        default -> throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                            "mensagem.tipo.invalido.use.acumulo.venda.resgate.ou.transferencia"));
                    };
                    BigDecimal q = numero(row.getCell(2), false), v = numero(row.getCell(4), true);
                    if (q.signum() == 0 || q.stripTrailingZeros().scale() > 2 || q.abs()
                        .compareTo(new BigDecimal("999999999999")) > 0)
                        throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                            "mensagem.quantidade.invalida.use.ate.duas.casas.decimais.diferente.de.zero"));
                    if (t == TipoOperacao.ACUMULO && q.signum() < 0)
                        throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                            "mensagem.acumulo.nao.pode.ter.quantidade.negativa.informe.o.tipo.de.saida.correto"));
                    if (v.signum() < 0 || v.stripTrailingZeros().scale() > 2 || v.compareTo(
                        new BigDecimal("999999999999")) > 0)
                        throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                            "mensagem.valor.invalido.informe.valor.nao.negativo.com.ate.duas.casas.decimais"));
                    String p = texto(row.getCell(3)), obs = texto(row.getCell(5));
                    if (p.isBlank() || p.length() > 80)
                        throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                            "mensagem.informe.o.programa.na.coluna.d.com.ate.80.caracteres"));
                    if (obs.length() > 500)
                        throw new IllegalArgumentException(
                            com.roknauta.milheiro.web.Textos.get("mensagem.observacoes.maximo.de.500.caracteres"));
                    linhas.add(new Linha(n, data, t, q.abs(), p, v, obs));
                } catch (RuntimeException e) {
                    throw erro(n, e instanceof DateTimeException ? com.roknauta.milheiro.web.Textos.get(
                        "mensagem.data.invalida.use.dd.mm.aaaa") : e.getMessage());
                }
            }
            if (linhas.isEmpty())
                throw new IllegalArgumentException(
                    com.roknauta.milheiro.web.Textos.get("mensagem.nao.ha.operacoes.nas.colunas.a.a.f.da.primeira.aba"));
            return linhas;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                "mensagem.nao.foi.possivel.ler.o.arquivo.use.uma.planilha.xlsx.valida.sem.senha"), e);
        }
    }

    private static CellType tipo(Cell c) {
        return c == null
            ? CellType.BLANK
            : c.getCellType() == CellType.FORMULA ? c.getCachedFormulaResultType() : c.getCellType();
    }

    private static String texto(Cell c) {
        return switch (tipo(c)) {
            case BLANK -> "";
            case STRING -> c.getStringCellValue().trim();
            case NUMERIC -> BigDecimal.valueOf(c.getNumericCellValue()).stripTrailingZeros().toPlainString();
            default -> throw new IllegalArgumentException(
                com.roknauta.milheiro.web.Textos.get("mensagem.celula.invalida.ou.formula.com.erro"));
        };
    }

    private static BigDecimal numero(Cell c, boolean zero) {
        String s = texto(c);
        if (s.isBlank() && zero)
            return BigDecimal.ZERO;
        if (tipo(c) == CellType.NUMERIC)
            return BigDecimal.valueOf(c.getNumericCellValue());
        s = s.replace("R$", "").replace("\u00a0", "").replace(" ", "");
        if (s.contains(","))
            s = s.replace(".", "").replace(',', '.');
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                "mensagem.numero.invalido.use.celulas.numericas.ou.formato.brasileiro.1.234.56"));
        }
    }

    private List<Movimento> movimentos(List<Linha> linhas) {
        Map<Integer, Linha> pares = new HashMap<>();
        Set<Integer> creditos = new HashSet<>();
        for (Linha l : linhas)
            if (l.tipo() == TipoOperacao.TRANSFERENCIA) {
                String obs = normal(l.observacoes());
                if (!obs.startsWith("para "))
                    throw erro(l.numero(), com.roknauta.milheiro.web.Textos.get(
                        "mensagem.na.transferencia.informe.para.nome.do.programa.nas.observacoes.e.uma.linha.de.ac"));
                String destino = nome(obs.substring(5));
                List<Linha> candidatos = linhas.stream().filter(
                    c -> c.tipo() == TipoOperacao.ACUMULO && c.data().equals(l.data()) && nome(c.programa()).equals(
                        destino) && normal(c.observacoes()).matches(
                        "(?:da|do|de) " + java.util.regex.Pattern.quote(nome(l.programa())) + "(?: com .*)?")).toList();
                if (candidatos.size() != 1 || !creditos.add(candidatos.get(0).numero()))
                    throw erro(l.numero(), com.roknauta.milheiro.web.Textos.get(
                        "mensagem.nao.foi.possivel.identificar.um.unico.credito.da.transferencia.use.um.acumulo.no") + " " + l.programa() + "'.");
                Linha c = candidatos.get(0);
                if (nome(l.programa()).equals(nome(c.programa())))
                    throw erro(l.numero(),
                        com.roknauta.milheiro.web.Textos.get("mensagem.origem.e.destino.devem.ser.diferentes"));
                pares.put(l.numero(), c);
            }
        List<Movimento> result = new ArrayList<>();
        for (Linha l : linhas) {
            if (creditos.contains(l.numero()))
                continue;
            Linha c = pares.get(l.numero());
            result.add(new Movimento(l, c));
        }
        result.sort(
            Comparator.comparing((Movimento m) -> m.origem().data()).thenComparingInt(m -> m.origem().numero()));
        return result;
    }

    @Transactional
    public Resultado importar(byte[] bytes) {
        List<Movimento> movimentos = movimentos(ler(bytes));
        programas.bloquearTodos();
        Map<String, ProgramaFidelidade> cadastro = new HashMap<>();
        for (ProgramaFidelidade p : carteira.programas()) {
            if (cadastro.put(nome(p.getNome()), p) != null)
                throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                    "mensagem.ha.programas.equivalentes.no.cadastro.unifique.os.nomes.antes.de.importar"));
        }
        carteira.limparHistoricoParaImportacao();
        int criados = 0, salvos = 0, transferencias = 0;
        for (Movimento m : movimentos) {
            Linha l = m.origem();
            List<String> nomes =
                m.credito() == null ? List.of(l.programa()) : List.of(l.programa(), m.credito().programa());
            for (String n : nomes)
                if (!cadastro.containsKey(nome(n))) {
                    ProgramaFidelidade p = ProgramaFidelidade.builder()
                        .categoria(CategoriaProgramaFidelidade.PONTOS)
                        .nome(n)
                        .build();
                    cadastro.put(nome(n), carteira.salvarProgramaFidelidade(p));
                    criados++;
                }
            com.roknauta.milheiro.dto.OperacaoFormulario o = new com.roknauta.milheiro.dto.OperacaoFormulario();
            o.setTipo(l.tipo());
            o.setData(l.data());
            o.setQuantidade(l.quantidade());
            o.setObservacoes(l.observacoes());
            Long destino = null;
            if (l.tipo() == TipoOperacao.RESGATE)
                o.setTaxas(Dinheiro.de(l.valor()));
            else if (m.credito() != null) {
                destino = cadastro.get(nome(m.credito().programa())).getId();
                // A razão efetiva preserva exatamente o crédito registrado, incluindo bônus.
                o.setPontosOrigem(l.quantidade());
                o.setPontosDestino(m.credito().quantidade());
                o.setBonus(BigDecimal.ZERO);
                o.setTaxas(Dinheiro.de(l.valor()));
                transferencias++;
                String obs = l.observacoes() + " | Crédito: " + m.credito().observacoes();
                if (obs.length() > 500)
                    throw erro(l.numero(), com.roknauta.milheiro.web.Textos.get(
                        "mensagem.observacoes.combinadas.da.transferencia.excedem.500.caracteres"));
                o.setObservacoes(obs);
            } else
                o.setValor(Dinheiro.de(l.valor()));
            try {
                Operacao salva = importarOperacao(o, cadastro.get(nome(l.programa())).getId(), destino);
                if (o.isTransferencia()) carteira.confirmarTransferenciaImportada(salva.getId());
            } catch (IllegalArgumentException e) {
                throw erro(l.numero(), e.getMessage());
            }
            salvos++;
        }
        carteira.atualizarConsolidados();
        return new Resultado(salvos, criados, transferencias);
    }

    private Operacao importarOperacao(com.roknauta.milheiro.dto.OperacaoFormulario formulario,
        Long programaId, Long destinoId) {
        Map<TipoOperacao, java.util.function.Supplier<Operacao>> importadores = new EnumMap<>(TipoOperacao.class);
        importadores.put(TipoOperacao.ACUMULO, () -> carteira.salvarAcumulo(formulario, programaId));
        importadores.put(TipoOperacao.VENDA, () -> carteira.salvarVenda(formulario, programaId));
        importadores.put(TipoOperacao.RESGATE, () -> carteira.salvarResgate(formulario, programaId));
        importadores.put(TipoOperacao.TRANSFERENCIA, () -> carteira.salvarTransferencia(formulario, programaId, destinoId));
        var importador = importadores.get(formulario.getTipo());
        if (importador == null)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                "mensagem.tipo.invalido.use.acumulo.venda.resgate.ou.transferencia"));
        return importador.get();
    }

    public byte[] modelo() {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet s = wb.createSheet(com.roknauta.milheiro.web.Textos.get("planilha.aba.operacoes"));
            Row h = s.createRow(0);
            CellStyle heading = wb.createCellStyle();
            heading.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            heading.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = wb.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            heading.setFont(font);
            for (int i = 0; i < 6; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(CABECALHO[i]);
                c.setCellStyle(heading);
                s.setColumnWidth(i, (i == 5 ? 60 : i == 3 ? 25 : 20) * 256);
            }
            CellStyle date = wb.createCellStyle();
            date.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy"));
            s.setDefaultColumnStyle(0, date);
            CellStyle number = wb.createCellStyle();
            number.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
            s.setDefaultColumnStyle(4, number);
            CellStyle pontos = wb.createCellStyle();
            pontos.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
            s.setDefaultColumnStyle(2, pontos);
            s.createFreezePane(0, 1);
            s.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 1000, 0, 5));
            Sheet ajuda = wb.createSheet(com.roknauta.milheiro.web.Textos.get("planilha.aba.instrucoes"));
            ajuda.setColumnWidth(0, 120 * 256);
            String[] notas = {com.roknauta.milheiro.web.Textos.get(
                "mensagem.preencha.a.primeira.aba.somente.a.a.f.serao.importadas.remova.exemplos.antes.de."),
                com.roknauta.milheiro.web.Textos.get(
                    "mensagem.data.data.excel.ou.dd.mm.aaaa.tipos.acumulo.venda.resgate.transferencia"),
                com.roknauta.milheiro.web.Textos.get(
                    "mensagem.quantidade.ate.duas.casas.saidas.aceitam.positivo.ou.negativo.acumulo.deve.ser.p"),
                com.roknauta.milheiro.web.Textos.get(
                    "mensagem.destino.programa.que.recebe.no.acumulo.programa.de.origem.nas.saidas"),
                com.roknauta.milheiro.web.Textos.get(
                    "mensagem.valor.total.pago.no.acumulo.liquido.recebido.na.venda.taxas.complemento.no.resga"),
                com.roknauta.milheiro.web.Textos.get(
                    "mensagem.transferencia.duas.linhas.na.mesma.data.saida.com.observacao.para.azul.fidelidad"),
                com.roknauta.milheiro.web.Textos.get(
                    "mensagem.o.par.vira.uma.unica.operacao.quantidades.definem.a.conversao.efetiva.ja.incluin"),
                com.roknauta.milheiro.web.Textos.get(
                    "mensagem.o.custo.do.credito.de.transferencia.e.recalculado.pelo.acumulado.historico.o.val"),
                com.roknauta.milheiro.web.Textos.get(
                    "planilha.programas.preservados"),
                com.roknauta.milheiro.web.Textos.get(
                    "planilha.importacao.substituicao")};
            for (int i = 0; i < notas.length; i++)
                ajuda.createRow(i).createCell(0).setCellValue(notas[i]);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(
                com.roknauta.milheiro.web.Textos.get("mensagem.nao.foi.possivel.gerar.o.modelo"), e);
        }
    }
}
