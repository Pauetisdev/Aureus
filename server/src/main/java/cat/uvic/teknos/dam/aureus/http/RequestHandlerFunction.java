// Paquete: cat.uvic.teknos.dam.aureus.http
package cat.uvic.teknos.dam.aureus.http;

import com.athaydes.rawhttp.core.RawHttpRequest;
import com.athaydes.rawhttp.core.RawHttpResponse;

// Interfaz funcional para manejar una petición
// Lanza excepción si hay un error de negocio o de cliente (e.g., 404, 400)
@FunctionalInterface
public interface RequestHandlerFunction {
    RawHttpResponse<?> handle(RawHttpRequest request, String pathVariable) throws Exception;
}