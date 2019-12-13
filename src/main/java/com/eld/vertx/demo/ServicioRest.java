package com.eld.vertx.demo;

import java.util.LinkedHashMap;
import java.util.Map;

import com.eld.vertx.demo.model.Whisky;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ServicioRest extends AbstractVerticle {
	
	private Map<Integer, Whisky> products = new LinkedHashMap<>();
	
	private void crearDataInicial() {
		Whisky johnny = new Whisky("Johnny Walker", "Scotland, Islay");
		Whisky chivas = new Whisky("Chivas Regal", "Scotland, Island");
		products.put(johnny.getId(), johnny);
		products.put(chivas.getId(), chivas);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		crearDataInicial();
		
		Router router = Router.router(vertx);
		
		router.get("/api/whiskies").handler(this::getAll);
		//crear
		router.route("/api/whiskies*").handler(BodyHandler.create());//* indica que se aceptan subrutas a partir desde esa direccion
		router.post("/api/whiskies").handler(this::addOne);
		//borrar
		router.delete("/api/whiskies/:id").handler(this::deleteOne);
		
		vertx.createHttpServer().requestHandler(router::accept)
			.listen(
					config().getInteger("http.port", 8080), 
					result -> {
						if (result.succeeded()) {
							startFuture.complete();
						} else {
							startFuture.fail(result.cause());
						}
		});
		
//		Record record = HttpEndpoint.createRecord("aplicaRest", "localhost", 8081, "/apiRest");
//		
//		ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
//		discovery.publish(record, handler -> {
//			
//		});
//		
//		discovery.getRecord(new JsonObject().put("name", "algun-nombre"), response ->{
//			if (response.succeeded() && response.result() != null) {
//				ServiceReference reference = discovery.getReference(response.result());
//				
//				HttpClient client = reference.getAs(HttpClient.class);
//			}
//		});
	}
	
	private void getAll(RoutingContext routingContext) {
		routingContext.response()
		.putHeader("content-type", "application/json; charset=utf-8")
		.end(Json.encodePrettily(products.values()));
	}
	
	private void addOne (RoutingContext routingContext) {
		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
		products.put(whisky.getId(), whisky);
		routingContext.response()
		.setStatusCode(201)
		.putHeader("content-type", "application/json; charset=utf-8")
		.end(Json.encodePrettily(whisky));
	}
	
	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsString = Integer.valueOf(id);
			products.remove(idAsString);
		}
		routingContext.response().setStatusCode(204).end();
	}

}
