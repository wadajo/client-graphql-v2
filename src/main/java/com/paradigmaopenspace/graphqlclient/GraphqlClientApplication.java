package com.paradigmaopenspace.graphqlclient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.client.HttpGraphQlClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@SpringBootApplication
public class GraphqlClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlClientApplication.class, args);
	}

	// BBDD
	List<Artista> otraBBDD=List.of(
			new Artista("Levstein", 1991),
			new Artista("Molina", 1988),
			new Artista("Casile", 1992),
			new Artista("Federovisky", 1994)
	);

	// Repository / Service
	int buscarAnoDelArtista(String apellido){
		return otraBBDD.parallelStream()
				.filter(artista -> artista.apellido().equalsIgnoreCase(apellido))
				.mapToInt(Artista::nacimiento)
				.findFirst()
				.orElseGet(()->0);
	}

	@Bean
	HttpGraphQlClient graphQlClient(){
		return HttpGraphQlClient.builder()
				.url("http://localhost:8181/graphql")
				.build();
	}

	@Bean
	CommandLineRunner inicio(HttpGraphQlClient graphQlClient) {
		return (args) -> {
			System.out.println("Estos son los artistas de los cuales hemos encontrado su fecha de nacimiento:");
			graphQlClient
					.mutate()
					.header("Authorization","Basic anVsaW86YXJ0ZUJBMjAyMg==")
					.build()
					.document("""
								query{
									 artistas{
										 apellido
									 }
								 }
							""")
					.retrieve("artistas")
					.toEntityList(Artista.class)
					.map(artistas -> artistas.stream()
							.map(artista -> new Artista(artista.apellido(), buscarAnoDelArtista(artista.apellido())))
					)
					.block(Duration.of(4, ChronoUnit.SECONDS))
					.filter(artista -> artista.nacimiento() > 0)
					.forEach(System.out::println);
		};
	}
}

//Mi definición de artista, distinta a la de la API externa
record Artista(String apellido, Integer nacimiento) {
}

record Documento(String id, String nombre){}