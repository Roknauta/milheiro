# Orientações do projeto Milheiro

## Testes unitários

Por orientação explícita do usuário, a partir de agora:

- Não criar, alterar ou executar testes unitários neste projeto.
- Não executar comandos de teste abrangentes que incluam testes unitários, como `mvn test`.
- Não realizar nenhuma validação dos ajustes sem solicitação explícita do usuário.
- Preservar os testes existentes; esta orientação não autoriza removê-los.

Esta orientação permanece válida até que o usuário solicite sua alteração.

## Interface e mensagens

- Usar o cadastro de pessoa física de `/home/douglas/workspace/idea/termitech` como referência de CRUD. Centralizar a estrutura em `WEB-INF/crud.xhtml`: Novo acima dos filtros, Pesquisar dentro dos filtros e ações de salvar e voltar/cancelar no rodapé do formulário.
- Abrir somente a pesquisa por padrão; mostrar resultados depois de Pesquisar e formulário somente após Novo ou Editar.
- Todas as mensagens e textos apresentados devem estar em arquivo properties, com chaves em português.
- Campos de formulário que combinam dois ou mais componentes (por exemplo label, input e message) devem usar composite. Não criar composites para componentes isolados.
- Em dataTable, usar a tag Facelets `t:column` para uma única exibição, com header centralizado e ordenação/filtro desativados por padrão. Manter `p:column` para conteúdo combinado ou ações. Permitir converter e footer na tag.
- Respeitar a paleta verde em todos os componentes novos; estilos gerais em custom.css e ajustes PrimeFaces em primefaces-custom.css.
- Calendários devem permitir navegação por mês e ano e entrada manual por padrão; restringir apenas quando necessário ao campo.
- Priorizar componentes JSF/PrimeFaces e formulários em duas colunas.

## Operações

- Cada tipo possui uma tela própria de consulta com botão Novo: Acúmulos, Transferências, Vendas, Resgates e Estornos. Compartilhar o template padrão; permitir editar, excluir e cancelar pelo grid; Acúmulos também permite confirmar.
- Status: CONFIRMADO, PENDENTE e CANCELADO. Acúmulo, Venda e Resgate manuais nascem confirmados. Transferências novas e créditos vinculados nascem pendentes.
- Pendentes e cancelados não afetam saldos nem cards financeiros. Cancelar uma saída devolve seus pontos; cancelar um crédito retira os pontos e o custo daquele crédito. Recalcular e validar saldos posteriores em toda mudança.
- Saída, crédito base e bônus têm status independentes. Uma mudança nunca propaga automaticamente para os outros lançamentos.
- Criar um acúmulo de destino pela paridade com todo o custo proporcional, carrinho e taxas. Com bônus, criar outro acúmulo com custo zero. Não contabilizar os créditos duas vezes nem tratá-los como novos desembolsos.
- Preservar o histórico existente e o caráter efetivado dos movimentos importados da planilha.

## Validações — orientação de 30/09/2026

- Fazer somente os ajustes solicitados. Não validar alterações por iniciativa própria.
- Não executar compilação, testes, verificações manuais, conferência no navegador ou outras validações, salvo quando o usuário solicitar explicitamente.
- A proibição de testes unitários continua válida; uma solicitação genérica de validação não autoriza testes unitários.

## Modelo das operações

- Operacao é a base abstrata das entidades Acumulo, Transferencia, Venda, Resgate e Estorno, com herança JOINED.
- Dados temporários de formulário, carrinho, proporção e bônus ficam em OperacaoFormulario. Compartilhar cálculos pelo TransferenciaHelper.
- Persistir os movimentos finais, custos e o valor adicional das transferências; manter versão para concorrência e vínculo/parcela dos créditos.
- Atualizar Consolidado (programa, acumulado, saldo e milheiro) na mesma transação de cada operação, importação e mudança de status.
- Estorno é integral e vinculado a uma operação confirmada, com data igual ou posterior. Impedir estornos confirmados duplicados e estorno de estorno.
- Estorno de acúmulo retira pontos e custo; estorno de saída devolve pontos e reverte desembolso/receita. Os créditos da transferência continuam independentes.
- Cancelar um estorno desfaz a reversão; bloquear mudança de status da operação original enquanto houver estorno confirmado.
- A aplicação deve preservar suas regras de integridade de saldo. A orientação para não validar ajustes refere-se ao trabalho do agente, não à remoção dessas regras de negócio.

## Telas e controllers de operações

- Cada operação possui XHTML completo e controller próprio: AcumuloController, TransferenciaController, VendaController, ResgateController e EstornoController.
- OperacaoController centraliza somente pesquisa, inclusão, mensagens, navegação e alteração de status.
- Manter cálculos de transferência, fator, carrinho e resumo em TransferenciaController; seleção e resumo do lançamento original em EstornoController.
- Cada XHTML define seus campos e colunas, sem escolher a operação pela URL nem usar um formulário genérico condicionado pelo tipo.
- Compartilhar somente o layout CRUD e fragmentos de ações comuns. DashboardController é responsável pelo dashboard; CarteiraBean permanece responsável pelos fatores. ProgramaFidelidadeController é responsável pelo cadastro de programas de fidelidade.

## Identidade e atributos comuns das entidades

- Todas as entidades de domínio devem herdar EntidadeBase, diretamente ou por Operacao. O id fica somente em EntidadeBase. Consolidado herda esse atributo e deriva seu valor de ProgramaFidelidade por @MapsId. Enums não participam dessa herança.
- O tipo de operação é definido pelas classes filhas; a coluna tipo é o discriminador JPA da herança JOINED, preservando o histórico e a migração existente.
- Status e versão continuam comuns às operações: controlam efetivação/cancelamento e concorrência.

## Serviço das operações

- OperacaoService é o serviço principal. Expor salvarAcumulo, salvarTransferencia, salvarVenda, salvarResgate e salvarEstorno, com retornos específicos.
- Cada controller chama diretamente o método do seu tipo. Compartilhar apenas preparação comum, persistência e consolidação; não recriar um salvarOperacao genérico com decisões por tipo.

## Construção das entidades

- Usar @SuperBuilder em toda a hierarquia das entidades, incluindo EntidadeBase, preservando @NoArgsConstructor para JPA e consumidores existentes.
- Preservar inicializações dos atributos com @Builder.Default. Não informar id ou versão ao construir novos registros.
- Serviços devem criar entidades com builders; atualizações de entidades já persistidas continuam usando setters, sem criar cópias com toBuilder.

## Importação substitutiva

- A importação da planilha substitui todo o histórico de operações, incluindo estornos e créditos vinculados, e recalcula os consolidados na mesma transação. Falhas devem reverter a limpeza e a inclusão.
- Preservar cadastros de programas e fatores; criar programas ausentes conforme a planilha.
- Não manter chaveImportacao, hashes ou deduplicação contra importações anteriores. Cada linha válida do arquivo participa da nova carga, respeitando o pareamento de transferências.

## Valores monetários

- Não persistir desembolso em Operacao. Acúmulo registra custo em valor; venda registra recebimento líquido; resgate registra taxas. Transferencia registra custo total em valor e carrinho/taxas em valorAdicional.
- Os cards derivam pagamentos dos movimentos confirmados; créditos de transferência não geram novos pagamentos. Estorno reverte os efeitos financeiros da operação original.
- Dinheiro é um objeto de valor que herda BigDecimal, não uma entidade JPA; está fora da herança EntidadeBase. Formata moedas sem converter câmbio. A aritmética herdada retorna BigDecimal.

- Atributos monetários persistidos usam Dinheiro e DinheiroPersistenceConverter para manter colunas decimais; quantidades, proporções e percentuais permanecem BigDecimal. Converter resultados aritméticos com Dinheiro.de sem arredondamento implícito.

## CRUD das operações

- Editar atualiza o lançamento existente e preserva seu status. Excluir e cancelar recalculam os consolidados na mesma transação, mantendo a integridade dos saldos.
- Excluir transferência remove também seus créditos. Bloquear edição/exclusão de lançamentos com estornos vinculados. Cancelamento continua independente por lançamento.
- Editar transferência usa valores finais persistidos; propagar data, destino e custo aos créditos, preservando quantidades e status. Bônus permanece com custo zero.

- Cada método de gravação de operação usa o repository específico da entidade (AcumuloRepository, TransferenciaRepository, VendaRepository, ResgateRepository e EstornoRepository). Não criar persistir genérico; compartilhar somente preparação e cópia dos atributos comuns em métodos privados.

## Programas de fidelidade

- Usar ProgramaFidelidade, CategoriaProgramaFidelidade e ProgramaFidelidadeRepository. O cadastro usa ProgramaFidelidadeController e programa-fidelidade.xhtml.
- Manter o mapeamento de ProgramaFidelidade para a tabela programa, preservando os dados e as chaves estrangeiras existentes.

- Dashboard usa dashboard.xhtml e DashboardController; gráficos podem usar cores variadas para distinguir programas. Manter os dois gráficos lado a lado em telas de computador.
