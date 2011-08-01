package com.kentchiu.eslpod.formatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import org.json.JSONArray;

public class GoogleSuggestFormatterTest extends TestCase {
	public void testExtractShowPart() throws Exception {
		String json = "[\"test\",[[\"test\",\"試驗, 化驗;化驗法;化驗劑, 檢驗;檢驗標準, 測驗;考察;小考, 考驗, 試驗;檢驗;測驗, 化驗,分析, 考驗;考察, 受試驗;受測驗, 測得結果, (為鑑定而)進行測驗\",\"0\"],[\"testa\",\"【植】種子之外堅皮;【動】甲殼;介殼\",\"1\"],[\"testament\",\"(大寫)聖經舊約;聖經新約, 【律】遺囑,遺言, 證明,證據, 信仰聲明, 【古】(人與上帝間的)聖約\",\"2\"],[\"tester\",\"試驗員;測試器\",\"3\"],[\"testimony\",\"【律】證詞,證言, 證據,證明;表徵, (信仰等的)聲明,公開表白, (基督教的)摩西十誡\",\"4\"],[\"testing\",\"傷腦筋的,棘手的\",\"5\"],[\"testis\",\"睪丸\",\"6\"],[\"testosterone\",\"【生化】【藥】睪(甾)酮,睪丸素(一種男性荷爾蒙)\",\"7\"],[\"testudo\",\"(古羅馬軍隊攻城時用的)龜甲形掩蔽物, 【動】陸龜\",\"8\"],[\"testy\",\"易怒的;暴躁的;不耐煩的\",\"9\"]],{\"k\":1}]";
		JSONArray ja3 = GoogleSuggestFormatter.extractShowPart(json);
		assertThat(ja3.getString(0), is("test"));
		assertThat(ja3.getString(1), is("試驗, 化驗;化驗法;化驗劑, 檢驗;檢驗標準, 測驗;考察;小考, 考驗, 試驗;檢驗;測驗, 化驗,分析, 考驗;考察, 受試驗;受測驗, 測得結果, (為鑑定而)進行測驗"));
	}

	public void testFormatText() throws Exception {
		String json = "[\"test\",[[\"test\",\"試驗, 化驗;化驗法;化驗劑, 檢驗;檢驗標準, 測驗;考察;小考, 考驗, 試驗;檢驗;測驗, 化驗,分析, 考驗;考察, 受試驗;受測驗, 測得結果, (為鑑定而)進行測驗\",\"0\"],[\"testa\",\"【植】種子之外堅皮;【動】甲殼;介殼\",\"1\"],[\"testament\",\"(大寫)聖經舊約;聖經新約, 【律】遺囑,遺言, 證明,證據, 信仰聲明, 【古】(人與上帝間的)聖約\",\"2\"],[\"tester\",\"試驗員;測試器\",\"3\"],[\"testimony\",\"【律】證詞,證言, 證據,證明;表徵, (信仰等的)聲明,公開表白, (基督教的)摩西十誡\",\"4\"],[\"testing\",\"傷腦筋的,棘手的\",\"5\"],[\"testis\",\"睪丸\",\"6\"],[\"testosterone\",\"【生化】【藥】睪(甾)酮,睪丸素(一種男性荷爾蒙)\",\"7\"],[\"testudo\",\"(古羅馬軍隊攻城時用的)龜甲形掩蔽物, 【動】陸龜\",\"8\"],[\"testy\",\"易怒的;暴躁的;不耐煩的\",\"9\"]],{\"k\":1}]";
		String text = GoogleSuggestFormatter.formatText(json);
		assertThat(text, is("試驗, 化驗;化驗法;化驗劑, 檢驗;檢驗標準, 測驗;考察;小考, 考驗, 試驗;檢驗;測驗, 化驗,分析, 考驗;考察, 受試驗;受測驗, 測得結果, (為鑑定而)進行測驗"));

	}
}
