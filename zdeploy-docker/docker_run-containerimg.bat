echo "컨테이너를 특정포터로 open"
rem -e 옵션은 컨테이너 환경을 적용하기 위해 사용한 것이고,

docker run -d --network ecommerce-network ^
  --name catalog-service ^
 -e "spring.cloud.config.uri=http://config-service:8888" ^
 -e "spring.rabbitmq.host=rabbitmq" ^
 -e "spring.zipkin.base-url=http://zipkin:9411" ^
 -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" ^
 -e "logging.file=/api-logs/catlogs-ws.log" ^
 changwskr/catalog-service:1.0

