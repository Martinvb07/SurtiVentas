-- Gives the seed stores real coordinates (Villavicencio, Meta) so the route
-- maps have points to show out of the box. New stores get coordinates from the
-- customer form.
UPDATE customer SET latitude = 4.1420000, longitude = -73.6266000 WHERE store_name = 'Tienda El Progreso';
UPDATE customer SET latitude = 4.1455000, longitude = -73.6301000 WHERE store_name = 'Autoservicio La Rebaja';
UPDATE customer SET latitude = 4.1389000, longitude = -73.6238000 WHERE store_name = 'Minimercado Las Palmas';
