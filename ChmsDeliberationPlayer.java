package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * This is my first deliberation player
 * @date 03.11.2013
 * @author chms (http://forallx.ru)
 */


public class ChmsDeliberationPlayer extends SampleGamer {

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {
		long start = System.currentTimeMillis();
		
		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		
		Move selection = getBestMove(moves);
		
		long stop = System.currentTimeMillis();
		
		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	/***
	 * Search game tree for a best move and return it
	 * @param moves Current action
	 * @return {Move}
	 * @throws TransitionDefinitionException 
	 * @throws MoveDefinitionException 
	 * @throws GoalDefinitionException 
	 */
	private Move getBestMove(List<Move> moves) throws TransitionDefinitionException, GoalDefinitionException, MoveDefinitionException {
		Move bestMove = moves.get(0); // assume that best move is first
		Integer score = 0;
		for(Move move : moves){
			List<Move> lm = new ArrayList<Move>(); 
			lm.add(move); // this thing is array of moves, but because we play single player game it have only one element 
			MachineState nextState = getStateMachine().getNextState(getCurrentState(), lm);
			Integer result = getMaxScore(nextState);
			if(result==100){
				return move; 
			}else if(result>score){
				// if we find move with score greater than current bestMove
				// then we set it to our current choice
				score=result;
				bestMove=move;
			}
		}
		return bestMove;
	}

	/**
	 * Search child nodes (moves) of given state
	 * If state is terminal return it's goal value
	 * @param state
	 * @return
	 * @throws GoalDefinitionException
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 */
	private Integer getMaxScore(MachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if(getStateMachine().isTerminal(state)){
			return getStateMachine().getGoal(state, getRole());
		}else{
			List<Move> actions = getStateMachine().getLegalMoves(state, getRole());
			Integer score=0;
			for(Move m : actions){
				List<Move> lm = new ArrayList<Move>();
				lm.add(m); // this thing is array of moves, but because we play single player game it have only one element
				MachineState nextState = getStateMachine().getNextState(state, lm);
				Integer result = getMaxScore(nextState);
				if(result==100){
					return 100; 
				}else if(result>score){
					score=result; 
				}
			}
			return score;
		}
	}

}
