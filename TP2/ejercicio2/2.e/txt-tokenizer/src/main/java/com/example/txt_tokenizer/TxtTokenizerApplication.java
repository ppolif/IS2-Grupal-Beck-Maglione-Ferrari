package com.example.txt_tokenizer;

import com.example.txt_tokenizer.model.Cliente;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

@SpringBootApplication
public class TxtTokenizerApplication {

	public static void main(String[] args) {

		SpringApplication.run(TxtTokenizerApplication.class, args);

		List<Cliente> clientes = new ArrayList<Cliente>();
		String path = "../clientes.txt";

		File file = new File(path);

		try {
			Scanner scanner = new Scanner(file);

			while (scanner.hasNextLine()) {

				//leemos el archivo por linea
				String linea = scanner.nextLine();

				//esto separa el contenido de cada linea segun las tabulaciones
				StringTokenizer atributo = new StringTokenizer(linea, "\t");

				Cliente cliente = new Cliente();

				//recorrer cada elemento en que se separo la linea
				while (atributo.hasMoreElements()) {
					cliente.setId(Integer.parseInt(atributo.nextElement().toString()));
					cliente.setNombre(atributo.nextElement().toString());
					cliente.setApellido(atributo.nextElement().toString());
					cliente.setDireccion(atributo.nextElement().toString());

				}
				clientes.add(cliente);
			}
			scanner.close();

			clientes.forEach(c -> System.out.println(c));
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
