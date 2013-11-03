package org.ggp.base.player.gamer.statemachine.sample;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/** 
 * This is my first legal player
 * @date   26.10.2013
 * @author chms (http://forallx.ru)
 *
 */

public final class ChmsLegalPlayer extends SampleGamer {

	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		// Get current time and save it as start time
		long start = System.currentTimeMillis();
		
		/**
		 * Запихиваем в память список легальных действий из 
		 * текущего состояния. Задача каждого stateMachineSelectMove()
		 * вернуть одно из этих действий. Выбор действия для игры это 
		 * задача УИП
		 */
		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		
		// Legal player is simple: it chooses first legal action
		Move selection = moves.get(0);
		
		// Get the end time
		// Important that stop < timeout
		long stop = System.currentTimeMillis();
		
		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}
}
