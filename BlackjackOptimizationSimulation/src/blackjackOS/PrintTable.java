package blackjackOS;

public class PrintTable {
	
	/* This method will print out the current probability distribution of the totals the player and dealer
	 * could finish with. It is used in the shouldHitAux method from the BlackjackPlayer class. */
	
	public static void tablePrint(double[] playerProbs, double[] dealerProbs) {
		System.out.print("  Player |      17 |      18 |      19 |      20 |      21 |    Bust |   Total |");
		System.out.println("\n--------------------------------------------------------------------------------");
		for (int i = 0; i < 11; i++) {
			if (i == 10) { 
				System.out.print("   " + "Bust" + "  | ");
			} else {    
				System.out.print("     " + (i + 12) + "  | ");
			}
			for (int j = 0; j < 6; j++) {
				double number = playerProbs[i] * dealerProbs[j];
				System.out.print("");
				System.out.printf("%.5f | ", number);
			} 
			System.out.printf("%.5f | ", playerProbs[i]);
			System.out.println("\n--------------------------------------------------------------------------------");
		} 
		System.out.printf("  Total  | %.5f | %.5f | %.5f | %.5f | %.5f | %.5f | %.5f | ", 
				dealerProbs[0], dealerProbs[1], dealerProbs[2], dealerProbs[3], dealerProbs[4], dealerProbs[5], 1.0);
		System.out.println("\n\n\n\n\n\n\n\n\n");
	}

	
}
	

	

