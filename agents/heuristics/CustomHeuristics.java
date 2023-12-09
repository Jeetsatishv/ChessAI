package hw2.agents.heuristics;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.player.Player;
import hw2.agents.heuristics.DefaultHeuristics.DefensiveHeuristics;
import hw2.agents.heuristics.DefaultHeuristics.OffensiveHeuristics;
import hw2.chess.game.history.History;
import hw2.chess.game.move.PromotePawnMove;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.PlayerType;
import hw2.chess.search.DFSTreeNode;
import hw2.chess.utils.Coordinate;

public class CustomHeuristics
{
	
	
	
	public static double pawnStructure(DFSTreeNode node){
		
		int doubledPawns = 0;
	    int isolatedPawns = 0;
	    int passedPawns = 0;
	    
	    for (Piece piece : node.getGame().getBoard().getPieces(node.getGame().getCurrentPlayer())) {
	        if (piece.getType() == PieceType.PAWN) {
	            Coordinate pawnPosition = node.getGame().getCurrentPosition(piece);
	            int pawnColumn = pawnPosition.getXPosition();
	            int pawnRow = pawnPosition.getYPosition();

	            // Check for doubled pawns
	            for (Piece otherPiece : node.getGame().getBoard().getPieces(node.getGame().getCurrentPlayer())) {
	                if (otherPiece.getType() == PieceType.PAWN && otherPiece != piece) {
	                    Coordinate otherPawnPosition = node.getGame().getCurrentPosition(otherPiece);
	                    if (otherPawnPosition.getXPosition() == pawnColumn && otherPawnPosition.getYPosition() > pawnRow) {
	                        doubledPawns++;
	                        break;
	                    }
	                }
	            }

	            // Check for isolated pawns
	            boolean hasNeighbor = false;
	            for (int columnOffset = -1; columnOffset <= 1; columnOffset += 2) {
	                if (pawnColumn + columnOffset >= 0 && pawnColumn + columnOffset < 8) {
	                    for (Piece otherPiece : node.getGame().getBoard().getPieces(node.getGame().getCurrentPlayer())) {
	                        if (otherPiece.getType() == PieceType.PAWN) {
	                            Coordinate otherPawnPosition = node.getGame().getCurrentPosition(otherPiece);
	                            if (otherPawnPosition.getXPosition() == pawnColumn + columnOffset) {
	                                hasNeighbor = true;
	                                break;
	                            }
	                        }
	                    }
	                }
	                if (hasNeighbor) break;
	            }
	            if (!hasNeighbor) isolatedPawns++;

	            // Check for passed pawns
	            boolean isPassed = true;
	            for (Piece opponentPiece : node.getGame().getBoard().getPieces(node.getGame().getOtherPlayer())) {
	                if (opponentPiece.getType() == PieceType.PAWN) {
	                    Coordinate opponentPawnPosition = node.getGame().getCurrentPosition(opponentPiece);
	                    if (opponentPawnPosition.getXPosition() >= pawnColumn - 1 && opponentPawnPosition.getXPosition() <= pawnColumn + 1 &&
	                            ((node.getGame().getCurrentPlayer().getPlayerType() == PlayerType.WHITE && opponentPawnPosition.getYPosition() > pawnRow) ||
	                                    (node.getGame().getCurrentPlayer().getPlayerType() != PlayerType.WHITE && opponentPawnPosition.getYPosition() < pawnRow))) {
	                        isPassed = false;
	                        break;
	                    }
	                }
	            }
	            if (isPassed) passedPawns++;
	        }
	    }

	    double pawnStructureScore = -0.5 * doubledPawns - 0.75 * isolatedPawns + passedPawns;
//	    System.out.println("The pawn score is" + pawnStructureScore);
	    return pawnStructureScore;
					
	}
	
	
	public static class OffensiveHeuristics extends Object
	{

		public static int getNumberOfPiecesWeAreThreatening(DFSTreeNode node)
		{
			int numPiecesWeAreThreatening = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(node.getGame().getCurrentPlayer()))
			{
				numPiecesWeAreThreatening += piece.getAllCaptureMoves(node.getGame()).size();
			}
			return numPiecesWeAreThreatening;
		}
		
		

	}

	public static class DefensiveHeuristics extends Object
	{

		public static int getNumberOfAlivePieces(DFSTreeNode node)
		{
			int numPiecesAlive = 0;
			for(PieceType pieceType : PieceType.values())
			{
				numPiecesAlive += node.getGame().getNumberOfAlivePieces(node.getGame().getCurrentPlayer(), pieceType);
			}
			if (node.getGame().getNumberOfAlivePieces(node.getGame().getCurrentPlayer(), PieceType.QUEEN) == 1) {
				return numPiecesAlive+10;
			}
			else {
				return numPiecesAlive;
			}
			
		}

		public static int getClampedPieceValueTotalSurroundingKing(DFSTreeNode node)
		{
			// what is the state of the pieces next to the king? add up the values of the neighboring pieces
			// positive value for friendly pieces and negative value for enemy pieces (will clamp at 0)
			int kingSurroundingPiecesValueTotal = 0;

			Piece kingPiece = node.getGame().getBoard().getPieces(node.getGame().getCurrentPlayer(), PieceType.KING).iterator().next();
			Coordinate kingPosition = node.getGame().getCurrentPosition(kingPiece);
			for(Direction direction : Direction.values())
			{
				Coordinate neightborPosition = kingPosition.getNeighbor(direction);
				if(node.getGame().getBoard().isInbounds(neightborPosition) && node.getGame().getBoard().isPositionOccupied(neightborPosition))
				{
					Piece piece = node.getGame().getBoard().getPieceAtPosition(neightborPosition);
					int pieceValue = Piece.getPointValue(piece.getType());
					if(piece != null && kingPiece.isEnemyPiece(piece))
					{
						kingSurroundingPiecesValueTotal -= pieceValue;
					} else if(piece != null && !kingPiece.isEnemyPiece(piece))
					{
						kingSurroundingPiecesValueTotal += pieceValue;
					}
				}
			}
			// kingSurroundingPiecesValueTotal cannot be < 0 b/c the utility of losing a game is 0, so all of our utility values should be at least 0
			kingSurroundingPiecesValueTotal = Math.max(kingSurroundingPiecesValueTotal, 0);
			return kingSurroundingPiecesValueTotal;
		}

		public static int getNumberOfPiecesThreateningUs(DFSTreeNode node)
		{
			// how many pieces are threatening us?
			int numPiecesThreateningUs = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(node.getGame().getOtherPlayer()))
			{
				numPiecesThreateningUs += piece.getAllCaptureMoves(node.getGame()).size();
			}
			return numPiecesThreateningUs;
		}
		
	}

	public static double getOffensiveHeuristicValue(DFSTreeNode node)
	{
		// remember the action has already taken affect at this point, so capture moves have already resolved
		// and the targeted piece will not exist inside the game anymore.
		// however this value was recorded in the amount of points that the player has earned in this node
		double damageDealtInThisNode = node.getGame().getBoard().getPointsEarned(node.getGame().getCurrentPlayer());

		switch(node.getMove().getType())
		{
		case PROMOTEPAWNMOVE:
			PromotePawnMove promoteMove = (PromotePawnMove)node.getMove();
			damageDealtInThisNode += Piece.getPointValue(promoteMove.getPromotedPieceType());
			break;
		default:
			break;
		}
		// offense can typically include the number of pieces that our pieces are currently threatening
		int numPiecesWeAreThreatening = OffensiveHeuristics.getNumberOfPiecesWeAreThreatening(node);
		
//		System.out.println("The number of pieces we are threatening are " + numPiecesWeAreThreatening);

		return damageDealtInThisNode + numPiecesWeAreThreatening;
	}

	public static double getDefensiveHeuristicValue(DFSTreeNode node)
	{
		// how many pieces exist on our team?
		int numPiecesAlive = DefensiveHeuristics.getNumberOfAlivePieces(node);

		// what is the state of the pieces next to the king? add up the values of the neighboring pieces
		// positive value for friendly pieces and negative value for enemy pieces (will clamp at 0)
		int kingSurroundingPiecesValueTotal = DefensiveHeuristics.getClampedPieceValueTotalSurroundingKing(node);

		// how many pieces are threatening us?
		int numPiecesThreateningUs = DefensiveHeuristics.getNumberOfPiecesThreateningUs(node);

		return numPiecesAlive + kingSurroundingPiecesValueTotal - numPiecesThreateningUs;
	}

	public static double getNonlinearPieceCombinationHeuristicValue(DFSTreeNode node)
	{
		// both bishops are worth more together than a single bishop alone
		// same with knights...we want to encourage keeping pairs of elements
		double multiPieceValueTotal = 0.0;

		double exponent = 1.5; // f(numberOfKnights) = (numberOfKnights)^exponent

		// go over all the piece types that have more than one copy in the game (including pawn promotion)
		for(PieceType pieceType : new PieceType[] {PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.QUEEN})
		{
			multiPieceValueTotal += Math.pow(node.getGame().getNumberOfAlivePieces(node.getGame().getCurrentPlayer(), pieceType), exponent);
		}

		return multiPieceValueTotal;
	}

	
	
//	public static double Development(DFSTreeNode node) {
//		double devScore = 0;
//
//	    // Define the central area coordinates
//	    int[] centralRows = {4, 5};
//	    int[] centralColumns = {1, 2, 3, 4, 5, 6, 7, 8};
//
//	    for (Piece piece : node.getGame().getBoard().getPieces(node.getGame().getCurrentPlayer())) {
//	        Coordinate piecePosition = node.getGame().getCurrentPosition(piece);
//	        int pieceRow = piecePosition.getYPosition();
//	        int pieceColumn = piecePosition.getXPosition();
//
//	        // Check if the piece is in the central area
//	        if (arrayContains(centralRows, pieceRow) && arrayContains(centralColumns, pieceColumn)) {
//	        	System.out.println("Central row is = " + centralRows);
//	        	System.out.println("Central Column is = " + centralColumns);
//	        	System.out.println("PieceRow is = " + pieceRow);
//	        	System.out.println("PieceColumn is = " + pieceColumn);
//	            // Assign points based on the piece type
//	            switch (piece.getType()) {
//	                case PAWN:
//	                    devScore += 1;
//	                    break;
//	                case KNIGHT:
//	                    devScore += 5;
//	                    break;
//	                case BISHOP:
//	                    devScore += 3;
//	                    break;
//	                case ROOK:
//	                    devScore += 0;
//	                    break;
//	                case QUEEN:
//	                    devScore += 0;
//	                    break;
//	                default:
//	                    break;
//	            }
//	        }
//	    }
//
//	    return devScore;
//	}
	
	public static double Development(DFSTreeNode node) {
		double devScore = 0;

	    // Define the central area coordinates

		
	    Player Vladimir;

		if (node.getMaxPlayer().getPlayerID() == 1) {
			Vladimir = new Player(1, PlayerType.WHITE);
			
			
		} else {
			Vladimir = new Player(0, PlayerType.BLACK);
		
		}
	    
	    int[] centralColumns = {3, 4, 5, 6};
	    int[] centralRowsBlack = {4};
	    int[] centralRowsWhite = {5};
	    
	    
	    for (Piece piece : node.getGame().getBoard().getPieces(Vladimir)) {
	        Coordinate piecePosition = node.getGame().getCurrentPosition(piece);
	        int pieceRow = piecePosition.getYPosition();
	        int pieceColumn = piecePosition.getXPosition();

	        // Check if the piece is in the central area
	        if(Vladimir.getPlayerType() == PlayerType.WHITE) {
	        	if (arrayContains(centralRowsWhite, pieceRow) && arrayContains(centralColumns, pieceColumn)) {
//		        	System.out.println("Central row is = " + centralRows);
//		        	System.out.println("Central Column is = " + centralColumns);
//		        	System.out.println("PieceRow is = " + pieceRow);
//		        	System.out.println("PieceColumn is = " + pieceColumn);
		            // Assign points based on the piece type
		            switch (piece.getType()) {
		                case PAWN:
		                    devScore += 1;
		                    break;
		                case KNIGHT:
		                    devScore += 3;
		                    break;
		                case BISHOP:
		                    devScore += 3;
		                    break;
		                case ROOK:
		                    devScore += 0;
		                    break;
		                case QUEEN:
		                    devScore += 0;
		                    break;
		                default:
		                    break;
		            }
		        }
	        } else {
	        	
	        	if (arrayContains(centralRowsBlack, pieceRow) && arrayContains(centralColumns, pieceColumn)) {
//		        	System.out.println("Central row is = " + centralRows);
//		        	System.out.println("Central Column is = " + centralColumns);
//		        	System.out.println("PieceRow is = " + pieceRow);
//		        	System.out.println("PieceColumn is = " + pieceColumn);
		            // Assign points based on the piece type
		            switch (piece.getType()) {
		                case PAWN:
		                    devScore += 1;
		                    break;
		                case KNIGHT:
		                    devScore += 3;
		                    break;
		                case BISHOP:
		                    devScore += 3;
		                    break;
		                case ROOK:
		                    devScore += 0;
		                    break;
		                case QUEEN:
		                    devScore += 0;
		                    break;
		                default:
		                    break;
		            }
		        }
	        	
	        }
	    }

	    return devScore;
	}

	private static boolean arrayContains(int[] array, int value) {
	    for (int element : array) {
	        if (element == value) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
	
	
	public static double getMaterialHeuristic(DFSTreeNode node)
    {
		Player Vladimir;
		Player Opponent;
		if (node.getMaxPlayer().getPlayerID() == 1) {
			Vladimir = new Player(1, PlayerType.WHITE);
			Opponent = new Player(0, PlayerType.BLACK);
		} else {
			Vladimir = new Player(0, PlayerType.BLACK);
			Opponent = new Player(1, PlayerType.WHITE);
		}
		
        int sumOfMaterial = 0;
        for(Piece piece : node.getGame().getBoard().getPieces(Vladimir))
        {
            if (piece.getType() == PieceType.PAWN) {
                
                sumOfMaterial += 1;
                
            } else if (piece.getType() == PieceType.BISHOP) {
                
                sumOfMaterial += 3;
                
            } else if (piece.getType() == PieceType.KNIGHT) {
                
                sumOfMaterial += 3;
                
            } else if (piece.getType() == PieceType.ROOK) {
                
                sumOfMaterial += 5;
                
            } else if (piece.getType() == PieceType.QUEEN) {
                
                sumOfMaterial += 9;
                
            }
        }
        
        int enemySumOfMaterial = 0;
        for(Piece piece : node.getGame().getBoard().getPieces(Opponent))
        {
            if (piece.getType() == PieceType.PAWN) {
                
            	enemySumOfMaterial += 1;
                
            } else if (piece.getType() == PieceType.BISHOP) {
                
            	enemySumOfMaterial += 3;
                
            } else if (piece.getType() == PieceType.KNIGHT) {
                
            	enemySumOfMaterial += 3;
                
            } else if (piece.getType() == PieceType.ROOK) {
                
            	enemySumOfMaterial += 5;
                
            } else if (piece.getType() == PieceType.QUEEN) {
                
            	enemySumOfMaterial += 9;
                
            }
        }
        
//        System.out.println("You have " + sumOfMaterial);
//        System.out.println("Enemy has " + enemySumOfMaterial);
        
        
        return 10*(sumOfMaterial - enemySumOfMaterial);
    }
	
	public static double moveAwayInit(DFSTreeNode node) {
		int historyLen = History.getHistory().size();
		int DEFAULTHISTLEN = 20;
		
		PlayerType i;
		Player Vladimir;
		Player Opponent;
		if (node.getMaxPlayer().getPlayerID() == 1) {
			Vladimir = new Player(1, PlayerType.WHITE);
			Opponent = new Player(0, PlayerType.BLACK);
			i = PlayerType.WHITE;
		} else {
			Vladimir = new Player(0, PlayerType.BLACK);
			Opponent = new Player(1, PlayerType.WHITE);
			i = PlayerType.BLACK;
		}
		
		double val = 0;
		
		for(Piece piece : node.getGame().getBoard().getPieces(Vladimir)) {
			if (piece.getType() == PieceType.PAWN) {
				 Coordinate piecePosition = node.getGame().getCurrentPosition(piece);
			        int pieceRow = piecePosition.getYPosition();
			        int pieceColumn = piecePosition.getXPosition();
			        if (i == PlayerType.WHITE) {
			        	if (historyLen < DEFAULTHISTLEN) {
				        	if ((pieceRow == 5 && pieceColumn == 4) || ((pieceRow == 5 && pieceColumn == 5))) {
				        		val += 0.5;
				        	}
			        	}
			        }
			        else {
			        	if (historyLen < DEFAULTHISTLEN) {
				        	if ((pieceRow == 4 && pieceColumn == 4) || ((pieceRow == 4 && pieceColumn == 5))) {
				        		val += 0.5;
				        	}
			        	}
			        }
			}
			
			
			
			if (piece.getType() == PieceType.BISHOP) {
				 Coordinate piecePosition = node.getGame().getCurrentPosition(piece);
			        int pieceRow = piecePosition.getYPosition();
			        int pieceColumn = piecePosition.getXPosition();
			        if (i == PlayerType.WHITE) {
			        	if (historyLen < DEFAULTHISTLEN) {
				        	if ((pieceRow == 7 && pieceColumn == 2) || ((pieceRow == 7 && pieceColumn == 7))) {
				        		val += 0.5;
				        	}
			        	}
			        }
			        else {
			        	if (historyLen < DEFAULTHISTLEN) {
				        	if ((pieceRow == 2 && pieceColumn == 2) || (pieceRow == 2 && pieceColumn == 7)) {
				        		val += 0.5;
				        	}
			        	}
			        }
			}
			
//			if (piece.getType() == PieceType.ROOK) {
//				 Coordinate piecePosition = node.getGame().getCurrentPosition(piece);
//			        int pieceRow = piecePosition.getYPosition();
//			        int pieceColumn = piecePosition.getXPosition();
//			        if (i == PlayerType.WHITE) {
//			        	if ((pieceRow != 8 && pieceColumn != 1) || ((pieceRow != 8 && pieceColumn != 8))) {
//			        		val += 3;
//			        	}
//			        }
//			        else {
//			        	if ((pieceRow != 1 && pieceColumn != 1) || ((pieceRow != 1 && pieceColumn != 8))) {
//			        		val += 3;
//			        	}
//			        }
//			}
			
			
			if (piece.getType() == PieceType.KNIGHT) {
				 Coordinate piecePosition = node.getGame().getCurrentPosition(piece);
			        int pieceRow = piecePosition.getYPosition();
			        int pieceColumn = piecePosition.getXPosition();
			        if (i == PlayerType.WHITE) {
			        	if (historyLen <= DEFAULTHISTLEN) {
				        	if ((pieceRow == 6 && pieceColumn == 6) || ((pieceRow == 6 && pieceColumn == 3))) {
				        		val += 0.5;
				        	}
			        	}
			        }
			        else {
			        	if (historyLen <= DEFAULTHISTLEN) {
				        	if ((pieceRow == 3 && pieceColumn == 6) || ((pieceRow == 3 && pieceColumn == 1))) {
				        		val += 0.5;
				        	}
			        	}
			        }
			}
			
			
		}
		
//		System.out.println("The score to Move Away from Initial Position is = " + val);
		
		return val;
	}
	
	
	

	/**
	 * TODO: implement me! The heuristics that I wrote are useful, but not very good for a good chessbot.
	 * Please use this class to add your heuristics here! I recommend taking a look at the ones I provided for you
	 * in DefaultHeuristics.java (which is in the same directory as this file)
	 */
	public static double getHeuristicValue(DFSTreeNode node)
	{
		
		double offenseHeuristicValue = getOffensiveHeuristicValue(node);
		double defenseHeuristicValue = getDefensiveHeuristicValue(node);
		double nonlinearHeuristicValue = getNonlinearPieceCombinationHeuristicValue(node);
		double pawnStructureValue = pawnStructure(node);
		double materialValue = getMaterialHeuristic(node);
		double developmentValue = Development(node);
		double moveAway = moveAwayInit(node);
	
		
//		System.out.println("Offense = " + offenseHeuristicValue);
//		System.out.println("Defense = " + defenseHeuristicValue);
//		System.out.println("NonLinear = " + nonlinearHeuristicValue);
//		System.out.println("Material = " + materialValue);
//		System.out.println("Development = " + developmentValue);
		
		
		// please replace this!
		return (moveAway + offenseHeuristicValue + defenseHeuristicValue + nonlinearHeuristicValue + materialValue + pawnStructureValue + developmentValue);
	}

}