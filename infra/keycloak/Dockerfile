# =============================================================================
# MedBook — Keycloak con tema custom
#
# Strategia produzione: il tema viene copiato nell'immagine al build time.
# Non servono volumi in produzione — l'immagine è autosufficiente.
# =============================================================================
FROM quay.io/keycloak/keycloak:26.0.0

# Copia il tema nella directory dei temi di Keycloak
COPY themes/medbook /opt/keycloak/themes/medbook

# Ottimizza Keycloak per il container (build delle configurazioni statiche)
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
