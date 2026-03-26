#!/bin/bash
# Installe le plugin Prometheus si absent, puis démarre OpenSearch

PLUGIN="prometheus-exporter"
PLUGIN_URL="https://github.com/opensearch-project/opensearch-prometheus-exporter/releases/download/3.4.0.0/prometheus-exporter-3.4.0.0.zip"

# Vérifie si le plugin est déjà installé
if /usr/share/opensearch/bin/opensearch-plugin list | grep -q "$PLUGIN"; then
  echo "[start.sh] Plugin $PLUGIN already installed, skipping."
else
  echo "[start.sh] Installing $PLUGIN..."
  /usr/share/opensearch/bin/opensearch-plugin install --batch "$PLUGIN_URL"
  echo "[start.sh] Plugin installed."
fi

exec /usr/share/opensearch/opensearch-docker-entrypoint.sh