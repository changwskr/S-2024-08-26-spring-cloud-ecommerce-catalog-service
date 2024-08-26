package com.example.catalogservice.messagequeue;

import com.example.catalogservice.business.dc.dao.model.CatalogEntity;
import com.example.catalogservice.business.dc.dao.ICatalogRepositoryDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * user-service 주문한다. http://127.0.0.1:8000/order-service/(user-id)/orders
 *      상품id, 수량, 단가
 * ---------------------------------------------------------------------------------
 *  * order-service 주문을 수신한다.
 *      createOrder.controller -----------------------------------------------------------------
 *            KafkaProducer는 주문이 들어오면
 * 	          (POST) http://127.0.0.1:8000/order-service/(user-id)/orders
 *            @PostMapping("/{userId}/orders")
 *            public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder orderDetails)
 *            {
 *                주문한다.
 *                OrderDto createdOrder = orderService.createOrder2(orderDto);
 *                주문이 성공하면 example-catalog-topic에 메시지를 publisher 한다.
 *                □□□□□□□□□□□ example-catalog-topic publisher □□□□□□□□□
 *                KafkaProducer kafkaProducer; 상품id, 수량, 단가 *
 *                kafkaProducer.send("example-catalog-topic", orderDto);
 *                □□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□
 *            }
 * ---------------------------------------------------------------------------------
 * catalog-service
 *      example-catalog-topic에 메시지를 subsciber 한다.
 *      public class KafkaConsumer {
 * 	        // 여기서 생산자에서 생산한 카탈로그에 대한 갯수를 수정해야 되어서 repository를 선언한다.
 *          ICatalogRepositoryDAO repository;
 *
 *          □□□□□□□□□□□ example-catalog-topic subscribe □□□□□□□□□□□□□□□
 *          ★★★★★ 여기서 리스너로 example-catalog-topic 수신대기하고 있다. 상품id, 수량, 단가
 *          @KafkaListener(topics = "example-catalog-topic")
 *          □□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□
 *          public void updateQty(String kafkaMessage) throws Exception {
 *              상품id, 수량, 단가
 *              CatalogEntity entity = repository.findByProductId((String)map.get("productId"));
 *              주문된 수량을 원 catalog 테이블에 있는 수량에서 차감하고 저장한다.
 *              entity.setStock(entity.getStock() - (Integer)map.get("qty"));
 *              repository.save(entity);
 *          }
 *      }
 */


@Service
@Slf4j
public class KafkaConsumer {
	// 여기서 생산자에서 생산한 카탈로그에 대한 갯수를 수정해야 되어서 repository를 선언한다.
    ICatalogRepositoryDAO repository;

    @Autowired
    public KafkaConsumer(ICatalogRepositoryDAO repository) {
        this.repository = repository;
    }
    
    // 생산자에서 생산한 이벤트 대기한다.
    @KafkaListener(topics = "example-catalog-topic")
    public void updateQty(String kafkaMessage) throws Exception {

    	log.info("★ CatalogService.KafkaConsumer.updateQty Listener start -->example-catalog-topic>" + kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        
        try {
        	// 생산자에서 발생한 메시지는 OrderDTO이다. 이것을 JSON KEY-VALUE값으로 변환한다.
            // TypeReference 이것을 이용해서 String 메시지를 Json 타입으로 전환한다.
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        // 생산품의 ID를 확인한다.
        // 상품id를 활용해서 catalog 테이블의 정보를 가지고 온다.
        CatalogEntity entity = repository.findByProductId((String)map.get("productId"));
        if (entity != null) {
        	// 수량을 감소시킨다.
            // entity 정보의 상품수량은 주문한 수량을 빼준다.
            entity.setStock(entity.getStock() - (Integer)map.get("qty"));
            // 데이타베이스에 저장한다.
            repository.save(entity);
        }
        else {
        	log.error("CatalogEntity 상품이 존재하지 않습니다.");
        	throw new Exception("CatalogEntity 상품이 존재하지 않습니다.");
        }

        log.info("★ CatalogService.KafkaConsumer.updateQty Listener end -->example-catalog-topic>" + kafkaMessage);
    }

    /**
     * example-order-topic은 orderservice.ordercontroller.createOrder()에서 발생한다.
     * 즉 주문이 정상적으로 완료되면 ordercontroller는 kafka에 메시지를 보내어 수량을 조정하라 한다.
     * 이때 사용되는 정보는 OrderDto이다.
     * OrderDto에는 상품id, 수량, 단가, 총금액, 주문번호, userid 가 저장된다.
     *
     * @param kafkaMessage
     * @throws Exception
     */
    @KafkaListener(topics="example-order-topic")
    public void processMessage(String kafkaMessage) throws Exception {

    	log.info("★ CataLog.KafkaConsumer.processMessage()는 example-order-topic 이벤트를 수신했다." + kafkaMessage);
        log.info("수신메시지 [" + kafkaMessage + "]");

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        // map에는 order서비스에서 발생시킨 데이타가 존재한다.
        // OrderDto의 내용이 전달되어 온다.
        // OrderDto에는 상품id, 수량, 단가, 총금액, 주문번호, userid 가 저장된다.
        // 상품id와 수량을 조정한다.
        // example-order-topic에 저장된 메시지를 수신하여 Catalog 테이블을 갱신하는 작업을 한다.

        log.info("상품id-productId를 활용해 현재 catalog 테이블의 정보를 읽는다.");
        CatalogEntity entity = repository.findByProductId((String)map.get("productId"));
        log.info("수신메시지 [" + entity.toString() + "]");
        if (entity != null) {
            log.info("테이블 상품 수량 - 주문 상품 수량 계산하고 테이블에 저장한다.");
            entity.setStock(entity.getStock() - (Integer)map.get("qty"));
            repository.save(entity);
        }
        else {
        	log.error("CatalogEntity 상품이 존재하지 않습니다.");
        	throw new Exception("CatalogEntity 상품이 존재하지 않습니다.");
        }
        log.info("★ CataLog.KafkaConsumer.processMessage()는 example-order-topic 이벤트를 완료했다." + kafkaMessage);
    }
}
