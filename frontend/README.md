# SurtiVentas — Frontend

Interfaz web de SurtiVentas: lo que ve cada rol al entrar a la plataforma, desde el vendedor que arma un pedido en la calle hasta el gerente que revisa sus indicadores del día.

## Cómo está pensado

La aplicación no es una sola pantalla genérica: cada rol tiene su propia vista, con solo la información y las acciones que le corresponden. Un vendedor no ve lo mismo que un facturador, y un bodeguero no puede entrar a pantallas de administración. Eso se resuelve con rutas protegidas: al iniciar sesión, la aplicación sabe quién eres y a dónde puedes entrar.

Todo vive dentro de un mismo "armazón" (sidebar de navegación + barra superior) que se mantiene fijo mientras el contenido central cambia según la sección — así cada rol siente que tiene su propia herramienta, aunque todos comparten la misma base visual.

## Identidad visual

SurtiVentas tiene un lenguaje visual propio, pensado para uso en bodega y en campo: azul corporativo profundo como color de marca, ámbar cálido para las acciones importantes, tipografía Manrope, tarjetas con bordes suaves y densidad cómoda para pantallas táctiles. La idea es que se sienta como una herramienta de trabajo seria, no como un panel de administración genérico.

## Qué hay hoy

Por ahora la interfaz cubre el ingreso a la plataforma (inicio de sesión) y el armazón general de navegación que recibirá los módulos de cada rol. A medida que se construyan los módulos de negocio (catálogo, pedidos, bodega, rutas, facturación, portal del comprador), cada uno se integra dentro de esa misma estructura.
