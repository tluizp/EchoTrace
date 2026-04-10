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

👉 Logs são desestruturados

👉 Métricas são agregadas

👉 Traces não entendem o negócio

Falta contexto.

---

## 💡 A solução

O **EchoTrace** transforma qualquer método da sua aplicação em um **evento de negócio estruturado** — automaticamente.

* Sem acoplamento.
* Sem dependência de infra.
* Sem complexidade.

---

## ⚡ Quick Start

Adicione a dependência:

GRADLE
```gradle
    implementation 'io.echotrace:echotrace-spring-boot-starter:1.0.0'
```

MAVEN
```xml
    <dependency>
        <groupId>io.echotrace</groupId>
        <artifactId>echotrace-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
```
Anote seu método:

```java
@EchoTrace(name = "criacao_pedido", capture={"request.cpf"}, captureReturn = true)
public Pedido criarPedido(PedidoRequest request) {
    return service.criar(request);
}
```

Pronto.

👉 Você já está gerando eventos.

---

## 📦 O que é gerado

Caso de sucesso
```json
{
  "event": "criacao_pedido",
  "timestamp": "2026-04-07T20:03:13Z",
  "durationMs": 8,
  "metadata": {
    "class": "PedidoService",
    "method": "criarPedido",
    "methodReturn": {"pix": "Teste12345"},
    "request.cpf": "123.456.789-01"
  },
  "traceId": "28a9c1fb-97d2-4c12-b067-e45863bc4a35",
  "spanId": "5161f3d2-a1a5-405c-a0e4-87f4313a6ef0"
}
```

Caso de falha
```json
{
  "event": "criacao_pedido",
  "timestamp": "2026-04-07T20:02:29Z",
  "durationMs": 8,
  "metadata": {
    "errorType": "RuntimeException",
    "errorMessage": "Erro ao consultar API externa",
    "errorStack": ["..."],
    "class":"PedidoService"
  },
  "traceId": "92d1a9f2-881c-489d-92a2-09f5295fbbf4",
  "spanId": "35906647-1c2e-4317-b6c4-8f1152b17a2c"
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

| Situação                    | Resultado                 |
|-----------------------------|---------------------------|
| Nenhuma configuração        | Log automático no console |
| `collector-url` configurado | Envio HTTP                |
| Publisher customizado       | Total controle            |

---

## 🌐 Configuração HTTP

```properties
echotrace.collector-url= http://localhost:3001
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

> Seu sistema já gera dados. Você só não está ouvindo.

**EchoTrace transforma execução em significado.**
