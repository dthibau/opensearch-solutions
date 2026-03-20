export OTEL_SERVICE_NAME=order-service
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:21890
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=none
export OTEL_LOGS_EXPORTER=none
#export OTEL_JAVAAGENT_DEBUG=true

java -javaagent:opentelemetry-javaagent.jar \
     -Dotel.traces.exporter=otlp \
     -Dotel.propagators=tracecontext,baggage \
     -Dotel.exporter.otlp.endpoint=http://localhost:21890 \
     -Dotel.exporter.otlp.protocol=grpc \
     -Dotel.metrics.exporter=none \
     -Dotel.logs.exporter=none \
     -Dotel.service.name=order-service \
     -jar target/otel-demo-*.jar