package blackjackOS;

import java.util.Map;

import java.util.Random;

/* The purpose of this class is simply to run a Blackjack game a certain number of times, which a user can
 * feel free to alter, and it will then print out information about the results of these games in terms of
 * how many the player won and how many the dealer won. Of course, the dealer's decisions are predetermined,
 * continuing to draw cards until the total value of their cards reaches at least 17, but the player who is
 * challenging the dealer has a variety of decisions to choose from, such as hitting, standing, doubling,
 * splitting if their first two cards are the same value, and surrendering. The methods in the 
 * BlackjackOptimization class will determine the decisions that this program makes. */
public class BlackjackSimulation {
	 
	public static void main(String[] args) {

		// Stores the result of a simulation in an object
		SimulationResult sim = simulateGames(10000, 1, 1.0);
		// Prints the results of the simulation to the console
		sim.printResults();
		
	} 

	
	
	public static SimulationResult simulateGames(int games, int decks, double reshuffleRatio) {
		
		// Stores the difference between how many games the player wins and how many
		// games the dealer wins
		double difference = 0;

		// Stores the amount of cards of each type left in the shoe.
		// The zero index corresponds to 10 and the one index corresponds to aces.
		// Two corresponds to two, three to three, and so on.
		int[] cardsAtStart = { 16 * decks, 4 * decks, 4 * decks, 4 * decks, 4 * decks, 4 * decks, 4 * decks,
				4 * decks, 4 * decks, 4 * decks };
		

		// This will store how many games ended in the player gaining the following
		// numbers of value, relative to the size of their initial wager, 
		// in this order: [-4 -3 -2 -1 -0.5 0 1 1.5 2 3 4]
		int outcomeTotals[] = new int[11];

		// Stores the time at the start of the simulation
		long startTime = System.currentTimeMillis();
		
		// The array of cards left of each value still in the shoe; currently, all cards are in it as no
		// games have been simulated
		int[] trueCardsLeft = cardsAtStart.clone();
		
		// If at the start of any game, the number of cards in the shoe dips below this, the shoe will
		// be reset
		double cardThreshold = decks * 52 * reshuffleRatio;
		
		// Will store how much time has elapsed since the beginning of the simulations
		long elapsedTime = 0;

		// Plays a game of Blackjack a given amount of times
		for (int i = 0; i < games; i++) {

			// If the number of cards is less than the threshold needed to reset the shoe (putting all cards
			// back into it), the shoe is reset
			if ((double)getSum(trueCardsLeft) < cardThreshold) {
				trueCardsLeft = cardsAtStart.clone();
			}


			// Store whether the player and the dealer will hit. Initialized as true
			// because thus far, they have no reason not to be hitting.
			boolean playerHitting = true;
			boolean dealerHitting = true;

			// Store whether the player and dealer each have aces in their hand.
			// This is important as an ace can either be worth 1 or 11.
			boolean playerHasAce = false;
			boolean dealerHasAce = false;

			// Gets the player's first card.
			int playerFirst = hit(trueCardsLeft);

			// If it is an ace, as the "hit" method returns 1 for aces
			if (playerFirst == 1) {
				playerFirst = 11;
				playerHasAce = true;
			}

			// Gets the dealer's first card
			int dealerFirst = hit(trueCardsLeft);

			// If it is an ace
			if (dealerFirst == 1) {
				dealerFirst = 11;
				dealerHasAce = true;
			}

			// Gets the player's second card
			int playerSecond = hit(trueCardsLeft);

			// If it is an ace and the player does not have an ace. If the player were to
			// have
			// two aces that both counted as 11, their total would exceed 21
			if (playerSecond == 1 && !playerHasAce) {
				playerSecond = 11;
				playerHasAce = true;
			}

			int[] cardsLeft = trueCardsLeft.clone();

			// Gets the dealer's second card
			int dealerSecond = hit(trueCardsLeft);

			// If it is an ace and the dealer does not already have an ace. While the player
			// should not
			// know if the dealer's second card is an ace, the "dealerHasAce" boolean is not
			// used until
			// after the player is finished drawing cards
			if (dealerSecond == 1 && !dealerHasAce) {
				dealerSecond = 11;
				dealerHasAce = true;
			}

			// Stores the player's current total
			int playerTotal = playerFirst + playerSecond;

			// Stores whether either player got Blackjack, when their first two cards add up
			// to 21. The
			// game does not continue if this is true, so it needs to be checked
			boolean blackjackFound = false;
			// Stores whether the player got Blackjack
			boolean playerBlackjack = false;

			// If, after drawing their first two cards, the player's total is 21, they have
			// Blackjack
			if (playerTotal == 21) {
				blackjackFound = true;
				playerBlackjack = true;
			}

			if (dealerFirst + dealerSecond == 21) {
				blackjackFound = true;

				if (!playerBlackjack) {
					// If the dealer has Blackjack and the player doesn't, the dealer wins the hand.
					// The player having Blackjack is checked for because neither the player or
					// dealer
					// wins the hand if they both have Blackjack
					difference--;
					outcomeTotals[3]++;
				} else {
					outcomeTotals[5]++;
				}

			} else if (playerBlackjack) {
				// If the player has Blackjack and the dealer does not. In traditional
				// Blackjack, the
				// player effectively wins the game plus half of another game, so the number
				// tracking the
				// difference in games won will now reflect that
				difference += 1.5;
				outcomeTotals[7]++;
			}

			// Goes to the next game if Blackjack is found
			if (blackjackFound) {
				continue;
			}

			// Stores whether the player can split their hand, which is when their two cards
			// are the same
			boolean splittable = playerFirst == playerSecond || (playerFirst == 11 && playerSecond == 1);

			// Will store the probabilities of the dealer finishing with 17, 18, 19, 20, 21,
			// or over 21.
			double[] dealerProbs = BlackjackOptimization.getDealerProbs(dealerFirst, cardsLeft, 1, dealerFirst == 11,
					new double[6]);

			// Stores, in order, whether the player should hit, if they should double the
			// stakes of the game, the probability of winning if they hit, the probability of winning if
			// they stand, and a boolean which represents whether the player should surrender, conceding the
			// game right now to only effectively lose half of a game.
			// The method takes in as parameters the player's total, the dealer's probabilities of
			// finishing with different totals, the player's projected total, whether the player has an
			// ace, and the array of cards available to draw from.
			Object[] hitArray = BlackjackOptimization.shouldHit(playerTotal, dealerProbs, playerHasAce, cardsLeft);

			// If the player can split their hand
			if (splittable) {

				// If both of the player's cards are aces, then the total goes to 22 for the
				// purpose of the input for the split method being 11, as the totals are divided by two.
				if (playerFirst == 11 && playerSecond == 1) {
					playerTotal = 22;
				}

				// Stores, if the player were to split, the probabilities of them finishing with
				// 12, 13, 14...20, 21, and over 21.
				double[] splitPlayerProbs = BlackjackOptimization.getPlayerProbs(playerTotal / 2, cardsLeft, 1.0,
						playerHasAce, new double[11], dealerProbs);

				// Stores the probability of the player winning if they chose to split
				double winIfSplit = BlackjackOptimization.playerWinProb(splitPlayerProbs, dealerProbs);

				// Stores the probability of the player winning by finding the higher likelihood
				// between winning if they stand and winning if they hit. The boolean at the 0 index of
				// the hitArray corresponds to whichever number is higher.
				double winProb = Math.max((double) hitArray[2], (double) hitArray[3]);

				// Stores the expected difference in games won between the player and dealer if
				// the player elects not to split
				double noSplitEV = BlackjackOptimization.getExpectedValue(winProb);
				// Stores that same expected difference, if the player were to split. Multiplied
				// by two because the player is now playing two hands
				double splitEV = 2 * BlackjackOptimization.getExpectedValue(winIfSplit);
				// If the player will benefit more from splitting than not splitting, they split
				if (splitEV > noSplitEV) {

					// The difference will get added to it the result of the hand being split, a method
					// that returns the difference in effective games won between the hands that
					// were played once the player split
					double split = split(playerTotal / 2, dealerFirst, dealerSecond,
							dealerFirst == 11 || dealerSecond == 11, cardsLeft, trueCardsLeft);
					difference += split;
					incArr(outcomeTotals, split);

					// The current simulation is ended, as the split method plays through the entire
					// game
					continue;
				}

			}

			// Stores whether the player doubled, whether they can double their hand, and
			// whether their hand busted, meaning its value went over 21. The hand has not yet doubled or
			// busted, but it is doublable while there are only two cards in the player's hand.
			boolean doubled = false;
			boolean doublable = true;
			boolean playerBusted = false;

			// If the player total was set to 22 due to two aces being present and the hand
			// was not split, it is set back to 12 as the second ace counts as one.
			if (playerTotal == 22) {
				playerTotal = 12;
			}

			// If the player should surrender, they effectively lose half the game and
			// concede it, concluding the current game
			if ((boolean) hitArray[4]) {
				difference -= 0.5;
				incArr(outcomeTotals, -0.5);
				continue;
			}

			// A do-while loop which checks to see if the player should hit and double,
			// doing so if it is supposed to. Will be broken out of if the player shouldn't and therefore
			// does not hit.
			do {
				// If the hand is not doublable, meaning it has already been hit on, the hit
				// array is again found with the updated player total.
				if (!doublable) {
					// Stores the dealer's updated probabilities with the new cardsLeft array, as it has
					// been altered with the player drawing another card, and the player still does not
					// know the value of the dealer's second card
					double[] newDealerProbs = BlackjackOptimization.getDealerProbs(dealerFirst, cardsLeft, 1,
							dealerFirst == 11, new double[6]);

					// Updates the array with the player's new total and the dealer's new  probabilities
					hitArray = BlackjackOptimization.shouldHitAux(playerTotal, newDealerProbs, playerHasAce, cardsLeft);

				}

				// If the player should hit, they hit.
				if ((boolean) hitArray[0]) {

					// Stores the next card
					int nextCard = hit(trueCardsLeft);
					removeElements(nextCard, cardsLeft);

					// If the player should double and the hand is doublable
					if (doublable && (boolean) hitArray[1]) {
						doubled = true;
					}

					// If the card is an ace and the player does not yet have one, it counts as 11;
					// one otherwise
					if (nextCard == 1 && !playerHasAce) {
						playerTotal += 11;
						playerHasAce = true;
					} else {
						playerTotal += nextCard;
					}

					// If the total is greater than 21 and the player has an ace, the total is
					// decreased by 10; the ace now counts as one, not 11
					if (playerTotal > 21 && playerHasAce) {
						playerTotal -= 10;
						playerHasAce = false;
					}

					if (playerTotal > 21) {
						playerBusted = true;
						break;
					} else if (playerTotal > 18 || (playerTotal > 16 && !playerHasAce)) {
						playerHitting = false;
						break;
					}

					// If the player doubles, they cannot hit again
					if (doubled) {
						break;
					}

				}
				// If the player should not hit
				else {
					playerHitting = false;
				}

				// After the first card, the player can no longer double
				doublable = false;

				// The loop runs again if no reason has yet been found for the player not to hit
				// again
			} while (playerHitting);

			// Stores the dealer's total and whether the dealer busted
			int dealerTotal = dealerFirst + dealerSecond;
			boolean dealerBusted = false;

			dealerHitting = dealerTotal < 17;

			// A do-while loop which runs while the dealer is hitting.
		    while (dealerHitting) {
				// If the player busted, there is no need to check what the dealer has, as the
				// player automatically loses
			    if (playerBusted) {
					break; 
				} 

				// If the dealer's total is greater than 16, then the dealer does not hit and
				// the loop is broken out of
				if (dealerTotal > 16) { 
					break; 
				}

				// Gets the next card for the dealer
				int nextCard = hit(trueCardsLeft);

				// If it is equal to one and the dealer does not yet have an ace, the ace's value is 11
				if (nextCard == 1 && !dealerHasAce) {
					dealerTotal += 11;
					dealerHasAce = true;
				} else {
					dealerTotal += nextCard;
				}

				// If the dealer total goes over 21 and they have an ace, 10 is taken off as the
				// dealer's ace now counts as 1 rather than 11
				if (dealerTotal > 21 && dealerHasAce) {
					dealerTotal -= 10;
					dealerHasAce = false;
				}

				// The dealer stops hitting if they have 17 or more, and they busted and
				// automatically lose if their total is over 21
				if (dealerTotal > 16) {
					dealerHitting = false;
					if (dealerTotal > 21) {
						dealerBusted = true;
					}
				}

				// The loop keeps running while the dealer is hitting
			}

			// Stores the result of the game in terms of how many the player won in 
			// comparison to the dealer
			int result = 0;
			// If the player busted, the dealer wins
			if (playerBusted) { 
				result--; 

				// Otherwise, if the dealer busted, the player wins 
			} else if (dealerBusted) { 
				result++; 
 
				// Otherwise, if the player's total is greater than the dealer's, the player 
				// wins 
			} else if (playerTotal > dealerTotal) { 
				result++; 
 
				// Otherwise, if the dealer's total is greater than the player's, the dealer 
				// wins 
			} else if (dealerTotal > playerTotal) { 
				result--; 
			} 

			// If the player doubled the stakes of the game 
			if (doubled) { 
				result *= 2; 
			} 

			// The difference gets added to it the result of this game 
			incArr(outcomeTotals, result); 
			difference += result; 

			// How much time has elapsed, in milliseconds, since the program was initially 
			// run. Used to 
			// see its efficiency 
			elapsedTime = System.currentTimeMillis() - startTime; 

		} 
		
		// The player's win rate over the course of the simulations 
		double winRate = ((games + difference) / 2) / games; 
		double elapsedSeconds = (double)elapsedTime / 1000; 
		
		// Returns an object storing the information about the simulation 
		return new SimulationResult(games, decks, outcomeTotals, elapsedSeconds, winRate);


	}

	/*
	 * Will run two hands against a dealer after the player has split their original
	 * hand, which they can do if their first two cards have the same value. Takes
	 * in as parameters the player's current value in each hand (these are the
	 * same), the dealer's first card, whether the dealer has an ace, the array of
	 * cards remaining according to the player, and the actual cards remaining, when
	 * considering the dealer's second card. This method will return a double which
	 * stores the amount of games the player went up through these two hands. Each
	 * hand has the same value as the original hand, meaning that the total amount
	 * of winnable points has been doubled, if this method is called.
	 */
	public static double split(int num, int dealerFirst, int dealerSecond, boolean dealerHasAce, int[] cardsLeft,
			int[] trueCardsLeft) {

		// Stores the number which will be returned.
		double result = 0;

		// Creates the array which stores the final totals of both of the player's
		// hands.
		int[] arr = new int[2];

		// Gets the values for the next cards which will be dealt to each of the
		// player's hands.
		int hit1 = hit(trueCardsLeft);
		removeElements(hit1, cardsLeft);

		int hit2 = hit(trueCardsLeft);
		removeElements(hit2, cardsLeft);

		// Gets the totals of each of the player's hands after they've received their
		// second card
		int num1 = num + (hit1 == 1 ? 11 : hit1);
		int num2 = num + (hit2 == 1 ? 11 : hit2);

		// Stores the dealer's total
		int dealerTot = dealerFirst + dealerSecond;

		// Stores whether the player doubled each of their hands
		boolean doubledFirst = false;
		boolean doubledSecond = false;

		// This loop will simply play through each of the two hands
		for (int i = 0; i < 2; i++) {

			// Stores the player's current value in the hand that is currently being played
			// through
			int value = (i == 0 ? num1 : num2);

			// If the player started with an ace and then drew an ace, their total would go
			// to 22, which of course cannot happen, so one of the aces now counts as 1 and the player's
			// hand value goes to 12
			if (value == 22) {
				value = 12;
			}

			// A rule of Blackjack, or the variation that this program simulates, does not
			// allow the to hit a split ace after the original hit. So, this will conclude and the
			// player's current value for this hand is final
			if (num == 11) {
				arr[0] = num1;
				arr[1] = num2;
				break;

			}

			// Stores whether the player is hitting
			boolean playerHitting = true;

			// If the player can double
			boolean doublable = true;

			// If the player's first card was an ace or they drew an ace in this hand.
			// Essentially stores whether the player has an ace whose value is 11
			boolean playerHasAce = num == 11 || (i == 0 ? hit1 : hit2) == 1;

			// Starts off by looking to see if the player should hit, leaving the loop if
			// they shouldn't. But if they should, it will keep on looking to see if they should hit again
			// until they shouldn't
			do {

				// The dealer's probabilities of ending with certain totals
				double[] dealerProbs = BlackjackOptimization.getDealerProbs(dealerFirst, cardsLeft, 1,
						dealerFirst == 11, new double[6]);

				// Stores information about the player's best decisions to make in this current
				// situation. If this is the first decision checkpoint, the hand is doublable, so we have
				// to call shouldHit() to consider the possibilities of doubling and splitting. But if
				// it is not the first checkpoint, we don't consider those possibilities and can only call
				// shouldHitAux() for the sake of efficiency
				Object[] hitArray = doublable
						? BlackjackOptimization.shouldHit(value, dealerProbs, playerHasAce, cardsLeft)
						: BlackjackOptimization.shouldHitAux(value, dealerProbs, playerHasAce, cardsLeft);

				// If the player should hit
				if ((boolean) hitArray[0]) {

					// The player's next card
					int nextCard = hit(trueCardsLeft);
					removeElements(nextCard, cardsLeft);

					// If it is an ace and the player doesn't have an ace, it counts as 11
					if (nextCard == 1 && !playerHasAce) {
						nextCard = 11;
						playerHasAce = true;
					}

					// If the player should double the value of the game and they can double it,
					// which is when this is the first card they are receiving after splitting. They will
					// double in this case
					if (doublable && (boolean) hitArray[1]) {

						// The player cannot receive more than one card after splitting, meaning they
						// are no longer hitting
						playerHitting = false;

						// Stores that the stakes of the current hand were doubled
						if (i == 0) {
							doubledFirst = true;
						} else {
							doubledSecond = true;
						}

					}

					// Adds to the player's value the card they received
					value += nextCard;

					// If the player has an ace and their value went over 21, the ace will now count
					// as 1
					if (playerHasAce && value > 21) {
						value -= 10;
						playerHasAce = false;
					}

					// In no scenario where the player's value is greater than 18 or greater than 16
					// while having an ace is hitting again the optimal decision. So, we simply do
					// not bother checking what the player's best decision is for the sake of efficiency
					if (value >= 19 || (value >= 17 && !playerHasAce)) {
						playerHitting = false;
					}

				} else {
					// If the player should not hit; this will cause the do-while loop to terminate
					playerHitting = false;
				}

				// The player can no longer double after they hit once
				doublable = false;

			} while (playerHitting); // Breaks out of the loop if the player should no longer hit

			// Stores the value of this hand in the array that holds then
			arr[i] = value;

		}

		// The dealer is only hitting while their hand's value is less than 17, so they
		// will not even hit to begin with if that is the case
		boolean dealerHitting = dealerTot < 17;

		// While the dealer is supposed to hit, they will keep hitting
		while (dealerHitting) {

			// The value of their next card
			int nextCard = hit(trueCardsLeft);

			// If they don't have an ace and they drew an ace, it counts as 11
			if (!dealerHasAce && nextCard == 1) {
				nextCard = 11;
				dealerHasAce = true;
			}

			// The dealer gets added to their total the value of their new card
			dealerTot += nextCard;
			// If they have an ace in their hand and their total went over 21, the ace now
			// counts as 1 rather than 11
			if (dealerHasAce && dealerTot > 21) {
				dealerTot -= 10;
				dealerHasAce = false;
			}

			// If their total surpassed 16, they do not hit again
			if (dealerTot > 16) {
				dealerHitting = false;
			}
		}

		// This loop will go through both of the values that the player ended with
		for (int i = 0; i < 2; i++) {

			// If the player doubled the value of the current hand
			boolean doubled = (i == 0 ? doubledFirst : doubledSecond);

			// If the player busted or went over 21, they lost the hand
			if (arr[i] > 21) {
				result -= (doubled ? 2 : 1);

			} else if (dealerTot > 21) {
				// Otherwise, if the dealer busted, the player wins
				result += (doubled ? 2 : 1);

			} else if (arr[i] > dealerTot) {
				// If the player's total is greater than the dealer's
				result += (doubled ? 2 : 1);

			} else if (arr[i] < dealerTot) {
				// If the dealer's total is greater than the player's
				result -= (doubled ? 2 : 1);

			}
		}

		return result;

	}

	/*
	 * Returns a number corresponding to a card randomly chosen from the cards
	 * available. Returns 1 for an ace
	 */
	public static int hit(int[] remainingCards) {
		Random random = new Random();
		// Gets a random number below the number of cards remaining and assigns the
		// corresponding card value 
		int numCardsLeft = getSum(remainingCards);
		 
		// Gets a random number below the number of cards remaining
		int randomInt = random.nextInt(0, numCardsLeft);
		  

		// Essentially translates the value of the random number into a value of a card
		int nextCard = getVal(randomInt, remainingCards);          

		// Removes this card from the shoe
		removeElements(nextCard, remainingCards);

		// If the value is 0, 10 needs to be returned instead, as getVal returns 0 through 9
		return (nextCard == 0 ? 10 : nextCard);

	}

	/*
	 * Returns the count of all the values in an integer array. Used for the purpose
	 * of finding the probability that the dealer or player ends with a certain
	 * number, as the return value of this method will be the denominator in such
	 * fractions throughout other methods in this class.
	 */
	public static int getSum(int[] arr) {
		int sum = 0;
		// Loops through all of the numbers, adding them together
		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
		}
		return sum;
	}

	/* Returns the value of a number as it corresponds to the amount of cards left
	 * in the shoe. Essentially, all of the remaining cards are lined up, starting
	 * with tens/jacks/queens/kings, then going to aces, then twos, and so on. This
	 * method will get the value of the card at the location corresponding to the
	 * randomInt parameter.
	 */
	private static int getVal(int randomInt, int[] cardsLeft) {
		int tracker = 0; // Stores the amount of cards which have been iterated over
		int val = 0; // Stores the value of the card which will be returned
		// Loops through the cardsLeft array, adding to the tracker how many cards of
		// each number there are
		for (int i = 0; i < cardsLeft.length; i++) {
			tracker += cardsLeft[i];
			// If the number becomes less than the tracker, the current index is returned
			if (randomInt < tracker) {
				return i;
			}
		}

		return val;

	}

	/* Removes from the array of remaining cards, a specified card */
	public static void removeElements(int num, int[] cardsLeft) {
		// If the value of the card which needs to be removed is 10, 1 is subtracted at
		// the 0 index
		cardsLeft[num == 10 ? 0 : num == 11 ? 1 : num]--;
	}

    /* These following methods are used to store the amounts of games that all
	 * possible results took place in. The Map stores the corresponding indeces in
	 * the outcomeTotals array of each of these possible results. The
	 * printOutcomeTotals method finds the percent of all the games that each
	 * outcome occurred in, and prints them out.
	 */
	private static Map<Double, Integer> resultToIdx = Map.ofEntries(Map.entry(-4.0, 0), Map.entry(-3.0, 1),
			Map.entry(-2.0, 2), Map.entry(-1.0, 3), Map.entry(-0.5, 4), Map.entry(0.0, 5), Map.entry(1.0, 6),
			Map.entry(1.5, 7), Map.entry(2.0, 8), Map.entry(3.0, 9), Map.entry(4.0, 10));

	
	/* Increments the outcomeTotals array at the index that stores the result of the game that was just
	 * simulated; this is called in every simulation only once the result has been determined. */
	private static void incArr(int[] outcomeTotals, double result) {
		outcomeTotals[resultToIdx.get(result)]++;
	}



}