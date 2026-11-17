# Diagramas UML — Cupcakes Gourmet

Diagramas em Mermaid: o GitHub renderiza automaticamente ao abrir este arquivo.
Foram conferidos contra o código (pacote `br.com.cupcakes`).

## 1. Casos de uso

```mermaid
flowchart LR
    C(["👤 Cliente"])
    A(["👤 Administrador"])
    subgraph S["Sistema — Cupcakes Gourmet"]
        UC1([Cadastrar-se · HU-01])
        UC2([Fazer login · HU-02])
        UC3([Ver vitrine · HU-03])
        UC4([Filtrar por categoria · HU-04])
        UC5([Ver detalhe do produto · HU-05])
        UC6([Adicionar ao carrinho · HU-06])
        UC7([Editar carrinho · HU-07])
        UC8([Calcular frete · HU-08])
        UC9([Finalizar pedido · HU-09])
        UC10([Acompanhar pedidos · HU-10])
        UC11([Aplicar cupom · HU-11])
        UC15([Recuperar senha · HU-15 — a implementar])
        UC12([Gerenciar catálogo · HU-12])
        UC13([Gerenciar estoque · HU-13])
        UC14([Gerenciar pedidos · HU-14])
    end
    C --- UC1 & UC2 & UC3 & UC4 & UC5 & UC6 & UC7 & UC8 & UC9 & UC10 & UC11 & UC15
    A --- UC2 & UC12 & UC13 & UC14
    UC9 -. "«include»" .-> UC8
    UC9 -. "«include»" .-> UC2
    UC4 -. "«extend»" .-> UC3
    UC11 -. "«extend»" .-> UC9
```

Observação: o administrador faz login pelo mesmo caso de uso (HU-02); o perfil `ADMIN` libera as rotas `/admin/**`.

## 2. Classes (domínio e camadas)

```mermaid
classDiagram
    direction LR
    class Usuario {
        -Long id
        -String nome
        -String email
        -String senhaHash
        -Perfil perfil
        -int tentativasFalhas
        -LocalDateTime bloqueadoAte
    }
    class Categoria {
        -Long id
        -String nome
    }
    class Cupcake {
        -Long id
        -String nome
        -String descricao
        -BigDecimal preco
        -String fotoUrl
        -String alergenicos
        -int estoque
        -boolean ativo
        +isDisponivel() boolean
    }
    class Pedido {
        -Long id
        -LocalDateTime dataHora
        -StatusPedido status
        -FormaPagamento formaPagamento
        -String cep
        -String enderecoEntrega
        -BigDecimal subtotal
        -BigDecimal frete
        -BigDecimal desconto
        -BigDecimal total
        -int prazoEntregaDias
        -LocalDate vencimentoBoleto
        +adicionarItem(ItemPedido)
    }
    class ItemPedido {
        -Long id
        -int quantidade
        -BigDecimal precoUnitario
        +getSubtotal() BigDecimal
    }
    class Cupom {
        -Long id
        -String codigo
        -int percentual
        -LocalDate validade
        -int limiteUso
        -int usos
        -boolean ativo
    }
    class Perfil { <<enumeration>> CLIENTE ADMIN }
    class StatusPedido { <<enumeration>> AGUARDANDO_PAGAMENTO EM_PREPARACAO SAIU_PARA_ENTREGA ENTREGUE CANCELADO }
    class FormaPagamento { <<enumeration>> CARTAO_CREDITO PIX BOLETO }

    class UsuarioService { +cadastrar() +autenticar() }
    class PedidoService { +finalizar() +atualizarStatus() +listarDoUsuario() +listarTodos() }
    class FreteService { +calcular(cep, subtotal) Frete }
    class CupcakeService
    class Carrinho { +adicionar() +alterar() +remover() +subtotal() }

    Usuario "1" --> "0..*" Pedido : faz
    Pedido "1" *-- "1..*" ItemPedido : contém
    ItemPedido "0..*" --> "1" Cupcake : refere-se a
    Cupcake "0..*" -- "0..*" Categoria : pertence a
    Pedido "0..*" --> "0..1" Cupom : usa
    class CupomService { +validar(codigo) +calcularDesconto() +registrarUso() }
    CupomService ..> Cupom
    PedidoService ..> CupomService : usa
    Usuario --> Perfil
    Pedido --> StatusPedido
    Pedido --> FormaPagamento
    PedidoService ..> Pedido
    PedidoService ..> FreteService : usa
    UsuarioService ..> Usuario
    CupcakeService ..> Cupcake
    Carrinho ..> Cupcake
```

## 3. Sequência — Finalizar pedido (HU-09)

```mermaid
sequenceDiagram
    actor C as Cliente
    participant PC as PedidoController
    participant PS as PedidoService
    participant FS as FreteService
    participant DB as MySQL (repositories)

    C->>PC: POST /checkout (cep, endereço, forma de pagamento)
    PC->>PS: finalizar(usuarioId, itens, cep, endereço, forma)
    PS->>PS: valida carrinho, endereço e forma de pagamento
    PS->>DB: busca usuário
    loop para cada item do carrinho
        PS->>DB: relê cupcake (estoque atual)
        alt estoque insuficiente ou inativo
            PS-->>PC: NegocioException("Estoque insuficiente...")
            PC-->>C: volta ao checkout com mensagem de erro (rollback)
        else estoque ok
            PS->>DB: baixa estoque (HU-13 RN#1)
        end
    end
    opt cupom aplicado (HU-11)
        PS->>DB: valida cupom (validade, limite de uso)
        PS->>PS: desconto = subtotal × percentual
        PS->>DB: registra mais um uso do cupom
    end
    PS->>FS: calcular(cep, subtotal − desconto)
    alt CEP não atendido
        FS-->>PS: NegocioException("Entrega não disponível...")
        PS-->>PC: erro (transação desfeita)
        PC-->>C: mensagem de erro
    else CEP atendido
        FS-->>PS: frete (valor, prazo)
        PS->>PS: total = subtotal + frete; boleto → vencimento +2 dias úteis
        PS->>DB: salva pedido (status AGUARDANDO_PAGAMENTO)
        PS-->>PC: pedido
        PC-->>C: redireciona para "Meus pedidos" com confirmação
    end
```

## 4. Atividades — Do acesso ao pedido concluído

```mermaid
flowchart TD
    I((●)) --> V[Ver vitrine]
    V --> F{Filtrar por categoria?}
    F -- sim --> V
    F -- não --> D[Ver detalhe do cupcake]
    D --> E{Tem estoque?}
    E -- não --> V
    E -- sim --> AC[Adicionar ao carrinho]
    AC --> M{Continuar comprando?}
    M -- sim --> V
    M -- não --> CAR[Revisar carrinho / calcular frete]
    CAR --> CUP{Tem cupom?}
    CUP -- sim --> AP[Aplicar cupom] --> CAR
    CUP -- não --> L{Está logado?}
    L -- não --> LOG[Login ou cadastro]
    LOG --> OK{Credenciais válidas?}
    OK -- não --> LOG
    OK -- sim --> CK[Checkout: CEP, endereço, pagamento]
    L -- sim --> CK
    CK --> V2{Dados e estoque válidos?}
    V2 -- não --> ERR[Exibir mensagem de erro] --> CK
    V2 -- sim --> P[Registrar pedido e baixar estoque]
    P --> AD[Admin atualiza status]
    AD --> ST{Status}
    ST -- Cancelado --> DEV[Devolver estoque] --> FIM((◉))
    ST -- Entregue --> FIM
```
