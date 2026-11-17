# Escopo e revisão do planejamento (PIT I → PIT II)

Ciclo de melhoria contínua (Kaizen) sobre a especificação ágil do PIT I.

## Escopo
Loja virtual de cupcakes gourmet com vitrine, carrinho, frete por CEP, pedido e painel administrativo.
Plataforma **web responsiva** (Spring Boot + Thymeleaf), e não app mobile nativo como na ideia original do PIT I: reduz custo, roda em qualquer celular pelo navegador e permite hospedar e testar com colegas por um link.

## O que mudou em relação ao PIT I

| # | Ajuste | Motivo |
|---|---|---|
| 1 | Backlog: HU-08 (frete) passa a vir **antes** da HU-09 (finalizar pedido) | O checkout usa o frete; a HU-09 depende da HU-08 |
| 2 | HU-09 (5 pontos) dividida em **HU-09A** (endereço e registro do pedido, 3 pts) e **HU-09B** (pagamento, 2 pts) | História grande demais para uma entrega; a parte de pagamento depende de gateway externo |
| 3 | Plataforma: web responsiva em vez de aplicativo mobile | Ver acima |
| 4 | Frete: valor fixo (R$ 12,00), prazo de 2 dias, atendimento por prefixo de CEP configurável | Regra simples, configurável em `application.properties` |
| 5 | Preço do item congelado no pedido (`preco_unitario`) | Preserva o histórico quando o preço muda |
| 6 | Exclusão lógica de cupcakes (`ativo`) | Mantém os pedidos antigos consistentes |

## Situação das histórias no código

| HU | Situação | Observação |
|---|---|---|
| 01 | Feito | Sem e-mail de confirmação (CA#1 parcial) |
| 02 | Feito | Bloqueio após 5 erros por 15 min |
| 03, 04 | Feito | |
| 05 | Feito | Sem avaliações e sem informação nutricional (CA#1 parcial) |
| 06, 07, 08 | Feito | |
| 09A | Feito | Pedido nasce como "Aguardando pagamento" |
| 09B | Parcial | Sem gateway real; CA#2 "Pagamento não aprovado" não aplicável; sem e-mail |
| 10 | Feito | Sem e-mail a cada mudança de status |
| 11 | Feito (PIT II) | Cupons com validade e limite de uso; 1 por pedido |
| 12 | Feito | Foto por URL (sem upload JPG/PNG 5 MB) |
| 13, 14 | Feito | Painel atualiza a cada 60 s |
| 15 | **A implementar** | Depende de envio de e-mail |

As limitações acima são declaradas de propósito: a documentação deve refletir o que o sistema realmente faz.

## Backlog revisado
Ver `PIT_I_corrigido.docx` (tabela da seção 9).
