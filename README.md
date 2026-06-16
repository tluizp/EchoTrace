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

---

## Documentation

Full documentation:

https://tluizp.github.io/EchoTraceDoc/

---

## License

Apache License 2.0
