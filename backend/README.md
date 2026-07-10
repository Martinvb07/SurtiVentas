# SurtiVentas — Backend

El motor de SurtiVentas: recibe las peticiones del frontend y del portal del comprador, aplica las reglas del negocio y es el único que habla directamente con la base de datos.

## Cómo está pensado

Todo el negocio gira alrededor de dos ideas centrales:

**Quién puede hacer qué.** Cada persona que entra al sistema tiene un rol (administrador, vendedor, bodeguero, conductor, facturador o comprador), y ese rol determina qué información puede ver y qué acciones puede ejecutar. El inicio de sesión no solo confirma que eres tú: también le dice al resto del sistema qué puertas puedes abrir.

**El pedido nunca se salta pasos.** Un pedido nace cuando un vendedor lo toma, y de ahí solo puede avanzar por un camino permitido: aprobación, alistamiento en bodega, entrega en ruta, facturación y pago. No es posible marcar un pedido como entregado si nunca pasó por bodega, ni facturar algo que no se entregó. Cada movimiento queda registrado con quién lo hizo y cuándo, así que en cualquier momento se puede reconstruir la historia completa de un pedido.

## Qué hay hoy

Por ahora el backend cubre el ingreso de usuarios y el ciclo de vida del pedido descrito arriba, con su registro de auditoría. Sobre esta base se irán construyendo los módulos de negocio completos: catálogo de productos e inventario, proveedores y compras (incluyendo lectura automática de facturas), rutas de vendedores y conductores, facturación y cartera, y notificaciones que avisan a cada rol cuando algo requiere su atención.
