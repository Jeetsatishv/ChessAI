package hw2.agents.moveorder;

import java.util.LinkedList;
import java.util.List;


import edu.cwru.sepia.util.Direction;
import hw2.chess.game.player.Player;
import hw2.agents.heuristics.DefaultHeuristics.DefensiveHeuristics;
import hw2.agents.heuristics.DefaultHeuristics.OffensiveHeuristics;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.PromotePawnMove;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.PlayerType;
import hw2.chess.search.DFSTreeNode;
import hw2.chess.utils.Coordinate;

public class CustomMoveOrderer
{
	
	public synchronized static boolean isInCheck(DFSTreeNode node)
	{
		
		Player currentPlayer = node.getGame().getCurrentPlayer();

		Piece theirKing = node.getGame().getBoard().getPieces(node.getGame().getOtherPlayer(currentPlayer), PieceType.KING).iterator().next(); // will always have a piece

		// get all CaptureMoves from the opposite player
//		for(Move move : this.getAllCaptureMoves(this.getOtherPlayer(player)))
//		{
//			Coordinate tgtPosition = this.getCurrentPosition(((CaptureMove)move).getTargetPlayer(),
//					((CaptureMove)move).getTargetPieceID());
//			if(tgtPosition.equals(ourKing))
//			{
//				return true;
//			}
//		}

		// see if any enemy piece can capture our king
		for(Piece ourPiece : node.getGame().getBoard().getPieces(currentPlayer))
		{
			for(Move captureMove : ourPiece.getAllCaptureMoves(node.getGame()))
			{
				if(((CaptureMove)captureMove).getTargetPieceID() == theirKing.getPieceID() &&
						((CaptureMove)captureMove).getTargetPlayer() == node.getGame().getOtherPlayer(currentPlayer))
				{
					return true;
				}
			}
		}
		return false;
	}

	public synchronized static boolean isInCheckmate(DFSTreeNode node)
	{
		
		Player currentPlayer = node.getGame().getCurrentPlayer();
		// is the current player in checkmate?
		boolean checkmated = true;

		// a player is in checkmate IF for every move the current player can make, they are still in check
		for(Move action : node.getGame().getAllMoves(node.getGame().getOtherPlayer(currentPlayer)))
		{
			checkmated = checkmated && node.getGame().applyMove(action).isInCheck(node.getGame().getOtherPlayer(currentPlayer));
		}

		return checkmated;
	}
	
	
	public static List<DFSTreeNode> order(List<DFSTreeNode> nodes)
	
	
	{
		
		// by default get the CaptureMoves first
		//List<DFSTreeNode> captureNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> otherNodes = new LinkedList<DFSTreeNode>();
		
		List<DFSTreeNode> nodesList = new LinkedList<DFSTreeNode>();

		for(DFSTreeNode node : nodes)
		{			
			if(node.getMove() != null)
			{	
				if(isInCheckmate(node)) {
					nodesList.add(node);
					
				} else if(isInCheck(node)) {
					nodesList.add(node);
					
				} else {
					switch(node.getMove().getType())
					{
					case CASTLEMOVE:
						nodesList.add(node);
						break;
					case CAPTUREMOVE:
						nodesList.add(node);
						break;
					case PROMOTEPAWNMOVE:
						nodesList.add(node);
						break;
					default:
						otherNodes.add(node);
						break;
					}
				}
			} else
			{
				otherNodes.add(node);
			}
		}

		// captureNodes.addAll(otherNodes);
		nodesList.addAll(otherNodes);
		return nodesList;
	}


	/**
	 * TODO: implement me!
	 * This method should perform move ordering. Remember, move ordering is how alpha-beta pruning gets part of its power from.
	 * You want to see nodes which are beneficial FIRST so you can prune as much as possible during the search (i.e. be faster)
	 * @param nodes. The nodes to order (these are children of a DFSTreeNode) that we are about to consider in the search.
	 * @return The ordered nodes.
	 */

}
