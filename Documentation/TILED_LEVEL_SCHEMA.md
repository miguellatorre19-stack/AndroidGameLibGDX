# Tiled Level Schema (KeyFinder)

Este contrato define como construir niveles en Tiled para que el parser del juego sea predecible.

## 1) Capas recomendadas

- `collisions_solid`: colisiones bloqueantes (paredes, puertas cerradas).
- `triggers`: zonas de trigger no bloqueantes (salida, eventos, etc).
- `spawns` (opcional): puntos de spawn de jugador, enemigos y trampas.

Compatibilidad legacy:
- Si no existe `collisions_solid`, el juego usa `collisions` o `colisions`.
- Si no existe `triggers`, intenta detectar `exit` en la capa de colisiones legacy.

## 2) Clases de objetos (`class`)

Usar siempre la propiedad de objeto `class` en Tiled:

- `door`: puerta que puede bloquear.
- `exit`: zona de salida de nivel.
- `solid`: obstáculo estático (si quieres clasificar explícitamente).
- `spawn_player` (opcional).
- `spawn_enemy` (opcional).
- `trap_arrow` (opcional).

## 3) Propiedades recomendadas por clase

### `door`
- `doorID` (string): identificador lógico de puerta.
- `requiresKey` (bool): si requiere llave para abrirse.

### `exit`
- `id` (string): valor recomendado `exit`.
- `exit_id` (string, opcional): identificador semántico.

## 4) Convenciones de geometría

- Priorizar `Rectangle` para colisiones.
- Si usas `Polygon`, mantenerlos simples/convexos siempre que sea posible.
- Evitar polígonos auto-intersectados.
- Mantener objetos alineados al grid (`16x16`) para evitar bordes ambiguos.

## 5) Notas de compatibilidad actual del parser

El parser actual prioriza:
1. `class=exit` para detectar salida.
2. fallback legacy por `id=exit` o nombre `trampilla`.

Para puertas prioriza:
1. `class=door`.
2. fallback legacy por `type/name` y `doorID/requiresKey`.
