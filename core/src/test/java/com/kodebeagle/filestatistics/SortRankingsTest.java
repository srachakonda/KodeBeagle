package com.kodebeagle.filestatistics;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class SortRankingsTest {

	Map<String, Integer> fileRankings = new HashMap<String, Integer>();
	Map<String, Integer> fileRankings1 = new HashMap<String, Integer>();
	int noOfFiles = 5;

	@Before
	public void setup() {

		setFileRankings(fileRankings);

	}

	public void setFileRankings(Map<String, Integer> fileRankings) {

		fileRankings.put("Test1.java", 1);
		fileRankings.put("Test2.java", 3);
		fileRankings.put("Test3.java", 5);
		fileRankings.put("Test4.java", 10);
		this.fileRankings = fileRankings;
	}

	@Test
	public void testSortRankingsMap() {
		fileRankings1 = SortRankings.sortRankingsMap(fileRankings, 2);
		assertEquals(2, fileRankings1.size());
	}

}
