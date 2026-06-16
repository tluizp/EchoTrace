# 🚀 EchoTrace

<p align="center">
  <h3 align="center">Observe your business. Not just your infrastructure.</h3>
</p>

<p align="center">
  Open-source business observability platform for tracking business events, funnels, conversions and customer journeys.
</p>

<p align="center">
  <a href="https://tluizp.github.io/EchoTraceDoc/">📖 Documentation</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/license-Apache%202.0-blue">
  <img src="https://img.shields.io/badge/Java-17-orange">
  <img src="https://img.shields.io/badge/Spring_Boot-2.x-green">
</p>

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
Spring Application
        │
        ▼
   @EchoTrace
        │
        ▼
 Business Event
        │
        ▼
 EventPublisher
        │
 ┌──────┼───────────┐
 ▼      ▼           ▼
HTTP   Kafka   Custom Publisher
        │
        ▼
  EchoTrace Collector
        │
        ▼
 Analytics & Dashboards
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

## Vision

EchoTrace is evolving beyond event collection.

Future capabilities include:

* Business Funnels
* Customer Journeys
* Conversion Analytics
* Event Correlation
* Business Metrics Dashboards
* Real-time Monitoring

---

## License

Apache License 2.0
