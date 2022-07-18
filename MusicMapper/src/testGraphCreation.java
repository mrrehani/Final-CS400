import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class testGraphCreation {

	Mapper mapper = new Mapper();

	@Test
	void testErrorHandling() throws IOException {

		// Verifying the correct error is thrown when trying to create graph with faulty
		// data.
		try {
			mapper.createGraph("doesntExist.csv");
		} catch (IOException correctError) {
		} catch (Exception wrongError) {
			System.out.println(wrongError.getMessage());
			fail();
		}

		// Verifying that a graph can be created with the correct data.
		try {
			mapper.createGraph("testData.csv");
			mapper.addEdges();
		} catch (IOException e) {
			System.out.println("Failed to create graph. Recieved the following error: " + e.getMessage());
			fail("Program failed to create graph.");
		}

		// At this point, the graph should have been created.
		// Now, we will verify the program handles bad input properly.
		String[] faultyInput = { "nonExistantSong" };
		ArrayList<Mapper.Band> reccs = mapper.findRecommendations(faultyInput);
		if (reccs != null) {
			fail();
		}

	}

	@Test
	void testGraphCreation() throws IOException {
		mapper.createGraph("testData.csv");
		mapper.addEdges();

		// Verifies an edge between two bands that share a genre exists.
		// In this case, the shared genre is Symphonic
		assertEquals(true, mapper.bandsGraph.containsEdge("iron maiden", "metallica"));
		assertEquals(true, mapper.bandsGraph.containsEdge("iron maiden", "black sabbath"));
		assertEquals(true, mapper.bandsGraph.containsEdge("iron maiden", "megadeth"));
		assertEquals(true, mapper.bandsGraph.containsEdge("dream theater", "opeth"));
		assertEquals(true, mapper.bandsGraph.containsEdge("megadeth", "slayer"));
		assertEquals(true, mapper.bandsGraph.containsEdge("megadeth", "metallica"));
		assertEquals(true, mapper.bandsGraph.containsEdge("black sabbath", "metallica"));

		assertEquals(true, mapper.bandsGraph.containsEdge("metallica", "iron maiden"));
		assertEquals(true, mapper.bandsGraph.containsEdge("megadeth", "iron maiden"));
		assertEquals(true, mapper.bandsGraph.containsEdge("black sabbath", "iron maiden"));
		assertEquals(true, mapper.bandsGraph.containsEdge("slayer", "megadeth"));
		assertEquals(true, mapper.bandsGraph.containsEdge("opeth", "dream theater"));
		assertEquals(true, mapper.bandsGraph.containsEdge("metallica", "megadeth"));
		assertEquals(true, mapper.bandsGraph.containsEdge("metallica", "black sabbath"));

		// Verifies an edge between two bands that don't share a genre doesn't exist.
		assertEquals(false, mapper.bandsGraph.containsEdge("iron maiden", "opeth"));
		assertEquals(false, mapper.bandsGraph.containsEdge("iron maiden", "dream theater"));
		assertEquals(false, mapper.bandsGraph.containsEdge("iron maiden", "slayer"));
		assertEquals(false, mapper.bandsGraph.containsEdge("iron maiden", "death"));
		assertEquals(false, mapper.bandsGraph.containsEdge("iron maiden", "amon amarth"));

		assertEquals(false, mapper.bandsGraph.containsEdge("megadeth", "death"));
		assertEquals(false, mapper.bandsGraph.containsEdge("megadeth", "opeth"));
		assertEquals(false, mapper.bandsGraph.containsEdge("megadeth", "amon amarth"));

		assertEquals(false, mapper.bandsGraph.containsEdge("black sabbath", "death"));
		assertEquals(false, mapper.bandsGraph.containsEdge("black sabbath", "opeth"));
		assertEquals(false, mapper.bandsGraph.containsEdge("black sabbath", "amon amarth"));
		assertEquals(false, mapper.bandsGraph.containsEdge("black sabbath", "dream theater"));

		assertEquals(false, mapper.bandsGraph.containsEdge("metallica", "death"));
		assertEquals(false, mapper.bandsGraph.containsEdge("metallica", "opeth"));
		assertEquals(false, mapper.bandsGraph.containsEdge("metallica", "amon amarth"));
		assertEquals(false, mapper.bandsGraph.containsEdge("metallica", "dream theater"));
		assertEquals(false, mapper.bandsGraph.containsEdge("metallica", "slayer"));

		assertEquals(false, mapper.bandsGraph.containsEdge("slayer", "metallica"));
		assertEquals(false, mapper.bandsGraph.containsEdge("slayer", "black sabbath"));
		assertEquals(false, mapper.bandsGraph.containsEdge("slayer", "iron maiden"));
		assertEquals(false, mapper.bandsGraph.containsEdge("slayer", "death"));
		assertEquals(false, mapper.bandsGraph.containsEdge("slayer", "opeth"));
		assertEquals(false, mapper.bandsGraph.containsEdge("slayer", "amon amarth"));
		assertEquals(false, mapper.bandsGraph.containsEdge("slayer", "dream theater"));

	}

	@Test
	void testPathCreation() throws IOException {
		mapper.createGraph("testData.csv");
		mapper.addEdges();

		// Verifying path cost is accurate.
		// Megadeth and Black Sabbath directly share an edge.
		// The weight of the edge should start off at 10 like any other edge,
		// then add 15 for the year difference (1968 vs 1983) and 2 for the fan
		// difference (5 vs 7).
		// That brings us up to 27. We then subtract 2 for any additional genre shared.
		// Since the bands share one extra genre, we subtract 2, leaving us with 25.
		assertEquals(25, mapper.bandsGraph.getWeight("black sabbath", "megadeth"));

		// The same logic yields 20 for the edge between Iron Maiden and Megadeth.
		assertEquals(20, mapper.bandsGraph.getWeight("iron maiden", "megadeth"));

		// From Iron Maiden to Slayer, we have a few possible routes.
		// Iron Maiden-Black Sabbath-Megadeth-Slayer (total weight of 59)
		// Iron Maiden-Metallica-Megadeth-Slayer (total weight of 50)
		// Iron Maiden-Megadeth-Slayer (total weight of 33)
		// Dijktra's shortest path should return a weight of 33 as it should choose the
		// third path.
		assertEquals(33, mapper.bandsGraph.getPathCost("iron maiden", "slayer"));
	}
	
	@Test
	void testInputHandling() throws IOException {
		mapper.createGraph("testData.csv");
		mapper.addEdges();
		
		//Verifying that capital letters/additional spaces are handled properly.
		String[] input = { " IRON MAIDEN           ", "sLAYer" };
		ArrayList<Mapper.Band> matchingBands = mapper.findRecommendations(input);
		String[] expectedOutput = { "metallica", "megadeth", "black sabbath" };

		//We want to verify that all bands in the expected output are in the actual output.
		//To do this, we must first create a new arraylist that doesn't contain Band objects, but rather String objects. 
		ArrayList<String> bandsNames = new ArrayList<String>();
		for (Mapper.Band band : matchingBands) {
			bandsNames.add(band.name);
		}
		
		for (int i = 0; i < expectedOutput.length; i++) {
			if (!bandsNames.contains(expectedOutput[i])) fail();
		}
	}

	@Test
	void testReccomendations() throws IOException {

		Mapper.Band currBand;
		ArrayList<Mapper.Band> matchingBands;

		mapper.createGraph("testData.csv");
		mapper.addEdges();

		String[] input = { "iron maiden", "slayer" };
		String[] expectedOutput = { "metallica", "megadeth", "black sabbath" };
		matchingBands = mapper.findRecommendations(input);

		int i = 0;
		while (i <= 5 && matchingBands.size() != 0) {
			matchingBands.sort(null);
			currBand = matchingBands.remove(0);
			if (!Mapper.inInput(input, currBand.name)) {
				if (!expectedOutput[i].equals(currBand.name)) {
					fail();
				}
				i++;
			}

		}

		String[] inputTwo = { "slayer" };
		String[] expectedOutputTwo = { "megadeth", "metallica", "iron maiden", "black sabbath" };
		matchingBands = mapper.findRecommendations(inputTwo);

		i = 0;
		while (i <= 5 && matchingBands.size() != 0) {
			matchingBands.sort(null);
			currBand = matchingBands.remove(0);
			if (!Mapper.inInput(inputTwo, currBand.name)) {
				if (!expectedOutputTwo[i].equals(currBand.name)) {
					fail();
				}
				i++;
			}

		}
	}

}
