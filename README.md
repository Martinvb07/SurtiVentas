<div align="center">

# 📦 SurtiVentas

**ERP de distribución mayorista — de la bodega a la tienda, sin hojas de cálculo**

*Compras a proveedores, inventario, pre-venta en campo, alistamiento, entrega y facturación, todo en una sola plataforma*

---

[![Angular](https://img.shields.io/badge/Angular-20-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.dev/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

</div>

---

## 🌟 ¿Qué es SurtiVentas?

**SurtiVentas** es un ERP pensado para distribuidoras mayoristas (al estilo Surtitodo): productos que entran de proveedores, se almacenan en bodega, se ofrecen a tiendas a través de vendedores en ruta, se despachan, se entregan y finalmente se facturan y cobran.

En lugar de coordinar el negocio entre cuadernos, WhatsApp y hojas de cálculo sueltas, cada pedido viaja por un flujo único y auditable — desde que el vendedor lo toma en la tienda hasta que se paga la factura — con cada rol viendo exactamente lo que le corresponde.

---

## 🎯 Problema que resuelve

Una distribuidora que crece deja de caber en procesos manuales:

- Pedidos que se pierden entre el vendedor, la bodega y el conductor
- Nadie sabe en qué estado quedó un pedido ni quién lo movió
- Inventario desactualizado porque las entradas de proveedor no quedan registradas
- Cartera y cuentas por cobrar que se llevan aparte, sin trazabilidad
- Cada rol (vendedor, bodeguero, conductor, facturador) trabajando a ciegas del resto

SurtiVentas centraliza todo el ciclo en una máquina de estados única, con auditoría de quién hizo qué y cuándo.

---

## 🚀 ¿Cómo funciona?

El corazón del sistema es el ciclo de vida del pedido:

```
Creado (Vendedor) → Pendiente de aprobación → Aprobado
   → En alistamiento (Bodeguero) → Alistado
   → Asignado a ruta (Conductor) → Entregado / Novedad
   → Facturado (Facturador) → Pagado / Cartera pendiente
```

Cada cambio de estado se valida contra el rol que lo ejecuta, queda registrado en un historial de auditoría, y (en fases posteriores) dispara una notificación en tiempo real al siguiente responsable de la cadena.

---

## 👥 Roles del sistema

| Rol | Qué hace |
|---|---|
| 🧑‍💼 **Administrador / Gerente** | Configuración global, usuarios, precios, rutas, metas y reportes |
| 🧑‍🤝‍🧑 **Vendedor** | Visita tiendas en su ruta, toma pedidos, consulta stock y comisiones |
| 📦 **Bodeguero / Alistador** | Alista pedidos aprobados y controla entradas de inventario |
| 🚚 **Conductor / Repartidor** | Entrega pedidos alistados y reporta novedades |
| 🧾 **Facturador / Cajero** | Genera facturas, registra pagos y gestiona cartera |
| 🏪 **Comprador (tienda)** | Portal de autoservicio: historial, estado de cuenta, repetir pedido |

---

## 📍 Estado actual del proyecto

Este repositorio contiene la **fundación** del sistema: el esqueleto sobre el que se construyen todos los módulos de negocio.

### ✅ Completado

- [x] Monorepo con backend (Spring Boot 3 / Java 21) y frontend (Angular 20)
- [x] Autenticación JWT + refresh tokens rotativos, BCrypt, RBAC para 6 roles
- [x] Máquina de estados del pedido con validación por rol y auditoría completa
- [x] Modelo de datos versionado con Flyway
- [x] Tema visual SurtiVentas (Angular Material, paleta e identidad propias)
- [x] Login funcional, guardas de ruta y shell de aplicación (sidebar + topbar)
- [x] Documentación de API con Swagger/OpenAPI
- [x] Docker Compose (frontend, backend, MySQL, placeholder de OCR) + CI en GitHub Actions

### 🔜 Próximamente

- [ ] Catálogo de productos e inventario (lotes, vencimientos, stock mínimo)
- [ ] Proveedores, órdenes de compra y escaneo OCR de facturas (Tesseract)
- [ ] Rutas de vendedores/conductores, geolocalización y comisiones
- [ ] Gestión de clientes (tiendas), cartera y cuentas por cobrar
- [ ] Toma de pedidos offline-first (IndexedDB) y carrito de pre-venta
- [ ] Kanban de alistamiento, despacho y mapa en vivo de conductores
- [ ] Facturación formal y registro de pagos
- [ ] Portal de autoservicio del comprador
- [ ] Dashboards por rol con Chart.js
- [ ] Notificaciones en tiempo real vía WebSocket/STOMP
- [ ] Alta disponibilidad: múltiples instancias de backend detrás de Nginx

---

## 🛠️ Tecnologías utilizadas

### 🖥️ Frontend

| Tecnología | Para qué se usa |
|---|---|
| ![Angular](https://img.shields.io/badge/Angular-DD0031?logo=angular&logoColor=white) | Framework principal, componentes standalone |
| ![Angular Material](https://img.shields.io/badge/Angular_Material-DD0031?logo=angular&logoColor=white) | Sistema de componentes UI, tema M3 propio |
| ![RxJS](https://img.shields.io/badge/RxJS-B7178C?logo=reactivex&logoColor=white) | Streams HTTP y estado reactivo |
| ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white) | Tipado estático en toda la app |
| ![SCSS](https://img.shields.io/badge/SCSS-CC6699?logo=sass&logoColor=white) | Estilos y tokens de diseño |

### ⚙️ Backend

| Tecnología | Para qué se usa |
|---|---|
| ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=springboot&logoColor=white) | API REST, capas Controller → Service → Repository |
| ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=springsecurity&logoColor=white) | Autenticación JWT y autorización por rol |
| ![Hibernate](https://img.shields.io/badge/Hibernate_JPA-59666C?logo=hibernate&logoColor=white) | Persistencia de datos |
| ![Flyway](https://img.shields.io/badge/Flyway-CC0200?logo=flyway&logoColor=white) | Versionado incremental de base de datos |
| ![MapStruct](https://img.shields.io/badge/MapStruct-59666C?logoColor=white) | Mapeo entidad ↔ DTO |
| ![Swagger](https://img.shields.io/badge/OpenAPI-85EA2D?logo=swagger&logoColor=black) | Documentación de la API |

### 🗄️ Datos e infraestructura

| Tecnología | Para qué se usa |
|---|---|
| ![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white) | Base de datos relacional |
| ![Docker](https://img.shields.io/badge/Docker_Compose-2496ED?logo=docker&logoColor=white) | Orquestación multi-contenedor |
| ![Nginx](https://img.shields.io/badge/Nginx-009639?logo=nginx&logoColor=white) | Servido del frontend y reverse proxy a la API |
| ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?logo=githubactions&logoColor=white) | CI para backend y frontend |
| 🔤 **Tesseract OCR** | Escaneo de facturas de proveedor (Fase 2) |

---

## 📂 Estructura del proyecto

```
SurtiVentas/
│
├── 📁 backend/                  → Spring Boot 3, Java 21, Maven
│   └── src/main/java/com/surtiventas/backend/
│       ├── config/              → Seguridad, CORS, OpenAPI
│       ├── security/jwt/        → Emisión y validación de JWT
│       ├── user/                → Usuarios y roles
│       ├── auth/                → Login, refresh tokens, registro
│       ├── order/                → Pedido, máquina de estados, auditoría
│       └── common/exception/    → Manejo global de errores
│
├── 📁 frontend/                 → Angular 20 (standalone)
│   └── src/app/
│       ├── core/auth/           → Servicio de auth, guardas, interceptor JWT
│       ├── shared/layout/       → Shell, sidebar, topbar
│       └── features/            → Módulos por ruta (auth, dashboard, …)
│
└── 📁 ocr/                      → Placeholder del servicio Tesseract (Fase 2)
```

---

<div align="center">

Hecho con ☕ para modernizar la distribución mayorista

**[Reportar un problema](https://github.com/Martinvb07/SurtiVentas/issues)** · **[Solicitar una función](https://github.com/Martinvb07/SurtiVentas/issues/new)**

</div>
