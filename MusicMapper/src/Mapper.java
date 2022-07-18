import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.lang.Math;

/**
 * Mapper class used to graph bands and connect them based on genre, year
 * formed, origin of band members, etc.
 */
public class Mapper {

	/**
	 * Band class primarily used for the purpose of sorting.
	 */
	public class Band implements Comparable<Band> {

		protected int weight;
		protected String name;

		public Band(String name, int weight) {
			this.weight = weight;
			this.name = name;

		}

		@Override
		public int compareTo(Band o) {
			if (weight > o.weight)
				return 1;
			else if (weight == o.weight)
				return 0;
			return -1;
		}

	}

	ArrayList<String> bands;
	Graph<String> bandsGraph;
	HashMap<String, LinkedList<String>> genres;
	HashMap<String, Integer> yearFormed;
	HashMap<String, Integer> numFans;
	HashMap<String, LinkedList<String>> countries;

	public Mapper() {
		bands = new ArrayList<String>();
		bandsGraph = new Graph<String>();
		genres = new HashMap<String, LinkedList<String>>();
		countries = new HashMap<String, LinkedList<String>>();
		yearFormed = new HashMap<String, Integer>();
		numFans = new HashMap<String, Integer>();

	}

	/**
	 * Creates a graph of the bands.
	 * 
	 * @return true if graph successfully created.
	 * @throws IOException if data unsuccessfully loaded.
	 */
	public boolean createGraph(String data) throws IOException {

		boolean succeeded = true;

		// https://www.codegrepper.com/code-examples/java/reading+line+breaks+from+text+file+in+java
		FileReader fr;
		fr = new FileReader(System.getProperty("user.dir") + File.separator + "data" + File.separator + data);
		BufferedReader br = new BufferedReader(fr);

		try {
			String currRow = br.readLine();
			currRow = br.readLine();

			while (currRow != null) {
				String bandName = currRow.split(",")[0].trim().toLowerCase();

				bands.add(bandName);
				bandsGraph.insertVertex(bandName);

				// numFans is divided by 500 because I didn't want the difference in fans
				// to play a large role in the overall weight.
				numFans.put(bandName, (int) Math.ceil(Double.parseDouble(currRow.split(",")[1]) / 500));
				yearFormed.put(bandName, Integer.parseInt(currRow.split(",")[2]));

				// For each band, a list of the countries the members are from are stored in a
				// hash map.
				String[] bandCountries = currRow.split(",")[3].split("_");// Underscore separates country names
				LinkedList<String> temp = new LinkedList<String>();
				for (int i = 0; i < bandCountries.length; i++) {
					temp.add(bandCountries[i]);
				}
				countries.put(bandName, temp);

				// A hash map of genres is created. Key is the genre, and value is a list of
				// bands belonging to that genre.
				String[] genre = currRow.split(",")[4].split("_");
				for (int i = 0; i < genre.length; i++) {
					if (genres.get(genre[i].trim()) == null) {
						genres.put(genre[i].trim(), new LinkedList<String>());
					}
					if (!genres.get(genre[i].trim()).contains(bandName)) {
						genres.get(genre[i].trim()).add(bandName);
					}
				}
				currRow = br.readLine();
			}
		} catch (IOException e) {
			succeeded = false;
		} finally {
			fr.close();
		}

		return succeeded;
	}

	/**
	 * Adds edges between edges bands that have matching genres.
	 * 
	 * @return true if successfully creates all the edges. False otherwise.
	 */
	public boolean addEdges() {

		// Condition most likely returns true if createGraph() hasn't been ran yet.
		if (genres == null || bandsGraph == null) {
			return false;
		}

		// An edge between two bands is created if they share at least one genre.
		// To determine this, we iterate through each key of the dictionary twice,
		// Creating an edge between nodes that share a key (genre).

		for (String key : genres.keySet()) {
			// value and otherValue are the bands that will have an edge made between them.
			for (String value : genres.get(key)) {
				for (String otherValue : genres.get(key)) {
					// Don't want a band with an edge to itself.
					if (!value.equals(otherValue)) {
						// If two bands share multiple genres, we don't want to create two edges, but
						// rather update the original edge's eight.
						if (!bandsGraph.containsEdge(value, otherValue)) {
							int weight = 10;

							// Larger year difference means they're less similar, thus increasing the
							// weight.
							int yearDifference = Math.abs(yearFormed.get(value) - yearFormed.get(otherValue));
							// Larger fan difference means they're less similar, thus increasing the weight.
							int fanDifference = Math.abs(numFans.get(value) - numFans.get(otherValue));
							weight = weight + fanDifference + yearDifference;

							// Being from the same country means they're more similar, thus reducing the
							// weight.
							for (String country : countries.get(value)) {
								if (countries.get(otherValue).contains(country)) {
									weight = Math.max(weight--, 0);
								}
							}
							bandsGraph.insertEdge(value, otherValue, weight);
						}

						// If bands share multiple genres, enters else statement.
						// More matching genres means they're more similar, thus reducing the weight.
						// To update the edge, we remove it, saving its weight,
						// and create a new one using original weight minus 2 (because they have another
						// matching genre).

						else {
							int weight = bandsGraph.getWeight(value, otherValue);
							bandsGraph.removeEdge(value, otherValue);
							weight = Math.max(0, weight - 2);// Matching genres = more similar = smaller weight
							bandsGraph.insertEdge(value, otherValue, weight);
						}
					}
				}
			}

		}
		return true;
	}

	/**
	 * Makes music recommendations based on the created graph.
	 * 
	 * @param input a list of the bands the user has given.
	 * @return An arraylist of band objects that are similar to the given input.
	 */
	public ArrayList<Band> findRecommendations(String[] input) {
		// For each given band, we will run a modified version of dijkstra's shortest
		// path.
		// This version will return us a every other band the given bands can reach.
		LinkedList<LinkedList<String>> paths = new LinkedList<LinkedList<String>>();

		for (int i = 0; i < input.length; i++) {
			if (bandsGraph.containsVertex(input[i].trim().toLowerCase())) {
				paths.add(bandsGraph.dijkstrasShortestPath(input[i].trim().toLowerCase()));

			} else {
				System.out.println(
						input[i].trim() + " was not found in the graph. Skipping it and moving on to the next band.");
			}
		}

		HashMap<String, Integer[]> matchingBandsHM = new HashMap<String, Integer[]>();
		ArrayList<Band> matchingBands = null;

		if (paths.size() > 0) {
			// If multiple bands are given, we want to give recommendations that take all
			// the given bands into account.
			// Thus, we'll only look into bands in the dataset that are connected to at
			// least 2 of the given bands (assuming
			// we are given more than 1 band).

			// If a band in the dataset is in two or more of the given bands. We will sum up
			// the "weight" between
			// the band in the dataset and each of the given bands it's connected to. We
			// will aslo track how many
			// bands it's connected to.
			int totalWeight;

			// i will represent one of the bands, n represents the other band.
			for (int i = 0; i < paths.size(); i++) {
				for (int n = i; n < paths.size(); n++) {
					if (n != i || paths.size() == 1) {

						int smallerSize = Math.min(paths.get(i).size(), paths.get(n).size());
						// b represents each band in the dataset.
						for (int b = 0; b < smallerSize; b++) {
							totalWeight = 0;

							// If a band in the dataset is multiple paths.
							if (paths.get(i).contains(paths.get(n).get(b))) {

								// While running dijkstra's shortest path and getting the path distance would be
								// a better way
								// to get the weight between a band from the dataset and each of the given bands
								// its connected to,
								// it would be inefficient. As a result, a slightly less accurate but more
								// efficient method will be used.
								// The path is found using dijkstra's shortest path algorithm, so its index is
								// related to how
								// Connected the band is to the given bands. As a result, we will be using that
								// instead.
								totalWeight += paths.get(i).indexOf(paths.get(n).get(b));
								totalWeight += paths.get(n).indexOf(paths.get(n).get(b));

								Integer[] bandInfo = { totalWeight, 1 };

								// If the current band in the dataset matches more than 2 bands, the condition
								// below
								// will evaluate to true. In this situation, we wont' add another item to the
								// hashMap,
								// Rather, we'll just update it.
								if (matchingBandsHM.containsKey(paths.get(n).get(b))) {
									bandInfo[0] += matchingBandsHM.get(paths.get(n).get(b))[0];
									bandInfo[1] = matchingBandsHM.get(paths.get(n).get(b))[1] + 1;

								}

								matchingBandsHM.put(paths.get(n).get(b), bandInfo);

							}

						}
					}
				}
			}

			// When selecting the final bands to display, we want to prioritize bands in the
			// dataset with more connections to the given bands.
			// So, the program tries to fill up an array of matching bands with the most
			// connections first.
			matchingBands = new ArrayList<Band>();
			int currSize = paths.size();
			while (matchingBands.size() < 1000 && currSize > 0) {
				for (String key : matchingBandsHM.keySet()) {
					if (matchingBandsHM.get(key)[1] == currSize) {
						// Band objects are used so that they can be easily sorted in an array according
						// to its weight attribute
						matchingBands.add(new Band(key, matchingBandsHM.get(key)[0] / matchingBandsHM.get(key)[1]));
					}
				}

				currSize--;
			}
		}
		return matchingBands;

	}

	/**
	 * Checks if given band matches the input.
	 * 
	 * @param input
	 * @param band
	 * @return true if band matches input, false otherwise.
	 */
	public static boolean inInput(String[] input, String band) {
		for (int n = 0; n < input.length; n++) {
			if (input[n].trim().toLowerCase().equals(band)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {

		System.out.println("Setting up, please wait a few moments.\n");

		Mapper map = new Mapper();

		// Setting up the file path
		File file = new File(System.getProperty("user.dir"));
		String parentPath = file.getAbsoluteFile().getParent();
		try {
			map.createGraph("testData.csv");
		} catch (IOException IOE) {
			System.out.println("An unexpected error occured when loading the data and creating the graph!"
					+ "Please verify that the data is in the correct folder (labeled 'data') and try again");
			System.exit(1);
		}

		// If the graph was created successfully, will now be addign edges.
		map.addEdges();

		System.out.println("Welcome to the music mapper! \n"
				+ "Please input a series of bands that you like, and I will find some good recommendations for you."
				+ "Please note: Bands MUST be separated with a comma!");
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		String[] input = myObj.nextLine().split(","); // Read user input
		System.out.println("Brilliant! Generating your results now! This may take a minute or two.\n");

		ArrayList<Band> matchingBands = map.findRecommendations(input);

		// Verifying that the program could create reccomendations.
		if (matchingBands == null || matchingBands.size() == 0) {
			System.out.println("An unexpected error occured when trying to find the matching bands. "
					+ "This may happen if none of the inputted songs were in the graph.");
			myObj.close();
			System.exit(1);
		}

		// Sorting the array so that the most similar bands are first.
		matchingBands.sort(null);

		System.out.println("Your recommendations are ready! Here they are!");

		Band currBand;
		int i = 1;
		while (i <= 5 && matchingBands.size() != 0) {
			currBand = matchingBands.remove(0);
			if (!inInput(input, currBand.name)) {
				System.out.println(i + ". " + currBand.name + " ");
				i++;
			}
		}
		myObj.close();
	}

}