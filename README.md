# Tracking-Kotlin

Proyecto de demostración con la API de Google Maps, basado en una arquitectura CLEAN con MVVM.

## Funciones de la aplicación

- Los usuarios pueden iniciar, pausar, cancelar y guardar un recorrido; este persiste en la BD de la App.
- Los usuarios pueden ver la lista de los últimos recorridos, desde esta lista, al hacer swipe se puede eliminar el recorrido.
- Los usuarios pueden hacer clic en cualquier recorrido para ver y compartir los detalles del mismo.

## Arquitectura de la aplicación
Basado en la arquitectura Clean y el patrón MVVM.

## La aplicación incluye los siguientes componentes principales:
- Un ViewModel que proporciona datos específicos para la interfaz de usuario.
- La interfaz de usuario, que muestra una representación visual de los datos en ViewModel.

## Paquetes de aplicaciones
- db.
- di.
- ui.
- util.

## Especificaciones de la aplicación
- SDK mínimo 16.
- Java (en la rama maestra) y Kotlin (en la rama kotlin_support).
- Arquitectura MVVM.
- Componentes de la arquitectura de Android (LiveData, ViewModel, componente de navegación, ConstraintLayout).