> ⚠️ EchoTrace is currently in early development and APIs may evolve between releases.

<div align="center">

# 🌌 EchoTrace

_Infrastructure observability tells you how your system behaves._  
**· Business observability tells you how your company performs ·**

✨ **Open-source business observability platform** for tracking business events, funnels, conversions, and customer journeys.

<br />

[![Documentation](https://img.shields.io/badge/Documentation-📖-blueviolet?style=for-the-badge)](https://tluizp.github.io/EchoTraceDoc/)

📦 [**Get Started**](https://tluizp.github.io/EchoTraceDoc/) · 🛠️ [**Features**](#key-features) · 🏗️ [**Architecture**](#architecture)

<br />

[![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/version-v0.1.0-green?style=flat-square)](https://github.com/tluizp/EchoTrace/releases)
[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.x_/_3.x-brightgreen?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/)

</div>

---

## Why EchoTrace?

Traditional observability tells you:

* CPU usage
* Memory consumption
* Request latency
* Error rates

But your business needs answers like:

* How many orders were actually created?
* How many payments were approved?
* Where are customers abandoning the checkout flow?
* Which business operations are failing most often?

Logs are unstructured.

Metrics are aggregated.

Traces understand requests.

**EchoTrace understands business events.**

---

## What is EchoTrace?

EchoTrace transforms application execution into structured business events.

With a single annotation, you can capture:

* Business operations
* Request attributes
* Return values
* Execution time
* Errors
* Custom business metadata

And send everything to:

* HTTP endpoints
* Kafka
* Elasticsearch
* Datadog
* Custom destinations

without coupling your application to any observability platform.

---

## Architecture

```text

┌──────────────────────────────┐
│      Spring Application      │
└──────────────┬───────────────┘
               │
               ▼
          @EchoTrace
               │
               ▼
      Business Event Layer
               │
               ▼
         EventPublisher
               │
      ┌────────┼────────┐
      ▼        ▼        ▼
    HTTP     Kafka    Custom
               │
               ▼
┌──────────────────────────────┐
│     EchoTrace Collector      │
└──────────────┬───────────────┘
               │
               ▼
      Event Processing Engine
               │
 ┌─────────────┼─────────────┐
 ▼             ▼             ▼
Metrics     Funnels      Journeys
               │
               ▼
       Business Dashboards
               │
               ▼
      Actionable Insights

```

---

## Modules

### echotrace-core

Core contracts, annotations and event model.

### echotrace-spring-boot-starter

Spring Boot auto-configuration and event interception.

### echotrace-collector

Event ingestion service responsible for storing and processing business events.

---

## Key Features

* Zero configuration startup
* Structured business events
* Automatic success and error tracking
* Event enrichment via Telemetry API
* Pluggable publishers
* No vendor lock-in
* Business-focused observability
* Versioned business outcomes and customer journeys
* Financial impact and deployment correlation metadata

## Business outcomes

EchoTrace 2.0 events can describe what an operation delivered to the business,
while remaining compatible with events that do not contain business context.

```java
@EchoTrace(
    name = "payment.processed",
    outcome = "payment.approval",
    journey = "order.checkout",
    stage = "payment",
    correlationId = "request.orderId",
    value = "request.amount",
    currency = "BRL"
)
public Payment approve(PaymentRequest request) {
    // A dynamic reason or other context can be added during execution.
    Telemetry.reason("ACQUIRER_TIMEOUT");
    return paymentService.approve(request);
}
```

Deployment metadata can be supplied by the delivery pipeline:

```yaml
echotrace:
  service-version: ${APP_VERSION:unknown}
  deployment-id: ${DEPLOYMENT_ID:unknown}
  commit-sha: ${GIT_COMMIT:unknown}
```

The collector stores outcomes, journey identifiers, values and deployment
metadata in queryable columns. Database schema changes are managed by Flyway.

### Reconstructing a journey

The collector exposes the ordered business and technical context of one operation:

```http
GET /api/journeys/{journeyId}
```

The response consolidates the journey status, elapsed time, affected value when
the journey failed, and every event with its trace and deployment correlation.

---

## Documentation

Full documentation:

https://tluizp.github.io/EchoTraceDoc/

---

## License

Apache License 2.0
