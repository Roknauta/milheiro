# Milheiro

Controle pessoal de pontos e milhas com **Java 25**, **Spring Boot 4.1.1**, **JoinFaces 6.1.0**, **PrimeFaces**, **Spring Data JPA** e **HSQLDB**. Segue a base técnica do projeto `jbdviagens`, com menu lateral e interface em português.

## Executar

Abra o `pom.xml` no IntelliJ e selecione um JDK 25. Execute `MilheiroApplication`, ou use:

```sh
./mvnw spring-boot:run
```

Abra http://localhost:5001. A porta diferente permite usar o jbdviagens simultaneamente.

```sh
./mvnw clean verify
java -jar target/milheiro-0.0.1-SNAPSHOT.jar
```

Execute a partir da raiz do projeto. A aplicação fica restrita a `127.0.0.1` por padrão e não possui autenticação.

## Funcionalidades

- **Visão geral:** saldo, acumulado, custo histórico e milheiro por programa; desembolso, receita e resultado de caixa.
- **Programas:** cadastrar, editar, desativar e excluir programas sem vínculos.
- **Fatores de conversão:** proporção de origem/destino e bônus padrão. Um cadastro por par de programas, com edição e exclusão.
- **Operações:** acúmulo, transferência, venda e resgate, com data, quantidade, valor, taxas e observações. Edição e cancelamento com histórico preservado.
- **Calculadora → Enviar:** origem, destino, fator editável, pontos a transferir e bônus, com carrinho opcional.
- **Calculadora → Receber:** mesmos campos, informando os pontos desejados no destino para obter a necessidade na origem. Ambos os resumos se atualizam durante o preenchimento.

O banco começa vazio. Cadastre os programas e lance os saldos iniciais como acúmulos, informando seu custo histórico. A planilha foi usada como referência de regras; seu histórico não é importado automaticamente.

## Calculadoras e regras de cálculo

Em **Enviar**, preencha origem e destino, confira o fator carregado do cadastro e informe pontos e bônus. O campo Fator é livre para a simulação: `3,500 : 1,000` significa 3,5 pontos de origem por 1 de destino. Um número sozinho, como `3,500`, equivale a `3,500 : 1,000`. Use vírgula ou ponto como separador decimal e não use separadores de milhar nesse campo. A proporção mantém conversões como 3,5:1 exatas, sem transformar o fator em uma dízima arredondada. Alterar o fator na calculadora não modifica seu cadastro.

O fator e o bônus padrão são carregados ao selecionar o par; se não houver cadastro, informe o fator manualmente. Os gastos, o acumulado e o saldo vêm dos lançamentos da origem, sem preenchimento manual desses valores. O resumo mostra o gasto total acumulado, o gasto proporcional da transferência em reais e em percentual, os pontos a creditar e o valor a associar ao destino.

Marque **Transferência com carrinho** para exibir **Pontos do Carrinho** e **Valor Carrinho**. Os pontos comprados pertencem à unidade da origem e recebem o mesmo fator e bônus. Ao desmarcar, os valores do carrinho deixam de participar da simulação.

| Resultado | Regra |
| --- | --- |
| Pontos creditados | (pontos do saldo + pontos do carrinho) × proporção destino ÷ proporção origem × (1 + bônus ÷ 100) |
| Necessidade em Receber | desejados × proporção origem ÷ [proporção destino × (1 + bônus ÷ 100)], arredondada para cima em 3 casas; subtrair pontos do carrinho, com mínimo zero |
| Gasto proporcional | pontos retirados do saldo × gasto histórico da origem ÷ acumulado histórico |
| Percentual histórico | pontos retirados do saldo ÷ acumulado histórico × 100 |
| Valor associado ao destino | gasto proporcional + valor pago no carrinho |

Em **Receber**, o total desejado inclui o bônus. Se o carrinho sozinho exceder esse total, a tela mostra a quantidade efetiva a creditar e informa que não é necessário retirar pontos do saldo. Saldo insuficiente gera um aviso sem impedir a simulação. Sem acumulado histórico, o custo proporcional fica indisponível; quando toda a transferência vem do carrinho, o custo da origem é zero. Valores vazios ou inválidos removem o resumo anterior e mostram a orientação correspondente. Nenhuma calculadora registra operações.

Pontos, fatores, bônus e percentuais são apresentados com três casas decimais em todas as telas; valores monetários mantêm duas casas. Os fatores digitados aceitam até três casas por parte da proporção. A formatação não migra nem altera os dados existentes. As operações reais continuam exigindo pontos inteiros e descartando a fração dos pontos recebidos, enquanto as calculadoras admitem simulações com três casas. A tela antiga `/calculadora.xhtml` funciona como acesso à calculadora Enviar.

O milheiro do painel continua usando gasto histórico ÷ acumulado × 1.000. Acúmulos somam valor e taxas ao custo; vendas debitam pontos e registram receita líquida das taxas; resgates debitam pontos. Transferências debitam e creditam na mesma transação, carregando custo proporcional e taxas ao destino. O custo e acumulado históricos da origem permanecem, conforme a regra da planilha. O desembolso do painel é calculado pelas saídas de caixa, sem duplicar o custo das transferências.

Operações são reconstruídas por data e identificador; alterações retroativas não podem deixar saldo negativo. Fatores e bônus ficam copiados em cada operação. Na tela Operações, **Usar conversão excepcional nesta operação** permite alterar a proporção daquele lançamento sem modificar o cadastro.

## Banco e backup

O HSQLDB funciona em **modo servidor**, iniciado automaticamente antes do pool de conexões/JPA. No encerramento normal da aplicação (inclusive Ctrl+C/SIGTERM), o JPA e o pool fecham primeiro; depois o catálogo é salvo e o servidor é encerrado. Uma falha de inicialização também libera o servidor criado pela aplicação. Uma porta ocupada impede o start, sem conectar silenciosamente a outro banco.

- Diretório: `/home/douglas/workspace/hsqldb/pessoal/milheiro`.
- Arquivos: `milheiro.properties`, `milheiro.script`, e demais arquivos HSQLDB nesse diretório.
- JDBC para a aplicação e ferramentas como DataGrip: `jdbc:hsqldb:hsql://127.0.0.1:9001/milheiro`.
- Usuário: `sa`; senha vazia, como no banco existente.
- O listener aceita conexões somente da máquina local e existe enquanto a aplicação estiver rodando.

`MILHEIRO_HSQL_PORT` altera a porta do banco (padrão 9001). `MILHEIRO_DB` altera o **prefixo completo sem extensão** dos arquivos, por exemplo `/outro/diretorio/milheiro`. `PORT` continua alterando apenas a porta HTTP (padrão 5001).

O banco anterior de `data/` foi copiado para o novo diretório, preservando os dados. A pasta `data/` original foi mantida como cópia anterior à migração e não é mais usada pela configuração padrão. Não copie essa versão antiga sobre um banco novo já utilizado.

Para backup, pare a aplicação e copie a pasta `/home/douglas/workspace/hsqldb/pessoal/milheiro/`. Espere o encerramento terminar antes de copiar/restaurar. Encerramentos forçados como SIGKILL não executam callbacks. Não execute duas instâncias usando os mesmos arquivos ou porta. O schema continua mantido pelo Hibernate (`ddl-auto=update`); adote migrações versionadas antes de mudanças estruturais em uma carteira já populada.

Os testes comuns desabilitam o servidor com `milheiro.hsql.enabled=false` e usam bancos em memória. Os testes de ciclo de vida abrem servidores reais em portas aleatórias com arquivos temporários; verificam acesso JDBC, reinício, fechamento do pool, remoção do lock, conflito de porta e limpeza em falhas de inicialização.

Implementação do encerramento conforme a [documentação oficial do HSQLDB](https://hsqldb.org/doc/guide/listeners-chapt.html): fechamento normal dos catálogos pertencentes ao servidor, seguido do encerramento do listener.

## Estrutura

`domain`: entidades; `repository`: persistência JPA; `service`: cálculos e regras transacionais; `web`: beans de tela e redirecionamento. As páginas ficam em `src/main/resources/META-INF/resources` e o CSS em `resources/css`.

Testes usam HSQLDB em memória, separados do banco pessoal. Cobrem fórmulas da planilha, consistência dos saldos, rollback, cancelamento, cadastro de fatores, renderização e submissão de formulários JSF.

Compatibilidade da base: https://github.com/joinfaces/joinfaces e https://docs.joinfaces.org/6.1.0/reference/.
