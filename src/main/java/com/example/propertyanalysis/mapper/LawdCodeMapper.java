package com.example.propertyanalysis.mapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class LawdCodeMapper {
	
	private final Map<String, String> codeMap = new HashMap<>();
	
	public LawdCodeMapper() {
        // =============================================================
        // 서울특별시 (11) - 25개 자치구
        // =============================================================
        codeMap.put("서울_종로구", "11110");
        codeMap.put("서울_중구", "11140");
        codeMap.put("서울_용산구", "11170");
        codeMap.put("서울_성동구", "11200");
        codeMap.put("서울_광진구", "11215");
        codeMap.put("서울_동대문구", "11230");
        codeMap.put("서울_중랑구", "11260");
        codeMap.put("서울_성북구", "11290");
        codeMap.put("서울_강북구", "11305");
        codeMap.put("서울_도봉구", "11320");
        codeMap.put("서울_노원구", "11350");
        codeMap.put("서울_은평구", "11380");
        codeMap.put("서울_서대문구", "11410");
        codeMap.put("서울_마포구", "11440");
        codeMap.put("서울_양천구", "11470");
        codeMap.put("서울_강서구", "11500");
        codeMap.put("서울_구로구", "11530");
        codeMap.put("서울_금천구", "11545");
        codeMap.put("서울_영등포구", "11560");
        codeMap.put("서울_동작구", "11590");
        codeMap.put("서울_관악구", "11620");
        codeMap.put("서울_서초구", "11650");
        codeMap.put("서울_강남구", "11680");
        codeMap.put("서울_송파구", "11710");
        codeMap.put("서울_강동구", "11740");
        
        
        // =============================================================
        // 인천광역시 (28) - 8개 자치구, 2개 군
        // =============================================================
        codeMap.put("인천_중구", "28110");
        codeMap.put("인천_동구", "28140");
        codeMap.put("인천_미추홀구", "28177"); // 구 남구
        codeMap.put("인천_연수구", "28185");
        codeMap.put("인천_남동구", "28200");
        codeMap.put("인천_부평구", "28237");
        codeMap.put("인천_계양구", "28245");
        codeMap.put("인천_서구", "28260");
        codeMap.put("인천_강화군", "28710");
        codeMap.put("인천_옹진군", "28720");
        
        
        // =============================================================
        // 경기도 (41) - 28개 시, 3개 군
        // =============================================================
        
        // 시 지역 (28개)
        codeMap.put("경기_수원시", "41110");
        codeMap.put("경기_성남시", "41130");
        codeMap.put("경기_의정부시", "41150");
        codeMap.put("경기_안양시", "41170");
        codeMap.put("경기_부천시", "41190");
        codeMap.put("경기_광명시", "41210");
        codeMap.put("경기_평택시", "41220");
        codeMap.put("경기_동두천시", "41250");
        codeMap.put("경기_안산시", "41270");
        codeMap.put("경기_고양시", "41280");
        codeMap.put("경기_과천시", "41290");
        codeMap.put("경기_구리시", "41310");
        codeMap.put("경기_남양주시", "41360");
        codeMap.put("경기_오산시", "41370");
        codeMap.put("경기_시흥시", "41390");
        codeMap.put("경기_군포시", "41410");
        codeMap.put("경기_의왕시", "41430");
        codeMap.put("경기_하남시", "41450");
        codeMap.put("경기_용인시", "41460");
        codeMap.put("경기_파주시", "41480");
        codeMap.put("경기_이천시", "41500");
        codeMap.put("경기_안성시", "41550");
        codeMap.put("경기_김포시", "41570");
        codeMap.put("경기_화성시", "41590");
        codeMap.put("경기_광주시", "41610");
        codeMap.put("경기_양주시", "41630");
        codeMap.put("경기_포천시", "41650");
        codeMap.put("경기_여주시", "41670");
        
        // 군 지역 (3개)
        codeMap.put("경기_연천군", "41800");
        codeMap.put("경기_가평군", "41820");
        codeMap.put("경기_양평군", "41830");
	}
	
    /**
     * 지역명으로 법정동코드를 조회합니다.
     * @param regionName 지역명 (예: "서울_강남구", "경기_수원시")
     * @return 법정동코드 (5자리)
     */
	public String getCode(String sido, String sigungu) {
		return codeMap.get(sido + "_" + sigungu);
	}
	
    /**
     * 법정동코드로 지역명을 조회합니다.
     * @param code 법정동코드 (5자리)
     * @return 지역명
     */
	public String getRegionName(String code) {
		return codeMap.entrySet().stream()
				.filter(entry -> entry.getValue().equals(code))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}
}
