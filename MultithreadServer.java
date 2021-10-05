package practica1;

import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.io.*;

public class MultithreadServer {
	
	public static void main( String args[] ) {
	
		ServerSocket s = null;
		Socket client = null;
		HashMap<String, Integer> cookies = new HashMap<String, Integer>();
		HashSet<String> cookiesSet = new HashSet<String>();
		
		// Establecemos el servicio en el puerto 9999
		// No podemos elegir un puerto por debajo del 1023 si no somos
		// usuarios con los máximos privilegios (root)
		try {
			s = new ServerSocket( 9999 );
		} catch( IOException e ) {
			System.out.println( e );
		}
	 
		// Creamos el objeto desde el cual atenderemos y aceptaremos
		// las conexiones de los clientes y abrimos los canales de
		// comunicación de entrada y salida
		while (true) {
			try {
				client = s.accept();
				GestorPeticion gestor = new GestorPeticion(client, cookies, cookiesSet);
				gestor.start();
			} catch( IOException e ) {
			 System.out.println( e );
			}
		 }
	}
}
	
class GestorPeticion extends Thread {
		
	Socket client;
	HashMap<String, Integer> cookies;
	HashSet<String> cookiesSet;
	
	public GestorPeticion(Socket client, HashMap<String, Integer> cookies, HashSet<String> cookiesSet) {
		this.client = client;
		this.cookies = cookies;
		this.cookiesSet = cookiesSet;
	}

	public void run() {
		InputStream is;
		OutputStream os;

		try {
			
			is = client.getInputStream();
	        os = client.getOutputStream();

	        // Recibir datos
	        byte[] buffer = new byte[1000];
	        int leido;
	        String request = new String();
	        leido = is.read(buffer);
	        request = new String(buffer, 0, leido);
			// Procesamiento
				// Obtener recurso
			String cookie = this.getCookie(request);
				// Procesar salida
			String response = this.getResponse(cookie, cookiesSet);
			// Replicar datos
			byte[] bufferResponse = response.getBytes();
			os.write(bufferResponse);

			is.close();
			os.close();
			client.close();
		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage() + ":" + e);
		}
	}
	// El recurso estará en la segunda palabra de la primera línea
	private String getCookie(String request) {
		String [] lines = request.split(" ");
		return lines[1];
	}
	
	private String getResponse(String cookie, HashSet<String> cookiesSet) {
		int valor = (int)(Math.random()*9999);
		// Si la cookie no se ha pedido anteriormente, se cuenta como primera aparición
		// y se guarda con un valor aleatorio asociado
		if (!cookies.containsKey(cookie)) {
			cookies.put(cookie, 1);
			cookiesSet.add(cookie+"="+valor);
		}
		// Si ya se ha pedido anteriormente, aumentamos el contador de solicitudes
		else
			cookies.put(cookie, cookies.get(cookie)+1);
		// Concatenamos la respuesta
		String response = "HTTP/1.1 200 OK\r\n";
		for (String c : cookiesSet)
			response = response.concat("Set-Cookie: " + c + "\r\n");
		response = response.concat("\r\n");
		response = response.concat("<html><head><title> Cookies del cliente </title>");
		response = response.concat("</head><body>");
		for (String c : cookies.keySet()) {
			response = response.concat("<h1>La cookie " + c + "  se ha pedido " + cookies.get(c) + " veces</h1>");
		}
		response = response.concat("</body></html>");
		return response;
	}
} 

