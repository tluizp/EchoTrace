# 🚀 EchoTrace

> **Pare de observar apenas o sistema. Comece a observar o seu negócio.**

---

## 🧠 O problema

Hoje você sabe tudo sobre:

* CPU 🔧
* Memória 💾
* Latência ⏱️

Mas não sabe responder:

* Quantos pedidos foram realmente criados?
* Onde os usuários estão desistindo?
* Quais operações falham no fluxo de negócio?

👉 Logs não resolvem isso.
👉 Métricas não contam a história completa.

---

## 💡 A solução

O **EchoTrace** transforma qualquer método da sua aplicação em um **evento de negócio estruturado** — automaticamente.

Sem acoplamento.
Sem dependência de infra.
Sem complexidade.

---

## ⚡ Quick Start

Adicione a dependência:

```gradle
    implementation 'io.echotrace:echotrace-core:1.0.0'
    implementation 'io.echotrace:echotrace-spring-boot-starter:1.0.0'
```

Anote seu método:

```java
@EchoTrace("pedido.criado")
public Pedido criarPedido(PedidoRequest request) {
    return service.criar(request);
}
```

Pronto.

👉 Você já está gerando eventos.

---

## 📦 O que é gerado

```json
{
  "event": "pedido.criado",
  "timestamp": "2026-03-19T18:15:00Z",
  "executionTimeMs": 87,
  "data": {}
}
```

---

## 🔌 Plugável por design

Você não fica preso a nenhuma tecnologia.

A lib apenas gera o evento.
Você decide o destino.

```java
@Bean
public EventPublisher customPublisher() {
    return payload -> enviarParaQualquerLugar(payload);
}
```

---

## ⚙️ Comportamento automático

| Situação                    | Resultado      |
| --------------------------- | -------------- |
| Nenhuma configuração        | Log automático |
| `collector-url` configurado | Envio HTTP     |
| Publisher customizado       | Total controle |

---

## 🌐 Configuração HTTP

```yaml
event:
  trace:
    collector-url: http://localhost:8080/events
```

---

## 🧩 Arquitetura

* **Core** → contratos e modelo
* **Starter** → auto-configuração Spring Boot
* **Publisher** → saída plugável

---

## 🔥 Diferenciais

### ✔ Zero configuração

Adicionou a dependência → já funciona

---

### ✔ Sem lock-in

Você nunca fica preso a Kafka, HTTP ou qualquer infra

---

### ✔ Foco em negócio

Eventos estruturados, não logs genéricos

---

### ✔ Extensível

Crie seus próprios publishers:

* Kafka
* HTTP
* Elasticsearch
* Datadog
* qualquer coisa

---

## 🛠️ Casos de uso

* 📊 Observabilidade de negócio
* 🔍 Auditoria de operações
* 📈 Analytics em tempo real
* 🔐 Rastreio de dados sensíveis (PII)
* 🧪 Debug de fluxos críticos

---

## 📄 Licença

MIT

---

## 💬 Filosofia

> Seu sistema já gera dados.
> Você só não está ouvindo.

**Business Event Trace transforma execução em significado.**
