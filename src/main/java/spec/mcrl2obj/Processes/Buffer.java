/**
 * 
 */
package spec.mcrl2obj.Processes;

import java.util.Set;

import org.javatuples.Pair;

import com.google.common.collect.Sets;

import rpstTest.Utils;
import sort.Data;
import sort.ISort;
import sort.Placeholder;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.MCRL2Utils;
import spec.mcrl2obj.Operator;

/**
 * This is the Buffer class. There are two types of buffers, no blocking one:
 * B(b1...bn:Data) = sum e1,...,en:Data.ib(e1,...,en).B(e1,...,en) +
 * ob(b1,...,bn).B(eps1,...,epsn) blocking one: B(b1...bn:Data) = sum
 * e1,...,en:Data.ib(e1,...,en).B(e1,...,en) + IF(!empty(b1) AND .... AND
 * !empty(bn)) THEN ob(b1,...,bn).B(eps1,...,epsn)
 * 
 * @author S. Belluccini
 *
 */
public class Buffer extends AbstractProcess {

	private Process buffer;
	private Action inputAction;
	private Action outputAction;
	private String attribute;
	public static final String BLOCK = "block";
	public static final String NOBLOCK = "noblock";

	public Buffer(int nlenght, String s) {
		this.attribute = s;
		buffer = new Process(Operator.PLUS);
		ISort[] epsArr = initPParamAndEpsArray(nlenght);
		addInputAction(nlenght);
		ISort[] param = this.getParameters();
		this.outputAction = MCRL2.getOutputAction(param);
		Process truep = new Process(Operator.DOT, this.outputAction, new Action(getId(), epsArr));
		if (s.equals(NOBLOCK)) {
			buffer.addChild(truep);
		} else {
			String noempty = "!" + MCRL2Utils.printf(MCRL2Utils.emptyf, param[0].toString());
			for (int i = 1; i < nlenght; i++) {
				noempty += "&& !" + MCRL2Utils.printf(MCRL2Utils.emptyf, param[i].toString());
			}
			buffer.addChild(new IfProcess(noempty, truep));
		}
	}

	/**
	 * Adds the action as input action
	 * 
	 * @param inputAction  the input action
	 * @param placeholders the parameters in input to the action
	 */
	private void addInputAction(int nlenght) {
		SUMProcess sumi = new SUMProcess(Data.nameSort(), nlenght);
		this.inputAction = sumi.getAction();
		buffer.addChild(new Process(Operator.DOT, sumi, new Action(getId(), sumi.getParameters())));
	}

	private ISort[] initPParamAndEpsArray(int nlenght) {
		Placeholder[] placeholdersOutput = new Placeholder[nlenght];
		ISort[] epsArr = new ISort[nlenght];
		for (int i = 0; i < nlenght; i++) {
			placeholdersOutput[i] = new Placeholder(Data.nameSort());
			if (attribute.equals(NOBLOCK))
				epsArr[i] = Data.nullvar();
			else
				epsArr[i] = Data.eps();
		}
		addParameters(placeholdersOutput);
		return epsArr;
	}

	/**
	 * Returns the input action of this buffer, i.e. i(d)
	 * 
	 * @return the input action of this buffer
	 */
	public Action getInputAction() {
		return this.inputAction;
	}

	/**
	 * Returns the output action of this buffer, i.e. o(d)
	 * 
	 * @return the output action of this buffer, i.e. o(d)
	 */
	public Action getOutputAction() {
		return this.outputAction;
	}

	/**
	 * Prints something with this structure: B(b1...bn:Data) = P + P
	 */
	@Override
	public Pair<String, Set<String>> toPrint() {
		String[] param = new String[this.getParameters().length];
		int i = 0;
		for (ISort sort : this.getParameters()) {
			param[i] = sort.toString() + ":" + sort.getNameSort();
			i++;
		}
		String s = this.getId() + "(" + Utils.organizeParametersAsString(param) + ") = " + buffer.toString();
		return Pair.with("", Sets.newHashSet(s));
	}

	public String toString() {
		Pair<String, Set<String>> print = this.toPrint();
		String string = "";
		for (String s : print.getValue1())
			string += s + ";\n";
		return string;
	}

	public String inizializeBuffer() {
		String st = "";
		for (int i = 0; i < this.getParameters().length; i++) {
			if (!st.isEmpty())
				st += ",";
			if (attribute.equals(NOBLOCK))
				st += Data.nullvar().getId();
			else
				st += Data.eps().getId();
		}
		return getId() + "(" + st + ")";
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
