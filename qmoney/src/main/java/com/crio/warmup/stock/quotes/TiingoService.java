
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private RestTemplate restTemplate;
  private ObjectMapper objectMapper = getObjectMapper();

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException, NullPointerException, StockQuoteServiceException{
    
    List<Candle> candleList = new ArrayList<>(); 

    try{
      String uri = buildUri(symbol, from, to);
      String result = restTemplate.getForObject(uri,String.class);
      List<TiingoCandle> candles = objectMapper.readValue(result,
          new TypeReference<List<TiingoCandle>>() {});   
      
      for (int i = 0; i < candles.size(); i++) {
        candleList.add(candles.get(i));
      }
    } catch (RuntimeException e){
      throw new StockQuoteServiceException(e.toString());
    }
    
    return candleList;

  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String baseUrl = "https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s";
    String token = "9d7047e6d0e5e2ec1644006d12d9955e69b81b58";
    return String.format(baseUrl, symbol, startDate, endDate, token);
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

}
