package blackjackOS;

/* This class represents an object that stores information about a simulation of Blackjack games, played
 * using the optimization methods. */
public class SimulationResult {
	
	int games;
	int decks;
	double[] outcomeProportions;
	double secondsElapsed;
	double gamesPerSecond;
	double winRate;
	double houseEdge;
	double playerEdge; 
	 
	/* Constructor */
	public SimulationResult(int games, int decks, int[] outcomeTotals, double secondsElapsed, 
			double winRate) {
		this.games = games;
		this.decks = decks;
		// Gets the proportions of each outcome amount from the totals
		this.outcomeProportions = outcomePropsFromTots(outcomeTotals, games);
		this.secondsElapsed = secondsElapsed;
		this.winRate = winRate;
		this.gamesPerSecond = games / secondsElapsed;
		this.houseEdge = (winRate - 0.5) * -2; // how "house edge" is defined in Blackjack
		this.playerEdge = -this.houseEdge; // opposite of house edge
	}
	
	/* This finds the proportions of the games simulated that ended up in the player winning each of the 
	 * possible outcome amounts, relative to their wager. 
	 * These are -4, -3, -2, -1, -0.5, 0, 1, 1.5, 2, 3, and 4. The outcomeTotals and outcomeProportions 
	 * are arrays of size 11 and they store the total number of games and proportions (respectively) of 
	 * each amount in the aforementioned order. */
	private static double[] outcomePropsFromTots(int[] outcomeTotals, int games) {
		
		double[] outcomeProportions = new double[11];
		for (int i = 0; i < 11; i++) {
			// For each possible amount, we divide the number of games that ended in each outcome by the
			// total number of games simulated
			outcomeProportions[i] = (double)outcomeTotals[i] / games;
		}
		return outcomeProportions; 
		  
	}
	
	/* Prints out results of the simulation, which are described by the variables */
	public void printResults() {
		
		System.out.println("Games played: " + games);
		System.out.println("Seconds elapsed: " + secondsElapsed);
		System.out.println("Games per second: " + gamesPerSecond);
		System.out.println("Win rate: " + winRate);
		System.out.printf("House edge: %.5f percent\n", houseEdge * 100);
		System.out.printf("Player edge: %.5f percent\n", playerEdge * 100);
		  
		 
		System.out.println("\nOutcome | Proportion");
		System.out.printf("-4      | %.10f\n", outcomeProportions[0]);
		System.out.printf("-3      | %.10f\n", outcomeProportions[1]);
		System.out.printf("-2      | %.10f\n", outcomeProportions[2]);
		System.out.printf("-1      | %.10f\n", outcomeProportions[3]);
		System.out.printf("-0.5    | %.10f\n", outcomeProportions[4]);
		System.out.printf("0       | %.10f\n", outcomeProportions[5]);
		System.out.printf("1       | %.10f\n", outcomeProportions[6]);
		System.out.printf("1.5     | %.10f\n", outcomeProportions[7]);
		System.out.printf("2       | %.10f\n", outcomeProportions[8]);
		System.out.printf("3       | %.10f\n", outcomeProportions[9]);
		System.out.printf("4       | %.10f\n", outcomeProportions[10]);
		
	}
	
	

}
