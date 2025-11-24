# ♾️ Participaciones Ilimitadas - Concursos Hype Music

## 🎯 Regla Principal

**TODOS los concursos permiten participaciones ilimitadas**

Los usuarios pueden participar tantas veces como quieran en cualquier concurso, sin restricciones.

---

## ✅ Qué Significa Esto

### Para Concursos Rápidos
- Puedes subir múltiples covers en "Mejor Cover de la Semana"
- Puedes participar todos los días si quieres
- Cada video cuenta como una entrada independiente
- Sin límite de participaciones por usuario

### Para Concursos de Alto Impacto
- Puedes subir varios videos musicales en "Mejor Video Musical"
- Puedes mejorar y resubir tu trabajo
- Cada producción cuenta como entrada separada
- Sin límite de participaciones por usuario

---

## 🎨 Interfaz de Usuario

### Indicador Visual
En cada tipo de concurso se muestra:
```
♾️ Participaciones ilimitadas
```

### Ubicación
- Aparece en la card de descripción del concurso
- Visible tanto en Rápidos como en Alto Impacto
- Color: Amarillo (primary) para destacar

---

## 💡 Ventajas

### Para Usuarios
1. **Más oportunidades de ganar** - Más entradas = más chances
2. **Experimentación** - Prueba diferentes estilos
3. **Mejora continua** - Perfecciona tu arte
4. **Sin presión** - No hay "una sola oportunidad"

### Para la Plataforma
1. **Más contenido** - Mayor actividad
2. **Engagement** - Usuarios más activos
3. **Calidad** - Los usuarios mejoran con práctica
4. **Descubrimiento** - Más talento visible

---

## 📊 Ejemplo de Uso

**Usuario: DJ_Neon**
**Concurso: Mejor Cover de la Semana**

```
Lunes 10:00 AM    → Sube cover de "Bohemian Rhapsody"
Martes 3:00 PM    → Sube cover de "Imagine"  
Miércoles 8:00 PM → Sube cover de "Hotel California"
Viernes 2:00 PM   → Sube cover de "Wonderwall"
```

**Resultado:** 4 participaciones activas, todas compiten

---

## 🔧 Implementación Técnica

### Data Model
```kotlin
data class Contest(
    ...
    val allowMultipleEntries: Boolean = true
)
```

### UI
- Indicador "♾️ Participaciones ilimitadas"
- Visible en ambos tipos de concursos
- Sin restricciones en el backend

---

**Estado:** ✅ Implementado
**Aplica a:** Todos los concursos (Rápidos y Alto Impacto)
