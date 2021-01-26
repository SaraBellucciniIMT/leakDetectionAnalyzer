package pcrrlalgoelement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javatuples.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.LoopProcess;
import spec.mcrl2obj.Processes.Process;

public class Loop extends AbstractParaout {

	@Override
	public AbstractProcess interpreter(Process process) {
		return new LoopProcess(Td(process));
	}

	/**
	 * The Td function applies Td over every block inside inside b and if b has the
	 * parallel operator than this block is transformed in a choice among the
	 * sequence of events inside the block. For more details look at pag 9 of the
	 * paper "PALM: a technique for Process ALgebraic specification Mining.
	 * 
	 * @see #seq(BlockStructure)
	 * 
	 * @param b the block structure
	 * @return the block structure resulting from Td
	 */
	private AbstractProcess Td(AbstractProcess b) {
		if (!b.getClass().equals(Process.class) && !b.getClass().equals(LoopProcess.class)) {
			return b;
		} else if (!((Process) b).getOperator().equals(Operator.PARALLEL)) {
			b = onEveryBlock((Process) b);
		} else if (((Process) b).getOperator().equals(Operator.PARALLEL)) {
			Set<List<AbstractProcess>> blockList = seq(onEveryBlock((Process) b));
			AbstractProcess[] arrC = new AbstractProcess[blockList.size()];
			int j = 0;
			for (List<AbstractProcess> l : blockList) {
				AbstractProcess[] arrS = new AbstractProcess[l.size()];
				for (int i = 0; i < arrS.length; i++)
					arrS[i] = l.get(i);
				AbstractProcess sequence = new Process(Operator.DOT, arrS);
				arrC[j++] = sequence;
			}
			b = new Process(Operator.PLUS, arrC);
		}

		return b;
	}

	/**
	 * Function that applies Td on every block inside this process
	 * 
	 * @param b the process
	 * @return the result of the application of Td over the blocks inside b
	 */
	private AbstractProcess onEveryBlock(Process b) {
		Process p = new Process(b.getOperator());
		for (int i = 0; i < b.getSize(); i++)
			p.addChildAtPosition(i, Td(b.getChildAtPosition(i)));
		return p;
	}

	/**
	 * Method to find all the possible interleaving among the blocks of a block;
	 * It's inspired to the CCS's expansion law.
	 * 
	 * @see #f(BlockStructure)
	 * @param b the abstractprocess
	 * @return
	 */
	private Set<List<AbstractProcess>> seq(AbstractProcess b) {
		Set<List<AbstractProcess>> blockList = new HashSet<List<AbstractProcess>>();
		for (int i = 0; i < ((Process)b).getSize(); i++) {
			Process remainingBlock;
			Set<Pair<AbstractProcess, AbstractProcess>> pair = f( ((Process)b).getChildAtPosition(i));
			for (Pair<AbstractProcess, AbstractProcess> p : pair) {
				remainingBlock =  new Process(((Process)b).getOperator());
				for(int j=0; j<((Process)b).getSize(); j++) {
					if(j!=i)
						remainingBlock.addChild(((Process)b).getChildAtPosition(j));
				}
				if (!p.getValue1().isEmpty())
					remainingBlock.addChildAtPosition(i, p.getValue1());
				if (remainingBlock.isEmpty()) {
					Set<List<AbstractProcess>> partialResult = seq(remainingBlock);
					partialResult.forEach(pR -> {
						pR.add(0, p.getValue0());
					});
					blockList.addAll(partialResult);
				} else {
					List<AbstractProcess> singleList = Lists.newArrayList(p.getValue0());
					  if (remainingBlock.getSize()==1) 
						  singleList.add(remainingBlock.getChildAtPosition(0));
					 
					blockList.add(singleList);
				}
			}
		}
		return blockList;
	}

	/**
	 * This method detect the first action that is executed inside the block and
	 * returns the remaining block after that action its executed; in case of choice
	 * this action is taken for all the blocks inside the choice block
	 * 
	 * @param block
	 * @return
	 */
	private Set<Pair<AbstractProcess, AbstractProcess>> f(AbstractProcess block) {
		Set<Pair<AbstractProcess, AbstractProcess>> set = Sets.newHashSet();
		if (!block.getClass().equals(Process.class)) {
			set.add(new Pair<AbstractProcess, AbstractProcess>(block, new Process()));
		} else if (((Process) block).getOperator().equals(Operator.PLUS)) {
			for (int i = 0; i < ((Process) block).getSize(); i++)
				set.addAll(f(((Process) block).getChildAtPosition(i)));
		} else {
			set = f(((Process) block).getChildAtPosition(0));
			AbstractProcess remainingBlock = ((Process) block).removeChildAtPosition(0);

			if (!remainingBlock.isEmpty()) {
				Set<Pair<AbstractProcess, AbstractProcess>> tmpSet = Sets.newHashSet();
				for (Pair<AbstractProcess, AbstractProcess> p : set) {
					if (!p.getValue1().isEmpty()) {
						tmpSet.add(new Pair<AbstractProcess, AbstractProcess>(p.getValue0(),
								new Process(((Process) block).getOperator(), p.getValue1(), remainingBlock)));
					} else
						tmpSet.add(new Pair<AbstractProcess, AbstractProcess>(p.getValue0(), remainingBlock));
				}
				set = tmpSet;
			}
		}
		return set;

	}

}
