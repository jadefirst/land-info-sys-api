package com.example.propertyanalysis;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.propertyanalysis.dto.CoordinateRequest;
import com.example.propertyanalysis.dto.InvestmentScoreResponse;
import com.example.propertyanalysis.mapper.LawdCodeMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RestController
@CrossOrigin(origins = "*")

public class MainController {

    private final LawdCodeMapper lawdCodeMapper;

	
	@Value("${kakao.api.key}")
	private String kakaoApiKey;
	
	@Value("${molit.api.key}")
	private String molitApiKey;


    MainController(LawdCodeMapper lawdCodeMapper) {
        this.lawdCodeMapper = lawdCodeMapper;
    }
	
    
    @CrossOrigin(origins = "http://localhost:5173")
	
//	좌표를 주소로 변환
	@PostMapping("/api/investment/score")
	public InvestmentScoreResponse calScore(@RequestBody CoordinateRequest request) {
		
		int score = 0;
		String sido = "";
		String sigungu = "";
		String address = "";
		int transportScore = 0;
		int facilityCount = 0;
		
		int distanceValue = 0;
		
		
//		String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=127.001409&y=37.590979";
		String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x="+ request.getLng()+"&y="+ request.getLat();
	    
		System.out.println("getLat: " + request.getLat() + "\"getLat: \" +: " + request.getLng()); 
		// 지하철역까지 거리 
		String transitUrl = "https://dapi.kakao.com/v2/local/search/category.json?category_group_code=SW8&x=" + request.getLng() + "&y=" + request.getLat() + "&radius=1000";
		
		// 주변 편의시설 개수
		String facilityUrl = "https://dapi.kakao.com/v2/local/search/keyword.json?query=마트&x=" + request.getLng() + "&y=" + request.getLat() + "&radius=500";
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization","KakaoAK " + kakaoApiKey);
		headers.set("Content-Type", "application/json");
		headers.set("User-Agent", "Mozilla/5.0");
		 
		 
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<?> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		ResponseEntity<String> responseTrans = restTemplate.exchange(transitUrl, HttpMethod.GET, entity, String.class);
		ResponseEntity<String> responseFacili = restTemplate.exchange(facilityUrl, HttpMethod.GET, entity, String.class);
		
		System.out.println("response :::::::::::::"+response);
		System.out.println("responseTrans :::::::::::::"+responseTrans);
		System.out.println("responseFacili :::::::::::::"+responseFacili);
		
		String responseTransData = responseTrans.getBody();
		String responseFaciliData = responseFacili.getBody();
		
		
	    LocalDate nowDate = LocalDate.now();
	    List<String> recentMons = new ArrayList<>();
		
		String kakaoJson = response.getBody();
		
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode;
		try {
			jsonNode = objectMapper.readTree(kakaoJson);
			sido = jsonNode.get("documents").get(0).get("address").get("region_1depth_name").asText();
			sigungu = jsonNode.get("documents").get(0).get("address").get("region_2depth_name").asText();
			address = jsonNode.get("documents").get(0).get("address").get("address_name").asText();
					
			System.out.println("kakaoJson :::::::::::::"+kakaoJson);
			
//			최근12개월 
		    for(int i = 1; i <= 12; i++) {
		    	String yearMon = nowDate.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyyMM"));
		    	recentMons.add(yearMon);
		    }
			
//		    교통점수
    		int totalDistance = 0;
    		int vaildCnt = 0;
    		int minDistance = Integer.MAX_VALUE;
		    jsonNode = objectMapper.readTree(responseTransData);
		    JsonNode documents = jsonNode.get("documents");
		    
		    if(documents == null || documents.size() == 0 ) {
		    	transportScore = 0;
		    }else {
		    	
			    
			    for(int i= 0; i < documents.size(); i++) {
			    	try {
	

					    String distance = documents.get(i).get("distance").asText();
					    distanceValue = Integer.parseInt(distance);
						if(distanceValue < minDistance) {
							minDistance = distanceValue;
						}
					
//					    totalDistance += distanceValue;
//					    vaildCnt++;
//					    System.out.println("totalDistance :::::::::::::"+totalDistance + "..." + vaildCnt);
					    
					    

					} catch (Exception e) {
				        System.out.println("데이터 파싱 에러: 교통시설 " + e.getMessage());
				        continue;
					}
			    }
			    if(minDistance != Integer.MAX_VALUE) {
//			    	int aveDistance = totalDistance / vaildCnt;			//평균거리

			    	
			    	if (minDistance <= 300) {
			    	    transportScore = 95;
			    	} else if (minDistance <= 500) {
			    	    transportScore = 85;
			    	} else if (minDistance <= 800) {
			    	    transportScore = 70;
			    	} else {
			    	    transportScore = 50;
			    	}
			    	
			    	
					System.out.println("transportScore :::::::::::::::::"+transportScore);
					
			    }else {
			    	transportScore = 0;
			    }
			
		    }
			
//		    편의시설
		    jsonNode = objectMapper.readTree(responseFaciliData);
		    Set<String> facilityIds = new HashSet<>();
		    if(responseTransData.contains("documents")) {
		    	JsonNode documentsFa = jsonNode.get("documents");
		    	for(int i=1; i< responseFaciliData.length(); i++) {
		    		try {
		    			
		    			JsonNode item = documentsFa.get(i);
		    			
		    			if(item == null || item.get("id") == null|| item.get("distance") == null) {
		    				continue;
		    			}
		    			
		    			String faciliId = item.get(i).get("id").asText();
		    			String distance = item.get(i).get("distance").asText();
		    			
		    			if(Integer.parseInt(distance) <= 500) {
		    				
		    				facilityIds.add(faciliId);
		    			}
		    			
		    			
		    			facilityCount = facilityIds.size();
					} catch (Exception e) {
				        System.out.println("데이터 파싱 에러: " + e.getMessage());
				        continue;
					}
		    	}
		    }
		    
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		

		
//		System.out.println("kakaoJson :::::::::::::"+kakaoJson);
		
//		법정동코드 매핑
		LawdCodeMapper mapper = new LawdCodeMapper();
		String lawdCode = mapper.getCode(sido,sigungu);
		
//		System.out.println("lawdCode :::::::::::::"+lawdCode);
		
		//12개월 누적데이터
		List<Integer> allAptTradePrices = new ArrayList<>(); 		
		List<Integer> allAptRentDeposits = new ArrayList<>(); 
		List<Integer> allOfficeteTradePrices = new ArrayList<>(); 
		List<Integer> allOfficeteRentDeposits = new ArrayList<>(); 
		double averAptTrade = 0.0;
		double averAptRent = 0.0;
		double averTradeOff = 0.0;
		double averRentOff = 0.0;
		
		
	    for(String dealYmd : recentMons) {
	    	
	    	
	    	CompletableFuture<String> futureAptTrade = CompletableFuture.supplyAsync(() -> aptTrade(lawdCode, dealYmd));
	    	CompletableFuture<String> futureAptRent = CompletableFuture.supplyAsync(() -> aptRent(lawdCode, dealYmd));
	    	CompletableFuture<String> futureOfficetelTrade = CompletableFuture.supplyAsync(() -> officetelTrade(lawdCode, dealYmd));
	    	CompletableFuture<String> futureOfficetelRent = CompletableFuture.supplyAsync(() -> officetelRent(lawdCode, dealYmd));
	    	
	    	CompletableFuture.allOf(futureAptTrade, futureAptRent, futureOfficetelTrade, futureOfficetelRent).join();				
	    	
	    	
	    	try {
	    		String getAptTrade = futureAptTrade.get();
	    		String getAptRent = futureAptRent.get();
	    		String getOfficetelTrade = futureOfficetelTrade.get();
	    		String getOfficetelRent = futureOfficetelRent.get();
	    		
	    		
//	    		System.out.println("getAptTrade :::::::::::::"+getAptTrade);
//	    		System.out.println("getAptRent :::::::::::::"+getAptRent);
//	    		System.out.println("getOfficetelTrade :::::::::::::"+getOfficetelTrade);
//	    		System.out.println("getOfficetelRent :::::::::::::"+getOfficetelRent);
	    		
//	    		아파트 매매 
	    		if(getAptTrade.contains("<item>")) {
	    			String[] dealAmounts = getAptTrade.split("<dealAmount>");
	    			for(int i =1; i < dealAmounts.length; i++) {
	    				try {
	    					String amount = dealAmounts[i].split("</dealAmount>")[0].replace(",", "");
	    					if(!amount.trim().isEmpty()) {
	    						allAptTradePrices.add(Integer.parseInt(amount));
//	    						System.out.println("allAptTradePrices ::::::::: " + allAptTradePrices);
	    					}
							
						} catch (NumberFormatException e) {
							System.out.println("숫자 변환 에러: " + e);
						}
	    			}
	    			

	    		}
	    		
//	    		아파트 전월세금
	    		if(getAptRent.contains("<item>")) {
	    			String[] deposits = getAptRent.split("<deposit>");
	    			for(int i=1; i < deposits.length; i++) {
	    				try {
	    					String deposit = deposits[i].split("</deposit>")[0].replace(",", "");
	    					if(!deposit.trim().isEmpty()) {
	    						allAptRentDeposits.add(Integer.parseInt(deposit));
//	    						System.out.println("allAptRentDeposits ::::::::: " + allAptRentDeposits);
	    					}
						} catch (Exception e) {
						}
	    				

	    			}
	    		}

//	    		오피스텔 매매 
	    		if(getOfficetelTrade.contains("<item")) {
	    			String[] dealAmountOffs = getOfficetelTrade.split("<dealAmount>");
	    			for(int i=1; i< dealAmountOffs.length; i++) {
	    				try {
	    					String dealAmountOff = dealAmountOffs[i].split("</dealAmount>")[0].replace(",", "");
	    					if(!dealAmountOff.trim().isEmpty()) {
	    						allOfficeteTradePrices.add(Integer.parseInt(dealAmountOff));
	    					}
						} catch (Exception e) {
						}
	    				
	    			}

	    		}
	    		
	    		
//	    		오피스텔 전월세
	    		if(getOfficetelRent.contains("<item>")) {
	    			String[] depositOffs = getOfficetelRent.split("<deposit>");
    				for(int i=1; i < depositOffs.length; i++) {
    					try {
    						String depositOff = depositOffs[i].split("</deposit>")[0].replace(",", "");
    						if(!depositOff.trim().isEmpty()) {
    							allOfficeteRentDeposits.add(Integer.parseInt(depositOff));
    						}
						} catch (Exception e) {
						}
    					

    				}
	    			
	    		}
	    		
	    				
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    }
		
	    //여기
	    
		if(!allAptTradePrices.isEmpty()) {
			averAptTrade = allAptTradePrices.stream()
					.mapToInt(Integer::intValue)
					.average()
					.orElse(0.0);
//			System.out.println("averAptTrade :::::::::::: " + averAptTrade);
		}
		
		if(!allAptRentDeposits.isEmpty()) {
			averAptRent = allAptRentDeposits.stream()
					.mapToInt(Integer::intValue)
					.average()
					.orElse(0.0);
//			System.out.println("averAptRent :::::::::::: " + averAptRent);
		}
	    
	    
		if(!allOfficeteTradePrices.isEmpty()) {
			averTradeOff = allOfficeteTradePrices.stream()
					.mapToInt(Integer::intValue)
					.average()
					.orElse(0.0);
//			System.out.println("averTradeOff :::::::::::: " + averTradeOff);
			
		}
	    
	    
		if(!allOfficeteRentDeposits.isEmpty()) {
			averRentOff = allOfficeteRentDeposits.stream()
					.mapToInt(Integer::intValue)
					.average()
					.orElse(0.0);
//			System.out.println("averRentOff :::::::::::: " + averRentOff);
		}
		
		
//		전세가율
		double aptJeonseRate = (averAptRent / averAptTrade) * 100; 			
		double officetelJeonseRate = (averRentOff / averTradeOff) * 100;
		
//		거래량-총건수
		int totalTransactions = allAptTradePrices.size() + allOfficeteTradePrices.size() + allAptRentDeposits.size() + allOfficeteRentDeposits.size();
		


		
//		수익성 점수
		int profitabilityScore = 0;
		if(aptJeonseRate >= 50) {
			profitabilityScore = 95;
		}else if(aptJeonseRate >= 45) {
			profitabilityScore = 90;
		}else if(aptJeonseRate >= 40) {
			profitabilityScore = 80;
		}else if(aptJeonseRate >= 35) {
			profitabilityScore = 70;
		}else if(aptJeonseRate >= 30) {
			profitabilityScore = 60;
		}else if(aptJeonseRate >= 25) {
			profitabilityScore = 50;
		}else {
			profitabilityScore = 40;
		}
		
//		거래 활성도 점수
		int activityScore = 0;
		if(officetelJeonseRate >= 800) {
			activityScore = 95;
		}else if(officetelJeonseRate >= 600) {
			activityScore = 85;
		}else if(officetelJeonseRate >= 400) {
			activityScore = 75;
		}else if(officetelJeonseRate >= 600) {
			activityScore = 65;
		}else if(officetelJeonseRate >= 600) {
			activityScore = 55;
		}else {
			activityScore = 45;
		}
		
//		편의성 교통
		int facilityScore = 0;			//수정
		if(facilityCount >= 15) {
			facilityScore = 98;
		}else if(facilityCount >= 14) {
			facilityScore = 95;
		}else if(facilityCount >= 12) {
			facilityScore = 93;
		}else if(facilityCount >= 10) {
			facilityScore = 90;
		}else if(facilityCount >= 9) {
			facilityScore = 88;
		}else if(facilityCount >= 7) {
			facilityScore = 85;
		}else if(facilityCount >= 5) {
			facilityScore = 70;
		}else {
			facilityScore = 65;
			
		}
		

		
//		종합 점수 계산
//		수익성 40% 거래활성도30% 편의성20% 기타10%
		int convenienceScore = (transportScore + facilityScore) / 2;
		int totalScore = (int)((profitabilityScore * 0.4) + (activityScore * 0.3) + (convenienceScore * 0.3));
		
		InvestmentScoreResponse result =new InvestmentScoreResponse();
				result.setTotalScore(totalScore);			//총합 점수
				result.setAddress(address);					//주소
				result.setProfitabilityScore(profitabilityScore);  
				result.setActivityScore(activityScore);            
				result.setConvenienceScore(convenienceScore);   
				result.setTransportScore(transportScore);   
				System.out.println("토탈 종합 :::::::::::: " + result.getTotalScore() + "주소 :::::::::::: " + result.getAddress() +
						"수익성점수 :::::::::::: " + result.getProfitabilityScore() +"거래 활성도 :::::::::::: " + result.getActivityScore() +
						"편의성점수  :::::::::::: " + result.getConvenienceScore() );
		return result;
		
		
	}

//	아파트 매매 실거래가
//	@GetMapping("/api/realestate/apt-trade")
	public String aptTrade(String lawdCode, String dealYmd) {
	    
	    String url = "http://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade" +
	                "?LAWD_CD=" + lawdCode + "&DEAL_YMD=" + dealYmd + "&serviceKey=" + molitApiKey;
	    
	    RestTemplate restTemplate = new RestTemplate();
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "Mozilla/5.0 (compatible; SpringBoot/2.0)");
	    headers.set("Accept", "*/*");
	    
	    HttpEntity<?> entity = new HttpEntity<>(headers);
	    
	    try {
	        ResponseEntity<String> response = restTemplate.exchange(
	            URI.create(url), 
	            HttpMethod.GET, 
	            entity, 
	            String.class
	        );
//	        System.out.println("response 아파트 매매 실거래가 ::::::::::"+response);
	        return response.getBody();
	        
	        
	        
	        
	    } catch (Exception e) {
	        return "API 호출 실패: " + e.getMessage();
	    }
		
	}

//	아파트 전월세 실거래가 자료
//	@GetMapping("/api/realestate/apt-rent")
	public String aptRent(String lawdCode, String dealYmd) {
		
		String url = "http://apis.data.go.kr/1613000/RTMSDataSvcAptRent/getRTMSDataSvcAptRent" +
				"?LAWD_CD=" + lawdCode + "&DEAL_YMD=" + dealYmd + "&serviceKey=" + molitApiKey;
		
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "Mozilla/5.0 (compatible; SpringBoot/2.0)");
		headers.set("Accept", "*/*");
		
		HttpEntity<?> entity = new HttpEntity<>(headers);
		
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					URI.create(url), 
					HttpMethod.GET, 
					entity, 
					String.class
					);
			
//			System.out.println("response 아파트 전월세 실거래가 자료 ::::::::::"+response);
			
			return response.getBody();
		} catch (Exception e) {
			return "API 호출 실패: " + e.getMessage();
		}
		
		
	}

//	오피스텔 매매 실거래가 자료
//	@GetMapping("/api/realestate/officetel-trade")
	public String officetelTrade(String lawdCode, String dealYmd) {
		
		String url = "http://apis.data.go.kr/1613000/RTMSDataSvcOffiTrade/getRTMSDataSvcOffiTrade" +
				"?LAWD_CD=" + lawdCode + "&DEAL_YMD=" + dealYmd + "&serviceKey=" + molitApiKey;
		
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "Mozilla/5.0 (compatible; SpringBoot/2.0)");
		headers.set("Accept", "*/*");
		
		HttpEntity<?> entity = new HttpEntity<>(headers);
		
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					URI.create(url), 
					HttpMethod.GET, 
					entity, 
					String.class
					);
			
//			System.out.println("response 오피스텔 매매 실거래가 자료 ::::::::::"+response);
			
			return response.getBody();
		} catch (Exception e) {
			return "API 호출 실패: " + e.getMessage();
		}
		
		
	}

//	오피스텔 전월세 실거래가 자료
//	@GetMapping("/api/realestate/officetel-rent")
	public String officetelRent(String lawdCode, String dealYmd) {
		
		String url = "http://apis.data.go.kr/1613000/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent" +
				"?LAWD_CD=" + lawdCode + "&DEAL_YMD=" + dealYmd + "&serviceKey=" + molitApiKey;
		
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "Mozilla/5.0 (compatible; SpringBoot/2.0)");
		headers.set("Accept", "*/*");
		
		HttpEntity<?> entity = new HttpEntity<>(headers);
		
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					URI.create(url), 
					HttpMethod.GET, 
					entity, 
					String.class
					);
			
//			System.out.println("response 오피스텔 전월세 실거래가 자료 ::::::::::"+response);
			
			return response.getBody();
		} catch (Exception e) {
			return "API 호출 실패: " + e.getMessage();
		}
		
		
	}
	
	
	
}
