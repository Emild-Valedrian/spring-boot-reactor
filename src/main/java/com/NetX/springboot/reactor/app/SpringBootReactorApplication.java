package com.NetX.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.emitter.Emitter;

import com.NetX.springboot.reactor.app.models.Comentarios;
import com.NetX.springboot.reactor.app.models.Usuario;
import com.NetX.springboot.reactor.app.models.UsuariosComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploUsuarioComentarioZipWith();
	}

	public void ejemploContraPresion() {
		Flux.range(1, 10)
		.log()
		//.limitRate(4)
		.subscribe(new Subscriber<Integer>() {

			private Subscription s;
			private Integer limite = 5;
			private Integer consumido = 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);

			}

			@Override
			public void onNext(Integer t) {
				log.info(t.toString());
				consumido++;
				if (consumido == limite) {
					consumido = 0;
					s.request(limite);
				}
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub

			}
		});
	}

	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}
					if (contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, el flux se detuvo en el 8!"));
					}
				}
			}, 1000, 1000);
		}).subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()),
				() -> log.info("Hasta aqui termina el conteo"));
	}

	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1)).doOnTerminate(latch::countDown).flatMap(i -> {
			if (i >= 5) {
				return Flux.error(new InterruptedException("El conteo solo llegar?? hasta el 5"));
			}
			return Flux.just(i);
		}).map(i -> "Hello" + i).retry(2).subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

		latch.await();
	}

	public void ejemploDelayElements() {
		Flux<Integer> rango2 = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango2.blockLast();

	}

	public void ejemploInterval() {
		Flux<Integer> rango2 = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rango2.zipWith(retraso, (ra, re) -> ra).doOnNext(i -> log.info(i.toString())).blockLast();
	}

	public void ejemploZipWithRangos() {
		Flux<Integer> rango1 = Flux.range(0, 4);
		Flux.just(1, 2, 3, 4).map(i -> (i * 2))
				.zipWith(rango1, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(texto -> log.info(texto));
	}

	public void ejemploUsuarioComentarioZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Luna", "Creciente"));

		Mono<Comentarios> comentarioUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("La valentia no es suficiente para vencer a alguien m??s fuerte que t??, mocoso");
			comentarios.addComentario("Lamentos de desesperaci??n, el regocijo de la muerte, LA DANZA MACABRA");
			comentarios.addComentario("Te retorceras hasta morir ni??o");
			return comentarios;
		});

		Mono<UsuariosComentarios> usuarioConComentarios = usuarioMono.zipWith(comentarioUsuarioMono).map(tuple -> {
			Usuario u = tuple.getT1();
			Comentarios c = tuple.getT2();
			return new UsuariosComentarios(u, c);
		});

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentarioZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Luna", "Creciente"));

		Mono<Comentarios> comentarioUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("La valentia no es suficiente para vencer a alguien m??s fuerte que t??, mocoso");
			comentarios.addComentario("Lamentos de desesperaci??n, el regocijo de la muerte, LA DANZA MACABRA");
			comentarios.addComentario("Te retorceras hasta morir ni??o");
			return comentarios;
		});

		Mono<UsuariosComentarios> usuarioConComentarios = usuarioMono.zipWith(comentarioUsuarioMono,
				(usuario, comentariosUsuario) -> new UsuariosComentarios(usuario, comentariosUsuario));
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentarioFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Luna", "Creciente"));

		Mono<Comentarios> comentarioUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("La valentia no es suficiente para vencer a alguien m??s fuerte que t??, mocoso");
			comentarios.addComentario("Lamentos de desesperaci??n, el regocijo de la muerte, LA DANZA MACABRA");
			comentarios.addComentario("Te retorceras hasta morir ni??o");
			return comentarios;
		});

		usuarioMono.flatMap(u -> comentarioUsuarioMono.map(c -> new UsuariosComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCollectList() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Valery", "Eyheralde"));
		usuariosList.add(new Usuario("Raquel", "Eyheralde"));
		usuariosList.add(new Usuario("E-drian", "Legrand"));
		usuariosList.add(new Usuario("E-drix", "Legrand"));
		usuariosList.add(new Usuario("K3lpie", "Apellido1"));
		usuariosList.add(new Usuario("A-phol??", "Apellido2"));
		usuariosList.add(new Usuario("V-alentin3", "Apellido3"));

		Flux.fromIterable(usuariosList).collectList().subscribe(lista -> {
			lista.forEach(item -> log.info(item.toString()));
		});
	}

	public void ejemploToString() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Valery", "Eyheralde"));
		usuariosList.add(new Usuario("Raquel", "Eyheralde"));
		usuariosList.add(new Usuario("E-drian", "Legrand"));
		usuariosList.add(new Usuario("E-drix", "Legrand"));
		usuariosList.add(new Usuario("K3lpie", "Apellido1"));
		usuariosList.add(new Usuario("A-phol??", "Apellido2"));
		usuariosList.add(new Usuario("V-alentin3", "Apellido3"));

		Flux.fromIterable(usuariosList).map(
				usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("raquel".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				}).map(nombre -> {
					return nombre.toLowerCase();
				}).subscribe(u -> log.info(u.toString()));
	}

	public void ejemploFlatMap() throws Exception {

		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Valery Eyheralde");
		usuariosList.add("Raquel Eyheralde");
		usuariosList.add("E-drian Legrand");
		usuariosList.add("E-drix Legrand");
		usuariosList.add("K3lpie Apellido1");
		usuariosList.add("A-phol?? Apellido2");
		usuariosList.add("V-alentin3 Apellido3");

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("v-alentin3")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				}).subscribe(u -> log.info(u.toString()));
	}

	public void ejemploIterable() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Valery Eyheralde");
		usuariosList.add("Valery Eyheralde");
		usuariosList.add("E-drian Legrand");
		usuariosList.add("K3lpie Apellido1");
		usuariosList.add("A-phol?? Apellido2");
		usuariosList.add("V-alentin3 Apellido3");

		Flux<String> nombres = Flux.fromIterable(usuariosList);

		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("valery")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Debe ingresar un nombre");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecuci??n del observable con ??xito!");
			}
		});
	}
}
