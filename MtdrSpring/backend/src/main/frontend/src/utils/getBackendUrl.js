export const getBackendUrl = async () => {
    // Aquí puedes implementar la lógica para obtener la dirección IP dinámica
    // Por ejemplo, puedes hacer una solicitud a un servicio de descubrimiento de IP
    // o leer la dirección IP desde un archivo de configuración

    // Ejemplo de obtener la IP desde un archivo de configuración
    const response = await fetch('/config/backend-url.json');
    const data = await response.json();
    return data.backendUrl;
};