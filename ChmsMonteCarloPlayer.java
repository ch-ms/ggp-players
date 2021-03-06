package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * This is my first Monte-Carlo player
 * @date 03.11.2013
 * @author chms (http://forallx.ru)
 */


public class ChmsMonteCarloPlayer extends SampleGamer {
	
	// Roles of players
	private Role opponent;
	private Role me;
	private static final Integer LIMIT = 1; // limit of search depth
	private static final Integer COUNT = 3; // max count of depth charges
	private long    finishBy = 0; // time then calculations need to be finished

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {
		long start = System.currentTimeMillis();
		finishBy = timeout - 3000;
		
		
		// Get roles of my player and opponent player
		me = getRole();
		opponent = me; // for single player games
		for(final Role role : getStateMachine().getRoles()){
			if(!role.equals(me)){
				opponent=role;
			}
		}

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
			Integer result = getMinScore(move, getCurrentState(), 0);
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
	 * @return
	 * @throws MoveDefinitionException 
	 * @throws TransitionDefinitionException 
	 * @throws GoalDefinitionException 
	 */
	private Integer getMinScore(Move action, MachineState state, Integer level) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		List<Move> moves = getStateMachine().getLegalMoves(state, opponent);
		Integer score = 100;
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
			Integer result = getMaxScore(newState, level+1);
			if(result<score){ score=result; }
		}
		return score;
	}

	/***
	 * Search max node for score
	 * @param state
	 * @param level Level of current search depth
	 * @return Score
	 * @throws GoalDefinitionException 
	 * @throws MoveDefinitionException 
	 * @throws TransitionDefinitionException 
	 */
	private Integer getMaxScore(MachineState state, Integer level) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if(getStateMachine().isTerminal(state)){
			return getStateMachine().getGoal(state, me);
		}else if (level>LIMIT){
			return getScoreByMontecarlo(state);
		}else{
			List<Move> moves = getStateMachine().getLegalMoves(state, me);
			Integer score = 0;
			for(Move move : moves){
				Integer result = getMinScore(move, state, level);
				if(result>score){ score = result; }
			}
			return score;
		}
	}
	
	
	
	/**
	 * Get score for state using Monte-Carlo method
	 * @param state
	 * @return score
	 * @throws MoveDefinitionException 
	 * @throws GoalDefinitionException 
	 * @throws TransitionDefinitionException 
	 */
	private Integer getScoreByMontecarlo(MachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		Integer total = 0;
		for(Integer i=0;i<COUNT;i++){
			total += depthCharge(state);
		}
		return total/COUNT;
	}

	/**
	 * Perform depth charge from state
	 * @param state
	 * @return score
	 * @throws GoalDefinitionException
	 * @throws MoveDefinitionException 
	 * @throws TransitionDefinitionException 
	 */
	private Integer depthCharge(MachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if(getStateMachine().isTerminal(state)){
			return getStateMachine().getGoal(state, me);
		}else if(System.currentTimeMillis() > finishBy){
			return 0;
		}else{
			List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(state);
			List<Move> randomJointMove = jointMoves.get(new Random().nextInt(jointMoves.size()));
			MachineState newState = getStateMachine().getNextState(state, randomJointMove);
			return depthCharge(newState);
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
