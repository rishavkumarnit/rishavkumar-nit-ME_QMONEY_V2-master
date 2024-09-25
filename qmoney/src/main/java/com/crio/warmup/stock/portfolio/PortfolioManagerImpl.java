
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {



 // adding comment

  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;
  private ObjectMapper objectMapper = getObjectMapper();

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  protected PortfolioManagerImpl(RestTemplate restTemplate, 
      StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
    this.restTemplate = restTemplate;
  }



  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
    String url = buildUri(symbol, from, to);
    TiingoCandle[] candlelist = restTemplate.getForObject(url , TiingoCandle[].class);
    return new ArrayList<>(Arrays.asList(candlelist));
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String baseUrl = "https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s";
    String token = "9d7047e6d0e5e2ec1644006d12d9955e69b81b58";
    return String.format(baseUrl, symbol, startDate, endDate, token);
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) throws  StockQuoteServiceException{
    List<AnnualizedReturn> aReturnlist = new ArrayList<>();


    for(PortfolioTrade each : portfolioTrades){
      List<Candle> candleList = new ArrayList<>();
      try {
        
        candleList = stockQuotesService.getStockQuote(each.getSymbol(), each.getPurchaseDate(), endDate);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }

      Double buyPrice = candleList.get(0).getOpen();
      Double sellPrice= candleList.get(candleList.size() - 1).getClose();
      Double tReturn = (sellPrice - buyPrice) / buyPrice;
      long daysHeld = ChronoUnit.DAYS.between(each.getPurchaseDate(), endDate);
      double totalNumYears = daysHeld / 365.24;
      double annualizedReturn = Math.pow(1 + tReturn, 1 / totalNumYears) - 1; 
      aReturnlist.add( new AnnualizedReturn(each.getSymbol(), annualizedReturn, tReturn));
    }
    Comparator<AnnualizedReturn> comparator = getComparator();
    Collections.sort(aReturnlist, comparator);
    return aReturnlist;
  }




}
