package com.vertx.monitor;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;

public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        // 1. Vert.x를 시작하기 전에 Micrometer Prometheus 옵션을 먼저 활성화합니다.
        VertxOptions options = new VertxOptions().setMetricsOptions(
            new MicrometerMetricsOptions()
                .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                .setEnabled(true)
        );

        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(new MainVerticle());
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // 2. Prometheus가 메트릭을 긁어갈 수 있도록 엔드포인트(/metrics) 바인딩
        router.route("/metrics").handler(ctx -> {
            // BackendRegistries에서 Vert.x가 내부적으로 사용하는 Prometheus Registry를 가져옵니다.
            PrometheusMeterRegistry registry = (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();
            if (registry != null) {
                ctx.response()
                   .putHeader("Content-Type", "text/plain; version=0.0.4")
                   .end(registry.scrape());
            } else {
                ctx.response().setStatusCode(500).end("Registry not initialized");
            }
        });

        // 2-1. 정적 HTML 테스트 페이지 제공
        router.route("/loadtest").handler(ctx -> ctx.reroute("/loadtest.html"));
        router.route("/*").handler(StaticHandler.create("webroot").setIndexPage("index.html"));

        // 3. 실시간 채팅 모사를 위한 간단한 WebSocket 핸들러 설정
        server.webSocketHandler(webSocket -> {
            if (!"/chat".equals(webSocket.path())) {
                webSocket.reject();
                return;
            }

            // 채팅방 개념으로 EventBus 브릿지 역할 모사
            webSocket.handler(buffer -> {
                vertx.eventBus().publish("chat.room.global", buffer.toString());
                webSocket.writeTextMessage("ACK: " + buffer.toString());
            });

            webSocket.closeHandler(v -> {
                // 커넥션 종료 처리
            });
        });

        // 4. HTTP 라우터와 웹소켓 설정을 포함하여 서버 구동 (포트 8080)
        server.requestHandler(router).listen(8080, res -> {
            if (res.succeeded()) {
                System.out.println("Vert.x 데몬 서비스가 8080 포트에서 시작되었습니다.");
            }
        });

        // 5. [테스트용 부하 시뮬레이션] 주기적으로 EventBus에 더미 데이터를 던져 메트릭 변화를 유도합니다.
        vertx.setPeriodic(1000, id -> {
            vertx.eventBus().request("chat.service.internal", "ping", reply -> {
               // 내부 로직 처리 메트릭 누적용
            });
        });

        vertx.eventBus().consumer("chat.service.internal", message -> {
            message.reply("pong");
        });
    }
}