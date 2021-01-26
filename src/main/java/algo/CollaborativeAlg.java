package algo;

import java.util.Collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.javatuples.Quartet;
import org.jbpt.pm.DataNode;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.IFlowNode;
import org.jbpt.pm.NonFlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.Task;

import com.google.common.collect.Sets;

import io.pet.AbstractDataPET;
import pcrrlalgoelement.Parout;
import sort.Data;
import sort.ISort;

import sort.Placeholder;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.Processes.Buffer;

import spec.mcrl2obj.Processes.ParticipantProcess;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * This is the Collaborative algorithm class. It computes through two steps a
 * mCRL2 object from a bpmn collaboration model. The first step is the
 * control-flow transformation. The second step is the data-object and message
 * flow transformation. The results of this process is a mCRL2 object.
 * 
 * @see #getSpec() returns the mcrl2 object that is generated from the bpmn
 *      models
 * 
 * @author S. Belluccini
 *
 */
public class CollaborativeAlg extends AbstractTranslationAlg {

	private Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmn;
	private Set<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>> internalCommList;
	private static final FlowNode epsilon = new Task();
	private Set<Pair<FlowNode, FlowNode>> messages;
	private Map<String, Placeholder> mapPNPlaceholders = new HashMap<String, Placeholder>();
	private MCRL2 mcrl2;

	/**
	 * Constructor for the Collaborative Algorithm class.
	 * 
	 * @param pair of bpmn sets and messages
	 */
	public CollaborativeAlg(Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> pair) {
		this.bpmn = pair.getLeft();
		this.messages = pair.getRight();
	}

	/**
	 * Computes the data exchanges among the tasks and parties in this collaboration
	 * model.
	 */
	private void analyzeData() {
		generateInternalCommunicationlist();
		for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple : internalCommList) {
			// If the left side is epsilon than the data are prior knowledge ofthe task
			// System.out.println(triple.toString());
			if (triple.getLeft().contains(epsilon)) {
				TaskProcess R = mcrl2.getActionTask(triple.getRight().getId());
				triple.getMiddle().forEach(d -> R.addInputDataToAction(d));
				continue;
			}

			Placeholder[] parameters = new Placeholder[triple.getMiddle().size()];
			int j = 0;
			for (DataNode d : triple.getMiddle()) {
				parameters[j] = mapPNPlaceholders.get(d.getName());
				j++;
			}

			Action send = null;
			ISort[] paramtosend = null;
			Pair<Set<DataNode>, Set<Placeholder>> pair_paramtosend = null;
			for (IFlowNode left : triple.getLeft()) {
				TaskProcess S = mcrl2.getActionTask(left.getId());
				// If all the structure to send this message/data already exists we just need to
				// add the output action to the new taskprocess
				if (send != null) {
					S.addOutputAction(send);
					S.addOutputDataToAction(paramtosend);
					continue;
				}
				// Otherwise, if this data/messagge is not sent to anyone we just update the
				// action parameters
				if (triple.getRight().equals(epsilon)) {
					S.addOutputDataToAction(triple.getMiddle(), Sets.newHashSet());
					continue;
				}
				TaskProcess R = mcrl2.getActionTask(triple.getRight().getId());
				Buffer buffer = null;
				// System.out.println(triple.toString());
				for (ParticipantProcess p : mcrl2.getParcipantProcesses()) {
					if (p.containActionTask(S.getAction()) && p.containActionTask(R.getAction())) {
						buffer = new Buffer(parameters.length, Buffer.NOBLOCK);
						break;
					} else if ((p.containActionTask(S.getAction()) && !p.containActionTask(R.getAction()))
							|| (p.containActionTask(R.getAction()) && p.containActionTask(S.getAction()))) {
						buffer = new Buffer(parameters.length, Buffer.BLOCK);
						break;
					}
				}
				mcrl2.addProcess(buffer);
				pair_paramtosend = createOutputChannel(triple.getMiddle(), left);
				paramtosend = S.addOutputDataToAction(pair_paramtosend.getKey(), pair_paramtosend.getValue());
				send = MCRL2.getOutputAction(paramtosend);
				S.addOutputAction(send);
				Action read = MCRL2.getInputAction(parameters);
				this.mcrl2.addCommunicaitonFunction(createSendReadCommunication(send, buffer.getInputAction()));
				this.mcrl2.addCommunicaitonFunction(createSendReadCommunication(buffer.getOutputAction(), read));
				R.addInputDataToAction(parameters);
				R.addInputAction(read);
			}
		}
	}

	/**
	 * Returns a communication function between the two actions
	 * 
	 * @param a an action
	 * @param b an action
	 * @return a communication function between the two actions
	 */
	private CommunicationFunction createSendReadCommunication(Action a, Action b) {
		Action sendread = new Action(MCRL2.getComFunResult(), a.getParameters());
		mcrl2.addAction(a, b, sendread);
		mcrl2.addAllow(sendread);
		mcrl2.addHide(sendread);
		return new CommunicationFunction(sendread, a, b);
	}

	/**
	 * Returns a pair describing the set of petnodes and placeholders to be sent
	 * 
	 * @param petnodes the set of petnodes
	 * @param node     the current node that we hare checking
	 * @return a pair describing the set of petnodes and placeholders to be sent
	 */
	private Pair<Set<DataNode>, Set<Placeholder>> createOutputChannel(Set<DataNode> petnodes, IFlowNode node) {
		Set<DataNode> pnToSend = new HashSet<DataNode>();
		Set<Placeholder> plToSend = new HashSet<Placeholder>();
		for (DataNode petnode : petnodes) {
			// If d appears for the first time in this node
			if (isitFirst(node, petnode))
				pnToSend.add(petnode);
			else
				plToSend.add(mapPNPlaceholders.get(petnode.getName()));
		}
		return Pair.of(pnToSend, plToSend);
	}

	/**
	 * Checks if this flownode is the one in which the data appears for the first
	 * time or not
	 * 
	 * @param node the flow node
	 * @param data to be checked
	 * @return true if this flownode is the one in which the data appears for the
	 *         first time, false otherwise
	 */
	private boolean isitFirst(IFlowNode node, DataNode data) {
		for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple : internalCommList) {
			// <epsilon,data,node>
			if (hasSameName(data.getName(), triple.getMiddle()) && triple.getRight().equals(node)
					&& triple.getLeft().contains(epsilon)) {
				return true;
				// ! <node', data,node>
			} else if (hasSameName(data.getName(), triple.getMiddle()) && triple.getLeft().contains(node)
					&& !existsATripleSending(node, data))
				return true;
		}
		return false;
	}

	/**
	 * Checks if the input node receives the input data from someone else
	 * 
	 * @param node the input node
	 * @param data a data object
	 * @return true if it exists another node that is sending the data to the input
	 *         node, false otherwise
	 */
	private boolean existsATripleSending(IFlowNode node, DataNode data) {
		for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple : internalCommList) {
			if (hasSameName(data.getName(), triple.getMiddle()) && triple.getRight().equals(node))
				return true;

		}
		return false;
	}

	/**
	 * Checks if one of the datanodes in the set has name equal to the one given as
	 * input
	 * 
	 * @param name  the string
	 * @param nodes a set of datanodes
	 * @return true if one of the datanodes in the set has name equal to the one
	 *         given as input, false otherwise
	 */
	private boolean hasSameName(String name, Set<DataNode> nodes) {
		for (DataNode d : nodes) {
			if (d.getName().equals(name))
				return true;
		}
		return false;
	}

	/**
	 * Returns the set of messages exchanged among participants
	 * 
	 * @param f                   the message flow that is sending the message
	 * @param tmpinternalCommList the list of communication <sender,
	 *                            message/data,receiver>
	 * @return the set of messages exchanged among participants
	 */
	private Set<DataNode> findData(IFlowNode f, Set<Triple<IFlowNode, DataNode, IFlowNode>> tmpinternalCommList) {
		Set<DataNode> set = new HashSet<DataNode>();
		for (Triple<IFlowNode, DataNode, IFlowNode> triple : tmpinternalCommList) {
			if (triple.getLeft().equals(f))
				set.add(triple.getMiddle());
		}
		return set;
	}

	/**
	 * If d1 and d2 in Data have the same name, then is the same data object, that's
	 * why if one has a PET associated then also the other one should have it. If
	 * not the method is copying it.
	 */
	private void reconnectWithPETS() {
		bpmn.forEach(b1 -> {
			bpmn.forEach(b2 -> {
				if (!b1.equals(b2)) {
					for (NonFlowNode f1 : b1.getNonFlowNodes()) {
						for (NonFlowNode f2 : b2.getNonFlowNodes()) {
							if (f1.getName().equals(f2.getName())) {
								if (f1.getTag() != null && f2.getTag() == null) {
									f2.setTag(f1.getTag());
									mcrl2.addPetLabel(((AbstractDataPET) f1.getTag()).getPETLabel());
								}
							}
						}
					}
				}
			});
		});
	}

	/**
	 * Fills the list internalcommlist with the information about sending and
	 * receiving task a their correspondent data
	 */
	private void generateInternalCommunicationlist() {
		epsilon.setTag("epsilon");
		epsilon.setName("epsilon");
		Set<Triple<IFlowNode, DataNode, IFlowNode>> tmpInternalCommList = new HashSet<Triple<IFlowNode, DataNode, IFlowNode>>();
		reconnectWithPETS();
		bpmn.forEach(b -> {
			for (NonFlowNode f : b.getNonFlowNodes()) {
				DataNode petnode = (DataNode) f;
				mcrl2.addDataObjectName(petnode.getName());
				if (!mapPNPlaceholders.containsKey(petnode.getName()))
					mapPNPlaceholders.put(petnode.getName(), new Placeholder(Data.nameSort()));
				Collection<IFlowNode> writingnodes = petnode.getWritingFlowNodes();
				Collection<IFlowNode> readnodes = petnode.getReadingFlowNodes();
				if (!writingnodes.isEmpty() && !readnodes.isEmpty()) {
					writingnodes.forEach(w -> {
						readnodes.forEach(r -> {
							tmpInternalCommList.add(Triple.of(w, petnode, r));
						});
					});
				} else if (writingnodes.isEmpty() && !readnodes.isEmpty()) {
					readnodes.forEach(r -> tmpInternalCommList.add(Triple.of(epsilon, petnode, r)));
				} else if (!writingnodes.isEmpty() && readnodes.isEmpty()) {
					writingnodes.forEach(w -> tmpInternalCommList.add(Triple.of(w, petnode, epsilon)));
				}
			}
		});
		messages.forEach(pair -> {
			Set<DataNode> set = findData(pair.getRight(), tmpInternalCommList);
			for (DataNode d : set) {
				tmpInternalCommList.add(Triple.of(pair.getLeft(), d, pair.getRight()));
			}
		});
		combiningbyData(combiningbyDataReceiver(tmpInternalCommList));
	}

	/**
	 * Unify the triples that have the same sender and receiver in a unique triple
	 * 
	 * @param tmpintermalcommlist the triples of communication
	 */
	private void combiningbyData(Set<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>> tmpintermalcommlist) {
		this.internalCommList = new HashSet<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>>();
		for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple : tmpintermalcommlist) {
			boolean find = false;
			for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> t : this.internalCommList) {
				if (t.getRight().equals(triple.getRight()) && t.getLeft().equals(triple.getLeft())) {
					t.getMiddle().addAll(triple.getMiddle());
					find = true;
					break;
				}
			}
			if (!find)
				this.internalCommList.add(triple);
		}
	}

	/**
	 * Returns a triple of <senders, messages/datas, receiver>
	 * 
	 * @param set containing all the triples with the communication
	 * @return a triple of <senders, message/data, receiver>
	 */
	private Set<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>> combiningbyDataReceiver(
			Set<Triple<IFlowNode, DataNode, IFlowNode>> set) {
		Set<Triple<IFlowNode, DataNode, IFlowNode>> copyset = new HashSet<Triple<IFlowNode, DataNode, IFlowNode>>(set);
		Set<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>> tmpintermalcommlist = new HashSet<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>>();
		for (Triple<IFlowNode, DataNode, IFlowNode> entry : set) {
			DataNode d = entry.getMiddle();
			if (!copyset.contains(entry))
				continue;
			Set<IFlowNode> senderset = new HashSet<IFlowNode>();
			senderset.add(entry.getLeft());
			Set<DataNode> datanodeset = new HashSet<DataNode>();
			datanodeset.add(entry.getMiddle());
			for (Triple<IFlowNode, DataNode, IFlowNode> entry2 : set) {
				if (!entry.equals(entry2) && d.equals(entry2.getMiddle())
						&& entry.getRight().equals(entry2.getRight())) {
					senderset.add(entry2.getLeft());
					copyset.remove(entry2);
				}
			}
			tmpintermalcommlist.add(Triple.of(senderset, datanodeset, entry.getRight()));
		}
		return tmpintermalcommlist;
	}

	/**
	 * {@inheritDoc} Returns a mcrl2 object
	 */
	@Override
	public MCRL2 getSpec() {
		// Set of control-flow
		Set<ParticipantProcess> tmcrl2 = new HashSet<ParticipantProcess>();
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> b : bpmn)
			tmcrl2.add(analyzeControlFlow(b));
		mcrl2 = new MCRL2();
		tmcrl2.forEach(t -> {
			Quartet<ParticipantProcess, Set<CommunicationFunction>, Set<Action>, Set<Action>> quartet = Parout
					.parout(t);
			mcrl2.addParticipantProcess(quartet.getValue0());
			mcrl2.addCommunicationFunction(quartet.getValue1());
			mcrl2.addAllow(quartet.getValue2().toArray(new Action[quartet.getValue2().size()]));
			mcrl2.addAllow(quartet.getValue0().getActionTask());
			mcrl2.addHide(quartet.getValue2().toArray(new Action[quartet.getValue2().size()]));
			mcrl2.addAction(quartet.getValue2().toArray(new Action[quartet.getValue2().size()]));
			mcrl2.addAction(quartet.getValue3().toArray(new Action[quartet.getValue3().size()]));
		});

		// Data-object and message flow transformation step
		analyzeData();
		return mcrl2;
	}

}
