# Banco XYZ - Tolerancia a fallos y arquitectura de eventos (Exp3 S7)

## Objetivo

Fortalecer el proyecto de microservicios del Banco XYZ (patrón BFF: `ms-core-banco`,
`bff-web`, `bff-movil`, `bff-cajeros`) con:

1. **Tolerancia a fallos** en las llamadas síncronas entre los BFF y `ms-core-banco`,
   usando Resilience4j (Circuit Breaker + Retry).
2. **Arquitectura orientada a eventos** para las transacciones del sistema, usando
   Apache Kafka, que desacopla el procesamiento asíncrono de efectos secundarios
   (comprobante/notificación) del flujo transaccional principal.

## Arquitectura de eventos elegida

Se optó por una arquitectura orientada a eventos con el patrón **Publish/Subscribe**
sobre **Apache Kafka**. El retiro de dinero (única operación transaccional del
sistema, disponible en el canal `bff-cajeros`) se mantiene **síncrono** vía REST,
porque el cliente necesita confirmación inmediata de su operación. Ese llamado
síncrono está protegido con Resilience4j.

Una vez que `ms-core-banco` confirma y persiste el retiro, publica un evento
`TransaccionRealizada` en el tópico `transacciones-realizadas`. Cualquier
consumidor puede suscribirse a ese evento sin acoplarse al productor; en esta
entrega, `bff-cajeros` lo consume para simular la generación de un comprobante.
Este diseño permite agregar a futuro más consumidores (notificaciones por
correo/SMS, auditoría, sistemas antifraude) **sin modificar** `ms-core-banco`
ni `bff-cajeros`, demostrando la escalabilidad del patrón.

**Diagrama de la solución:** ver `docs/diagrama-solicitud.png` y
`docs/diagrama-confirmacion.png` (flujo síncrono de retiro con Resilience4j, y
flujo asíncrono del evento de confirmación vía Kafka).

## Estructura del proyecto

```
├── banco/                  # ms-core-banco: backend central (Spring Boot 4.0.9-SNAPSHOT)
├── banco_web/banco/        # bff-web: canal Web (solo lectura)
├── banco_movil/banco/      # bff-movil: canal Móvil (solo lectura)
├── banco_cajero/banco/     # bff-cajeros: canal Cajero (retiro, Spring Boot 3.3.5)
├── docker-compose.yml      # Kafka + Zookeeper + Kafka UI
└── docs/                   # Diagramas y capturas de evidencia
```

## Decisiones técnicas relevantes

- **`bff-cajeros` corre en Spring Boot 3.3.5** (el resto de los módulos sigue en
  `4.0.9-SNAPSHOT`): la librería `resilience4j-spring-boot3` aún no es totalmente
  compatible con esa build preview de Spring Boot 4. Como los microservicios se
  comunican por HTTP/REST, no hay conflicto entre versiones distintas de Spring
  Boot en cada módulo.
- **`ms-core-banco` define manualmente el bean `KafkaTemplate`** (clase
  `KafkaProducerConfig`) en lugar de depender de la auto-configuración de
  Spring Boot, por la misma razón: la auto-configuración de Kafka aún no está
  madura en `4.0.9-SNAPSHOT`.
- El Circuit Breaker de `bff-cajeros` ignora explícitamente los errores 4xx
  (`HttpClientErrorException`) como saldo insuficiente o cuenta inexistente,
  ya que son errores de negocio, no fallas de infraestructura.

## Instrucciones de ejecución

1. **Levantar Kafka:**
   ```
   docker compose up -d
   ```
   Verifica con `docker ps` que estén `zookeeper`, `kafka` y `kafka-ui` corriendo.
   Kafka UI queda disponible en `http://localhost:8085`.

2. **Levantar los microservicios**, en este orden:
   1. `ms-core-banco` (puerto 8090)
   2. `bff-cajeros` (puerto 8094, HTTPS)
   3. `bff-web` (puerto 8091) y `bff-movil` (puerto 8092), si se quieren probar

3. **Probar el retiro** (credenciales Basic Auth: `cajero-client` / `cajero123`):
   ```
   curl.exe -k -u cajero-client:cajero123 -X POST https://localhost:8094/cajero/cuentas/{id}/retiro -H "Content-Type: application/json" -d '{\"monto\":1000}'
   ```

4. **Probar la tolerancia a fallos:** apagar `ms-core-banco` y repetir el retiro
   varias veces seguidas. Las primeras respuestas tardan ~1 s (reintentos);
   luego de que el circuito se abre, las respuestas son inmediatas con un
   mensaje de servicio no disponible. Al reactivar `ms-core-banco` y esperar
   ~10 s, el circuito vuelve a cerrarse.

5. **Verificar el evento Kafka:** tras un retiro exitoso, revisar la consola de
   `ms-core-banco` (evento publicado), la consola de `bff-cajeros` (comprobante
   generado) y/o el tópico `transacciones-realizadas` en Kafka UI.

## Evidencia de ejecución

Ver carpeta `docs/` para capturas de: retiro exitoso, comportamiento del
circuit breaker (antes/después de abrirse), evento publicado y consumido, y
mensajes en Kafka UI.