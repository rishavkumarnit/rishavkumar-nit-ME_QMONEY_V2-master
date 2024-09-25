
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.ThreadContext;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException,  NullPointerException, ArrayIndexOutOfBoundsException {
    File jsonfile = resolveFileFromResources(args[0]);
    List<String> stockSymbols = new ArrayList<>();
    ObjectMapper objectMapper = getObjectMapper();
    List<PortfolioTrade> temp = new ArrayList<>();
    temp = readTradesFromJson(args[0]);    
    PortfolioTrade[] trades = objectMapper.readValue(jsonfile, PortfolioTrade[].class);
    for(PortfolioTrade each : trades){
      stockSymbols.add(each.getSymbol());
    }
    return stockSymbols;
  }


  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    List<PortfolioTrade> list = new ArrayList<>();
    ObjectMapper objectMapper = getObjectMapper();
    File jsonfile = resolveFileFromResources(filename);
    PortfolioTrade[] trades = objectMapper.readValue(jsonfile, PortfolioTrade[].class);
    for(PortfolioTrade each : trades){
      list.add(each);
    }
    return list;
  }
  

  public static String getToken() {
    return "9d7047e6d0e5e2ec1644006d12d9955e69b81b58";
  }


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException, NullPointerException, ArrayIndexOutOfBoundsException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() throws IOException, URISyntaxException,  NullPointerException, ArrayIndexOutOfBoundsException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException, NullPointerException, ArrayIndexOutOfBoundsException {
    List<String> sortedStocks = new ArrayList<>();
    List<priceandname> list = new ArrayList<>();
    File reader = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    List<PortfolioTrade> temp = new ArrayList<>();
    temp = readTradesFromJson(args[0]);
    PortfolioTrade[] trades = om.readValue(reader, PortfolioTrade[].class);
    for (PortfolioTrade each : trades) {
      String apiUrl = prepareUrl(each, LocalDate.parse(args[1]), "9d7047e6d0e5e2ec1644006d12d9955e69b81b58");
      double closingPrice = getClosingPrice(apiUrl);
      priceandname obj = new priceandname(closingPrice, each.getSymbol());
      list.add(obj);
    }
    Collections.sort(list);
    for(priceandname each : list){
      sortedStocks.add(each.sharename);
    }
    return sortedStocks;
  }


  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) throws IOException, URISyntaxException,NullPointerException, ArrayIndexOutOfBoundsException{
    String baseUrl = "https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s";
    String symbol = trade.getSymbol(); 
    String startDate = trade.getPurchaseDate().toString();   
    return String.format(baseUrl, symbol, startDate, endDate, token);
  }


  public static double getClosingPrice (String apiUrl) throws IOException, URISyntaxException, NullPointerException, ArrayIndexOutOfBoundsException{
    TiingoCandle[] response = null;
    RestTemplate restTemplate = new RestTemplate();
    try{
      response = restTemplate.getForObject(apiUrl, TiingoCandle[].class);
    }
    catch(RuntimeException e){
    }
    double closingPrice = response[response.length - 1].getClose();  
    return closingPrice;
  }


  static Double getOpeningPriceOnStartDate(List<Candle> candles)  {
    try{
      return candles.get(0).getOpen();
    }
    catch (Exception e) {
      //e.printStackTrace();
      return null;
    }
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles)  {
    try{
      return candles.get(candles.size() - 1).getClose();
    }
    catch (Exception e) {
      //e.printStackTrace();
      return null;
    }
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token){
    try{
      String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate().toString() + "&endDate=" + endDate + "&token=" + token;
      RestTemplate restTemplate = new RestTemplate();
      TiingoCandle[] candles = restTemplate.getForObject(url, TiingoCandle[].class);
      return new ArrayList<>(Arrays.asList(candles));
    }
    catch (Exception e) {
      //e.printStackTrace();
      return Collections.emptyList();
    }
  }


  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) throws IOException, URISyntaxException {  

    String filename = args[0];
    List<PortfolioTrade> portfolioTrades = readTradesFromJson(filename);
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    for (PortfolioTrade trade : portfolioTrades) {
        LocalDate endDate = LocalDate.parse(args[1]);
        List<Candle> candles = fetchCandles(trade, endDate, "9d7047e6d0e5e2ec1644006d12d9955e69b81b58");
        Double buyPrice = getOpeningPriceOnStartDate(candles);
        Double sellPrice = getClosingPriceOnEndDate(candles);
        
        AnnualizedReturn annualizedReturnObj = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
        annualizedReturns.add(annualizedReturnObj);
    }
    Collections.sort(annualizedReturns, Collections.reverseOrder());
    return annualizedReturns;
  }



  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, Double buyPrice, Double sellPrice){
    try{
      Double tReturn = (sellPrice - buyPrice) / buyPrice;
      long daysHeld = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
      double totalNumYears = daysHeld / 365.24;
      double annualizedReturn = Math.pow(1 + tReturn, 1 / totalNumYears) - 1; 
      
      return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, tReturn);
    }
    catch (Exception e) {
      //e.printStackTrace();
      return null;
    }
  }



  public static double getbuyPrice (String apiUrl) throws IOException, URISyntaxException, NullPointerException, ArrayIndexOutOfBoundsException{
    TiingoCandle[] response = null;
    RestTemplate restTemplate = new RestTemplate();
    try{
      response = restTemplate.getForObject(apiUrl, TiingoCandle[].class);
    }
    catch(RuntimeException e){
    }
    double buyPrice = response[0].getOpen();  
    return buyPrice;
  }



  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "trades.json";
    String toStringOfObjectMapper = "ObjectMapper";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "";


   return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
       toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
       lineNumberFromTestFileInStackTrace});
 }






  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
      List<PortfolioTrade> portfolioTrades = readTradesFromJson(file);
       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
      return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    //printJsonObject(mainCalculateSingleReturn(args));
    //printJsonObject(mainCalculateReturnsAfterRefactor(args));

  }


  // public static void main(String[] args) throws Exception, IOException, URISyntaxException,  NullPointerException, ArrayIndexOutOfBoundsException {
  //   Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
  //   ThreadContext.put("runId", UUID.randomUUID().toString());

  //   printJsonObject(mainReadFile(args));

  //   printJsonObject(mainReadQuotes(args));

  // }

}





class priceandname implements Comparable<priceandname> {

  double price;
  String sharename;

  priceandname(double price, String sharename) throws IOException, URISyntaxException{
    this.price = price;
    this.sharename = sharename;
  }

  @Override
  public int compareTo(priceandname obj1) {
    if(this.price > obj1.price){
      return 1;
    }
    else if(this.price < obj1.price){
      return -1;
    }
    return 0;
  }

}

