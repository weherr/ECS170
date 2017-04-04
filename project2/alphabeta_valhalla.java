import java.lang.Math;


public class alphabeta_valhalla extends AIModule
{
	private int maxPlayer;
	private int minPlayer;
	
	private static int[][] winRDiagonalMatrix = {
		{0, 0, 0, 1, 1, 1, 1},
		{0, 0, 1, 2, 2, 2, 1},
		{0, 1, 2, 3, 3, 2, 1},
		{1, 2, 3, 3, 2, 1, 0},
		{1, 2, 2, 2, 1, 0, 0},
		{1, 1, 1, 1, 0, 0, 0}
	};
	private static int[][] winLDiagonalMatrix = {
		{1, 1, 1, 1, 0, 0, 0},
		{1, 2, 2, 2, 1, 9, 0},
		{1, 2, 3, 3, 3, 1, 0},
		{0, 1, 2, 3, 3, 2, 1},
		{0, 0, 1, 2, 2, 2, 1},
		{0, 0, 0, 1, 1, 1, 1}
	};
	private static int[] winUpMatrix = {1, 2, 3, 3, 2, 1};
	private static int[] winLRMatrix = {1, 2, 3, 4, 3, 2, 1};

	public void getNextMove(final GameStateModule state)
	{
		maxPlayer = state.getActivePlayer();
		if (maxPlayer == 1)
			minPlayer = 2;
		else
			minPlayer = 1;

		chosenMove = iterativeAlphaBetaDecision(state);
	}

	/**
	* @return the chosenMove
	*/
	public int iterativeAlphaBetaDecision(final GameStateModule state)
	{
		GameStateModule stateCopy = state.copy();
		int maxAction = 0;
		int maxV = Integer.MIN_VALUE;
		int v = 0;
		int maxDepth = Integer.MAX_VALUE;
		int d;
		for(d = 1; d < maxDepth && !terminate; d++)
		{
			for(int i = 0; i < stateCopy.getWidth(); i++){
				if(stateCopy.canMakeMove(i))
				{
					stateCopy.makeMove(i);
					v = minValue(stateCopy, Integer.MIN_VALUE, Integer.MAX_VALUE, d);
					if (v >= maxV)
					{
						maxV = v;
						maxAction = i;
					}
					stateCopy.unMakeMove();
				}
			}
		}
		System.out.println("Depth: " + d);
		return maxAction;
	}

	public int maxValue(final GameStateModule state, int alpha, int beta, int depth)
	{
		//if terminate that means the time limit is about up so return
		if (state.isGameOver() || terminate || depth <= 0)
		{
			return eval(state);
		}
		int v = Integer.MIN_VALUE;
		//want to find smallest values first which is the sides
		for(int i = 0; i < 2; i++){ //left side
			if(state.canMakeMove(i))
			{
				state.makeMove(i);
				v = Math.max(v, minValue(state, alpha, beta, depth-1));
				state.unMakeMove();
				if(v >= beta)
					return v;
				alpha = Math.max(v, alpha);
			}
		}
		for(int i = 5; i < 7; i++){ //right side
			if(state.canMakeMove(i))
			{
				state.makeMove(i);
				v = Math.max(v, minValue(state, alpha, beta, depth-1));
				state.unMakeMove();
				if(v >= beta)
					return v;
				alpha = Math.max(v, alpha);
			}
		}
		for(int i = 2; i < 5; i++){ //middle
			if(state.canMakeMove(i))
			{
				state.makeMove(i);
				v = Math.max(v, minValue(state, alpha, beta, depth-1));
				state.unMakeMove();
				if(v >= beta)
					return v;
				alpha = Math.max(v, alpha);
			}
		}
		return v;
	}

	public int minValue(final GameStateModule state, int alpha, int beta, int depth)
	{
		//if terminate that means the time limit is about up so return
		if (state.isGameOver() || terminate || depth <= 0)
		{
			return eval(state);
		}
		int v = Integer.MAX_VALUE;
		//want to find largest values first which is the middle
		for(int i = 2; i < 5; i++){ //middle
			if(state.canMakeMove(i))
			{
				state.makeMove(i);
				v = Math.min(v, maxValue(state, alpha, beta, depth-1));
				state.unMakeMove();
				if(v <= alpha)
					return v;
				beta = Math.min(v, beta);
			}
		}
		for(int i = 0; i < 2; i++){ //left side
			if(state.canMakeMove(i))
			{
				state.makeMove(i);
				v = Math.min(v, maxValue(state, alpha, beta, depth-1));
				state.unMakeMove();
				if(v <= alpha)
					return v;
				beta = Math.min(v, beta);
			}
		}
		for(int i = 5; i < 7; i++){ //right side
			if(state.canMakeMove(i))
			{
				state.makeMove(i);
				v = Math.min(v, maxValue(state, alpha, beta, depth-1));
				state.unMakeMove();
				if(v <= alpha)
					return v;
				beta = Math.min(v, beta);
			}
		}
		return v;
	}

	public int eval(final GameStateModule state)
	{
		if (state.isGameOver()) //game is over so check who winner is
		{
			if (state.getWinner() == maxPlayer)
				return Integer.MAX_VALUE;
			else if  (state.getWinner() == 0)
				return 0;
			else
				return Integer.MIN_VALUE;
		}
		else
		{
			int possibleWins = 0;
			for(int c = 0; c < state.getWidth(); c++)
			{
				for(int r = 0; r < state.getHeight(); r++)
				{
					if (terminate) //so we don't forfeit
						return possibleWins;

					if (state.getAt(c, r) == maxPlayer) //offensive strategy
					{
						//up
						if (r+1 <= 5 && (state.getAt(c, r+1) == maxPlayer || state.getAt(c, r+1) == 0))
						{
							possibleWins += winUpMatrix[r];
							if (r+2 <= 5 && (state.getAt(c, r+2) == maxPlayer || state.getAt(c, r+2) == 0))
							{
								possibleWins += 2 * winUpMatrix[r];
								if (r+3 <= 5 && (state.getAt(c, r+3) == 0))
								{
									possibleWins += 4 * winUpMatrix[r];
								}
							}
						}

						//left
						if (c-1 >= 0 && (state.getAt(c-1, r) == maxPlayer || state.getAt(c-1, r) == 0))
						{
							possibleWins += winLRMatrix[c];
							if (c-2 >= 0 && (state.getAt(c-2, r) == maxPlayer || state.getAt(c-2, r) == 0))
							{
								possibleWins += 2 * winLRMatrix[c];
								if (c-3 >= 0 && (state.getAt(c-3, r) == 0))
								{
									possibleWins += 4 * winLRMatrix[c];
								}
							}
						}

						 //right
						if (c+1 <= 6 && (state.getAt(c+1, r) == maxPlayer || state.getAt(c+1, r) == 0))
						{
							possibleWins += winLRMatrix[c];
							if (c+2 <= 6 && (state.getAt(c+2, r) == maxPlayer || state.getAt(c+2, r) == 0))
							{
								possibleWins += 2 * winLRMatrix[c];
								if (c+3 <= 6 && (state.getAt(c+3, r) == 0))
								{
									possibleWins += 4 * winLRMatrix[c];
								}
							}
						}

						//up diagonal right
						if (r+1 <= 5 && c+1 <= 6 && (state.getAt(c+1, r+1) == maxPlayer || state.getAt(c+1, r+1) == 0))
						{
							possibleWins += winRDiagonalMatrix[r][c];
							if (r+2 <= 5 && c+2 <= 6 && (state.getAt(c+2, r+2) == maxPlayer || state.getAt(c+2, r+2) == 0))
							{
								possibleWins += 2 * winRDiagonalMatrix[r][c];
								if (r+3 <= 5 && c+3 <= 6 && (state.getAt(c+3, r+3) == 0))
								{
									possibleWins += 4 * winRDiagonalMatrix[r][c];
								}
							}
						}

						//up diagonal left
						if (r+1 <= 5 && c-1 >= 0 && (state.getAt(c-1, r+1) == maxPlayer || state.getAt(c-1, r+1) == 0))
						{
							possibleWins += winLDiagonalMatrix[r][c];
							if (r+2 <= 5 && c-2 >= 0 && (state.getAt(c-2, r+2) == maxPlayer || state.getAt(c-2, r+2) == 0))
							{
								possibleWins += 2 * winLDiagonalMatrix[r][c];
								if (r+3 <= 5 && c-3 >= 0 && (state.getAt(c-3, r+3) == 0))
								{
									possibleWins += 4 * winLDiagonalMatrix[r][c];
								}
							}
						}
					}
					else if (state.getAt(c, r) == minPlayer) //blocking strategy
					{
						//up
						if (r+1 <= 5 && state.getAt(c, r+1) == minPlayer)
						{
							if ( (r+2 <= 5 && state.getAt(c, r+2) == maxPlayer) || 
								 (r-1 >= 0 && state.getAt(c, r-1) == maxPlayer) )
							{
								possibleWins += 50;
							}
						}

						//left
						if (c-1 >= 0 && state.getAt(c-1, r) == minPlayer)
						{
							if ((c-2 >= 0 && state.getAt(c-2, r) == maxPlayer) ||
								(c+1 <= 6 && state.getAt(c+1, r) == maxPlayer) )
							{
								possibleWins += 50;
							}
						}

						 //right
						if (c+1 <= 6 && state.getAt(c+1, r) == minPlayer)
						{
							if ((c+2 <= 6 && state.getAt(c+2, r) == maxPlayer) ||
								(c-1 >= 0 && state.getAt(c-1, r) == maxPlayer) )
							{
								possibleWins += 50;
							}
						}

						//up diagonal right
						if (r+1 <= 5 && c+1 <= 6 && state.getAt(c+1, r+1) == minPlayer)
						{
							if ( (r+2 <= 5 && c+2 <= 6 && state.getAt(c+2, r+2) == maxPlayer) ||
								 (r-1 >= 0 && c-1 >= 0 && state.getAt(c-1, r-1) == maxPlayer) )
							{
								possibleWins += 50;
							}
						}

						//up diagonal left
						if (r+1 <= 5 && c-1 >= 0 && (state.getAt(c-1, r+1) == minPlayer || state.getAt(c-1, r+1) == 0))
						{
							if ( (r+2 <= 5 && c-2 >= 0 && state.getAt(c+2, r+2) == maxPlayer) ||
								 (r+1 <= 5 && c+1 <= 6 && state.getAt(c+1, r+1) == maxPlayer) )
							{
								possibleWins += 50;
							}
						}
					}
				}
			}
			return possibleWins;
		}
	}
}
