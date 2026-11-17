# Manual de uso — Cupcakes Gourmet

Versão 1.1 (PIT II) · Autor: Henrique Tayler Silva · RGM 33296961

Sistema web de venda de cupcakes. Funciona no computador e no celular, pelo navegador.

## 1. Acesso
- Endereço local: `http://localhost:8080` (veja o README para instalar).
- Endereço publicado: ver o link no README do repositório.

| Perfil | Como entrar |
|---|---|
| Cliente | Crie sua conta em **Cadastrar** |
| Administrador | `admin@cupcakes.com` / `Admin@123` |

## 2. Cliente

### 2.1 Vitrine e filtro
A página inicial mostra só os cupcakes com estoque. Use **Categoria** para filtrar (ex.: Chocolate).

![Vitrine](prints/01-vitrine.png)
![Filtro por categoria](prints/02-vitrine-filtro-chocolate.png)

### 2.2 Detalhe do produto
Clique no nome ou na foto. A página mostra descrição, preço, categorias e o **alerta de alergênicos**.

![Detalhe](prints/03-detalhe-alergenicos.png)

### 2.3 Criar conta e entrar
Informe nome, e-mail e senha (mínimo 8 caracteres). O e-mail não pode se repetir.
Depois de 5 senhas erradas seguidas, o acesso fica bloqueado por 15 minutos.

![Cadastro](prints/04-cadastro.png)
![E-mail repetido](prints/05-cadastro-erro-email-repetido.png)
![Login com senha errada](prints/06-login-erro-credenciais.png)

### 2.4 Carrinho
1. Na vitrine, clique em **Adicionar ao carrinho** (máximo de 50 unidades por item).
2. Em **Carrinho**, altere a quantidade e clique em **Atualizar**, ou clique na lixeira para remover.

![Item adicionado](prints/07-vitrine-item-adicionado.png)

### 2.5 Cupom de desconto
Digite o código e clique em **Aplicar cupom**. Só um cupom por pedido. Cupons de teste:

| Código | Desconto | Situação |
|---|---|---|
| BEMVINDO10 | 10% | válido |
| DOCE15 | 15% | válido |
| NATAL2025 | 20% | vencido (mostra a mensagem de erro) |

![Cupom inválido](prints/08-carrinho-cupom-erro.png)
![Cupom aplicado](prints/09-carrinho-cupom-aplicado.png)

### 2.6 Frete
Digite o CEP e clique em **Calcular frete**. A loja entrega em CEPs que começam com **387** (Patos de Minas e região). Frete de R$ 12,00, grátis acima de R$ 150,00 (já com desconto), prazo de 2 dias.

![Frete calculado](prints/10-carrinho-frete-calculado.png)
![CEP fora da área](prints/11-carrinho-cep-fora-da-area.png)

### 2.7 Finalizar pedido
Clique em **Finalizar pedido** (pede login), informe CEP, endereço e forma de pagamento (cartão, PIX ou boleto) e confirme.
O pedido nasce como **Aguardando pagamento**. No boleto, o vencimento é de 2 dias úteis.

> O pagamento é simulado: não há cobrança real (sem integração com operadora).

![Checkout](prints/12-checkout.png)

### 2.8 Meus pedidos
Mostra cada pedido com itens, desconto, frete, total e status atual.

![Meus pedidos](prints/13-meus-pedidos.png)

## 3. Administrador

### 3.1 Produtos e estoque
Menu **Admin: Produtos**. Produto com estoque 0 sai da vitrine sozinho. **Remover** tira o produto da vitrine sem apagar o histórico de pedidos.

![Produtos e estoque](prints/14-admin-produtos-estoque.png)
![Editar cupcake](prints/15-admin-editar-cupcake.png)

### 3.2 Pedidos
Menu **Admin: Pedidos**. Escolha o novo status e clique em **Salvar**. A página se atualiza a cada 60 segundos.
Cancelar um pedido devolve os itens ao estoque. Pedido cancelado não pode ser reaberto.

![Pedidos recebidos](prints/16-admin-pedidos.png)

## 4. No celular
A vitrine se ajusta à tela do celular.

![Vitrine no celular](prints/18-vitrine-celular.png)

## 5. Mensagens do sistema

| Mensagem | O que fazer |
|---|---|
| Credenciais inválidas | Confira e-mail e senha |
| Acesso bloqueado por excesso de tentativas... | Aguarde 15 minutos |
| E-mail já cadastrado | Use outro e-mail ou entre na conta |
| Cupom inválido ou expirado | Confira o código; o cupom pode ter vencido ou esgotado |
| Entrega não disponível para esta região | A loja só entrega nos CEPs atendidos |
| Estoque insuficiente para: ... | Diminua a quantidade |
| Seu carrinho está vazio | Adicione um produto antes de finalizar |

## 6. Limitações conhecidas
- Não envia e-mails (confirmação de cadastro, de pedido e de status).
- Recuperação de senha (HU-15) ainda não implementada.
- Pagamento simulado; foto do produto por URL (sem upload).
