# Orientações do projeto Milheiro

## Testes unitários

Por orientação explícita do usuário, a partir de agora:

- Não criar, alterar ou executar testes unitários neste projeto.
- Não fazer testes na aplicação.
- Não executar comandos de teste abrangentes que incluam testes unitários, como `mvn test`.
- Não realizar nenhuma validação dos ajustes sem solicitação explícita do usuário.
- Preservar os testes existentes; esta orientação não autoriza removê-los.

Esta orientação permanece válida até que o usuário solicite sua alteração.

## Interface e mensagens

- Usar `src/main/resources/META-INF/resources/programa-fidelidade.xhtml` e `src/main/java/com/roknauta/milheiro/web/crud/ProgramaFidelidadeController.java` como padrão para criar ou adaptar telas CRUD. Cada cadastro deve possuir uma classe DTO e uma classe Service próprias, seguindo a estrutura de `CrudControllerBase` e `CrudService`. Essa referência atualiza o padrão de telas CRUD; preservar as regras específicas dos controllers e serviços de operações descritas neste documento.

- Usar o cadastro de pessoa física de `/home/douglas/workspace/idea/termitech` como referência de CRUD. Centralizar a estrutura em `WEB-INF/crud.xhtml`: Novo acima dos filtros, Pesquisar dentro dos filtros e ações de salvar e voltar/cancelar no rodapé do formulário.
- Abrir somente a pesquisa por padrão; mostrar resultados depois de Pesquisar e formulário somente após Novo ou Editar.
- Todas as mensagens e textos apresentados devem estar em arquivo properties, com chaves em português.
- Campos de formulário que combinam dois ou mais componentes (por exemplo label, input e message) devem usar composite. Não criar composites para componentes isolados.
- Em dataTable, usar a tag Facelets `t:column` para uma única exibição, com cabeçalho no estilo padrão do PrimeFaces e ordenação/filtro desativados por padrão. Manter `p:column` para conteúdo combinado ou ações. Permitir converter e footer na tag.
- Usar a aparência padrão do tema PrimeFaces configurado. Não adicionar CSS próprio nem estilos inline sem pedido explícito; ajustes visuais serão feitos sob demanda.
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
- OperacaoDTO herda BaseDTO e concentra os atributos comuns. AcumuloDTO, TransferenciaDTO, VendaDTO, ResgateDTO e EstornoDTO reaproveitam essa base e declaram somente seus atributos específicos. Não recriar OperacaoFormulario. Carrinho, proporção, bônus e resumo ficam em TransferenciaDTO; compartilhar cálculos pelo TransferenciaHelper nos serviços.
- Persistir os movimentos finais, custos e o valor adicional das transferências; manter versão para concorrência e vínculo/parcela dos créditos.
- Atualizar Consolidado (programa, acumulado, saldo e milheiro) na mesma transação de cada operação, importação e mudança de status.
- Estorno é integral e vinculado a uma operação confirmada, com data igual ou posterior. Impedir estornos confirmados duplicados e estorno de estorno.
- Estorno de acúmulo retira pontos e custo; estorno de saída devolve pontos e reverte desembolso/receita. Os créditos da transferência continuam independentes.
- Cancelar um estorno desfaz a reversão; bloquear mudança de status da operação original enquanto houver estorno confirmado.
- A aplicação deve preservar suas regras de integridade de saldo. A orientação para não validar ajustes refere-se ao trabalho do agente, não à remoção dessas regras de negócio.

## Telas e controllers de operações

- Cada operação possui XHTML completo e controller próprio: AcumuloController, TransferenciaController, VendaController, ResgateController e EstornoController.
- Não recriar OperacaoController. Cada controller de operação estende diretamente CrudControllerBase com seu DTO e Service específicos. Ações extras, como cancelar e confirmar, são declaradas nos respectivos controllers e delegam ao serviço específico. Cancelamento é comum na base; confirmação pertence a AcumuloService. Preservar a versão apresentada no grid. Não manter regras de operação nos controllers.
- Concentrar cálculos de transferência, aplicação de fator, carrinho e resumo em TransferenciaService; seleção, elegibilidade e resumo do lançamento original em EstornoService. Os controllers específicos apenas delegam as ações da tela aos respectivos serviços.
- Cada XHTML define seus campos e colunas, sem escolher a operação pela URL nem usar um formulário genérico condicionado pelo tipo.
- Compartilhar somente o layout CRUD e fragmentos de ações comuns. DashboardController é responsável pelo dashboard; FatorConversaoController é responsável pelo cadastro de fatores, usando FatorConversaoDTO e FatorConversaoService em fator-conversao.xhtml. ProgramaFidelidadeController é responsável pelo cadastro de programas de fidelidade.

## Identidade e atributos comuns das entidades

- Todas as entidades de domínio devem herdar EntidadeBase, diretamente ou por Operacao. O id fica somente em EntidadeBase. Consolidado herda esse atributo e deriva seu valor de ProgramaFidelidade por @MapsId. Enums não participam dessa herança.
- O tipo de operação é definido pelas classes filhas; a coluna tipo é o discriminador JPA da herança JOINED, preservando o histórico e a migração existente.
- Status e versão continuam comuns às operações: controlam efetivação/cancelamento e concorrência.

## Serviço das operações

- OperacaoService é a única classe base concreta, sem parâmetros genéricos, para os serviços de operações e seus consumidores comuns. Compartilhar nessa base somente preparação comum, controle de versão/status, bloqueios de integridade, exclusão comum e orquestração da consolidação; os serviços filhos herdam diretamente de OperacaoService e implementam CrudService com seu DTO específico. OperacaoService é o bean principal (`@Primary`) para consultas de operações, dashboard, calculadora e importação.
- Cada controller CRUD usa seu DTO e Service específicos; salvarAcumulo, salvarTransferencia, salvarVenda, salvarResgate e salvarEstorno são implementados exclusivamente nos respectivos serviços filhos. Compartilhar apenas preparação comum, persistência e consolidação; não recriar um salvarOperacao genérico com decisões por tipo.

## Construção das entidades

- Usar @SuperBuilder em toda a hierarquia das entidades, incluindo EntidadeBase, preservando @NoArgsConstructor para JPA e consumidores existentes.
- Preservar inicializações dos atributos com @Builder.Default. Não informar id ou versão ao construir novos registros.
- Serviços devem usar o mapper próprio da entidade para converter DTOs em entidades com toEntity e atualizar registros existentes com updateEntity e @MappingTarget. Não montar entidades com builders nos serviços nem criar cópias com toBuilder. Configurar MapStruct com builder desabilitado; preservar os defaults dos construtores JPA. Cálculos e associações controladas pelo negócio permanecem nos serviços, preservando id, versão, status e vínculos nas edições.

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

- Dashboard usa dashboard.xhtml e DashboardController. Manter os gráficos lado a lado usando o layout nativo do PrimeFaces e as cores padrão dos gráficos.

## Aparência padrão — orientação atual

- Removida a personalização verde, os arquivos custom.css e primefaces-custom.css e os estilos inline dos XHTML.
- Manter a estrutura com componentes JSF/PrimeFaces e seus layouts nativos, sem recriar a identidade visual anterior.
- Classes funcionais nativas, como ui-confirmdialog-yes/no, devem ser preservadas.

- Seleções de ProgramaFidelidade usam o composite programaFidelidadeAutoComplete (label, autoComplete e message), com busca por nome e seleção por id Long; preservar eventos de seleção e limpeza.

## Ajuste visual autorizado

- Permitir CSS de estrutura, espaçamento, alinhamento, tipografia e responsividade em custom.css e primefaces-custom.css. Preservar todas as cores, fundos, bordas coloridas e estados nativos do tema PrimeFaces; não reintroduzir a paleta verde personalizada.

## Nomes das telas e títulos de formulário

- Menus e arquivos das operações no singular: Acúmulo (acumulo.xhtml), Transferência (transferencia.xhtml), Venda (venda.xhtml), Resgate (resgate.xhtml) e Estorno (estorno.xhtml).
- O template CRUD mostra Cadastrando <menu> para registros novos e Editando <menu> para registros existentes, distinguindo pelo id do registro.

## Reaproveitamento e responsabilidades

- Sempre reaproveitar atributos e código compartilhados quando necessário, evitando duplicações. DTOs filhos devem herdar os atributos básicos de OperacaoDTO, seguindo o padrão de ProgramaFidelidadeController e CrudControllerBase.
- Evitar ao máximo `if` para decidir comportamento por tipo; preferir polimorfismo, métodos específicos e composição. Preservar condições necessárias às regras de integridade e às pré-condições dos serviços.
- Controllers cuidam somente da apresentação, navegação e chamadas aos serviços. Lógicas de operação ficam nos Services específicos dos filhos de Operacao, com reaproveitamento dos serviços comuns e de OperacaoService para as regras transacionais existentes.

## Mappers por entidade

- Sempre criar um mapper MapStruct próprio para cada entidade, seguindo ProgramaFidelidadeMapper: `@Mapper(componentModel = "spring")`, conversão para DTO e, nas entidades concretas, métodos de criação e atualização com `@MappingTarget`.
- Não concentrar conversões de entidades diferentes em um mapper único, nem recriar OperacaoDTOMapper. Mappers de associações são reutilizados por `uses`; atributos comuns podem compartilhar uma `@MapperConfig`.
- Preservar id, versão, status e vínculos controlados pelo serviço nas atualizações; não atribuir id ou versão na construção de novos registros.

## Escopo dos serviços de operações

- OperacaoService concentra somente comportamento comum a todas as operações. Não implementar nessa base gravação por tipo, confirmação de acúmulo, criação/edição de créditos, preparação de estorno nem efeitos específicos sobre o consolidado. Não incluir CRUD nem consultas de cadastro de programas ou fatores.
- ProgramaFidelidadeService concentra o cadastro e a consulta de programas; FatorConversaoService concentra o cadastro e a consulta de fatores. Dashboard, calculadora, importação e controllers devem chamar o serviço correspondente à responsabilidade, preservando as transações existentes.

## Anotações e exclusão no padrão CRUD

- Controllers CRUD usam `@Component` e `@ViewScoped` (`jakarta.faces.view.ViewScoped`), seguindo `web/crud/ProgramaFidelidadeController.java`. Não usar anotações JPA de entidade nos controllers.
- A exclusão é a ação `delete` de CrudControllerBase; não criar `deleteRegistro` nos controllers concretos. Reaproveitar o fluxo de atualização dos resultados e mensagens da base.
- Os Services específicos de operações implementam `delete(DTO)` delegando a `super.excluir(dto)` em OperacaoService. O grid envia o DTO para preservar a versão exibida e o controle de concorrência; os demais cadastros podem usar `delete(Long)`.

## Distribuição das regras por operação

- OperacaoService contém apenas comportamento comum; não manter métodos específicos dos filhos nessa base. Cada serviço concentra gravação e regras do seu tipo, chamando os métodos comuns públicos de OperacaoService.
- AcumuloService cuida da gravação, confirmação e criação/validação dos créditos. TransferenciaService cuida da gravação/edição da transferência, exclusão dos créditos vinculados e confirmação de transferências importadas. VendaService e ResgateService cuidam de suas gravações. EstornoService cuida da gravação e preparação do estorno.
- Os efeitos específicos de cada tipo sobre os saldos ficam no respectivo serviço. A base apenas orquestra a reconstrução do histórico e a atualização transacional dos consolidados.
- A importação chama os serviços específicos para gravar cada tipo e mantém a limpeza e a carga na mesma transação.

## Unificação dos serviços comuns

- Manter somente OperacaoService como base e serviço comum, sem generics. Não recriar OperacaoConsultaService nem OperacaoCrudService.
- Os Services filhos herdam diretamente OperacaoService, implementam CrudService com o DTO concreto e chamam métodos públicos da base para consultas, filtros, preparação, exclusão, status, integridade e consolidação comuns.
- Preservar métodos e repositórios específicos de gravação nos filhos; a base não decide como salvar cada tipo.

## Conversão de DTO para entidade

- Reaproveitar os mappers por entidade em vez de builders Lombok ou cópia manual dos atributos do DTO. Id e versão não são mapeados na criação; status e vínculos são definidos/preservados pelas regras do serviço.
- A preparação comum de lançamentos valida os dados e resolve o programa; a conversão dos atributos é responsabilidade do mapper. Os cálculos específicos continuam no serviço de cada tipo.

## Autocomplete de cadastros

- Centralizar as buscas de autocomplete em web/crud/CrudHelper. getProgramasFidelidade(String nome) chama ProgramaFidelidadeService.buscarPorNome(nome), recebe DTOs e converte para SelectItem com id Long e nome como label.
- O serviço passa o texto pesquisado ao repository; buscar programas ativos por nome, sem carregar todo o cadastro para filtrar na memória. O repository define a busca sem distinção de maiúsculas/minúsculas e ordena por nome.
- O composite programaFidelidadeAutoComplete utiliza CrudHelper, sem receber listas dos controllers. Preservar seleção por id Long, label da seleção existente, validação e eventos itemSelect/clear.
