package com.example.catalogservice.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.catalogservice.transfer.dto.CatalogDto;
import com.example.catalogservice.business.dc.dao.model.CatalogEntity;
import com.example.catalogservice.business.as.CatalogServiceAS;
import com.example.catalogservice.transfer.vo.Greeting;
import com.example.catalogservice.web.transfer.vo.RequestCatalogVO;
import com.example.catalogservice.web.transfer.vo.ResponseCatalogVO;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/catalog-service")
@Slf4j
public class CatalogController {
    Environment env;
    CatalogServiceAS catalogService;

    @Autowired
    private Greeting greeting;
    

    @Autowired
    public CatalogController(Environment env, CatalogServiceAS catalogService) {
        this.env = env;
        this.catalogService = catalogService;
    }

    @GetMapping("/health_check")
    @Timed(value="catalog.status", longTask = true)
    public String status() {
        return String.format("It's Working in Catalog Service on PORT %s",
                env.getProperty("local.server.port"));
    }
    @GetMapping("/welcome")
    @Timed(value="catalog.welcome", longTask = true)
    public String welcome(HttpServletRequest request, HttpServletResponse response) {   	
        return greeting.getMessage();
    }
    

    @GetMapping("/catalogs")
    public ResponseEntity<List<ResponseCatalogVO>> getCatalogs() {
        Iterable<CatalogEntity> catalogList = catalogService.getAllCatalogs();

        List<ResponseCatalogVO> result = new ArrayList<>();
        catalogList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseCatalogVO.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/catalogs-execute")
    public ResponseEntity<ResponseCatalogVO> execute(@RequestBody RequestCatalogVO catalog) {

        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // mapper를 통해서 매핑
        CatalogDto catalogDto = mapper.map(catalog, CatalogDto.class);

        log.debug("catalogDto-" + catalogDto);

        CatalogEntity catlogEntity = null;
        // 서비스 요청
        try {
            catlogEntity = catalogService.updateCatalog(catalogDto);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.error("상품이 존재하지 않습니다.");
        }
        // mapper를 통해서 매핑
        ResponseCatalogVO responseCatalog = mapper.map(catlogEntity, ResponseCatalogVO.class);
        // 클라이언트로 리턴 201
        // body에 ResponseUser를 셋팅한다.
        return ResponseEntity.status(HttpStatus.CREATED).body(responseCatalog);
    }

    // 여기서 상품의 수량을 조정한다. orderservice에서 catalogservice 를 호출하여 수량을 조정한다.
    // 이 것은 orderservice가 성공하면 orderservice에서 직접호출하는 방식으로 활용가능하다.
     @PostMapping("/catalogs")
    public ResponseEntity<ResponseCatalogVO> updateCatalog(@RequestBody RequestCatalogVO catalog) {
    	
        ModelMapper mapper = new ModelMapper();
        
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // mapper를 통해서 매핑
        CatalogDto catalogDto = mapper.map(catalog, CatalogDto.class);
        
        log.debug("catalogDto-" + catalogDto);
        
        CatalogEntity catlogEntity = null;
        // 서비스 요청
        try {
        	catlogEntity = catalogService.updateCatalog(catalogDto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("상품이 존재하지 않습니다.");
		}
        // mapper를 통해서 매핑
        ResponseCatalogVO responseCatalog = mapper.map(catlogEntity, ResponseCatalogVO.class);
        // 클라이언트로 리턴 201
        // body에 ResponseUser를 셋팅한다.
        return ResponseEntity.status(HttpStatus.CREATED).body(responseCatalog);
    }

    
    
}
