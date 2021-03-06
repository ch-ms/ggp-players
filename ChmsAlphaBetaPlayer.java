package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * This is my first minmax player with alpha-beta pruning
 * @date 03.11.2013
 * @author chms (http://forallx.ru)
 */


public class ChmsAlphaBetaPlayer extends SampleGamer {
	
	// Roles of players
	private Role opponent;
	private Role me;

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {
		long start = System.currentTimeMillis();
		
		// Get roles of my player and opponent player
		me = getRole();
		for(final Role role : getStateMachine().getRoles()){
			if(!role.equals(me)){
				opponent=role;
			}
		}
		
		System.out.println("===");

		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), me);
		
		Move selection = getBestMove(moves);
		
		long stop = System.currentTimeMillis();
		
		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	/***
	 * Search game tree for a best move and return it
	 * @param moves
	 * @return {Move}
	 * @throws GoalDefinitionException 
	 * @throws TransitionDefinitionException 
	 * @throws MoveDefinitionException 
	 */
	private Move getBestMove(List<Move> moves) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		Move bestMove = moves.get(0); // assume that best move is first
		Integer score = 0;
		for(Move move : moves){
			Integer result = getMinScore(move, getCurrentState(), 0, 100);
			outMoveScore(move, result);
			if(result>score){
				// if we find move with score greater than current bestMove
				// then we set it to our current choice
				score=result;
				bestMove=move;
			}
		}
		return bestMove;
	}

	/***
	 * Search min node for score
	 * @param action
	 * @param state 
	 * @param alpha
	 * @param beta
	 * @return
	 * @throws MoveDefinitionException 
	 * @throws TransitionDefinitionException 
	 * @throws GoalDefinitionException 
	 */
	private Integer getMinScore(Move action, MachineState state, Integer alpha, Integer beta) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		List<Move> moves = getStateMachine().getLegalMoves(state, opponent);
		for(Move move : moves){
			List<Move> tryMove = new ArrayList<Move>();
			// The action sequence is important in tryMove list
			for(Map.Entry<Role, Integer> entry : getStateMachine().getRoleIndices().entrySet()){
				if(entry.getKey().equals(me)){
					tryMove.add(action);
				}else{
					tryMove.add(move);
				}
			}
			
			MachineState newState = getStateMachine().getNextState(state, tryMove);
			Integer maxval = getMaxScore(newState, alpha, beta);
			beta = Math.min(beta, maxval);
			if(beta<=alpha){ return alpha; }
		}
		return beta;
	}

	/***
	 * Search max node for score
	 * @param state
	 * @param alpha
	 * @param beta
	 * @return
	 * @throws GoalDefinitionException 
	 * @throws MoveDefinitionException 
	 * @throws TransitionDefinitionException 
	 */
	private Integer getMaxScore(MachineState state, Integer alpha, Integer beta) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if(getStateMachine().isTerminal(state)){
			return getStateMachine().getGoal(state, me);
		}else{
			List<Move> moves = getStateMachine().getLegalMoves(state, me);
			for(Move move : moves){
				Integer minval = getMinScore(move, state, alpha, beta);
				alpha = Math.max(alpha, minval);
				if(alpha>=beta){ return beta; }
			}
			return alpha;
		}
	}
	
	/**
	 * Log action and it's score to console
	 * @param move
	 * @param result
	 */
	private void outMoveScore(Move move, Integer result) {
		System.out.print("move is ");
		System.out.print(move);
		System.out.print("result is" );
		System.out.println(result);
	}
}
