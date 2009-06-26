package edu.ohio_state.khatchad.refactoring.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class Worklist extends LinkedHashSet implements Iterator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2876031326847373317L;

	private static boolean isValidTypeSignature(String sig) {
		if (Signature.getTypeSignatureKind(sig) == Signature.BASE_TYPE_SIGNATURE)
			return true;
		else if (Signature.getTypeSignatureKind(sig) == Signature.ARRAY_TYPE_SIGNATURE)
			return isValidTypeSignature(Signature.getElementType(sig));
		else
			return false;
	}

	private final Collection computationForest = new LinkedHashSet();

	private ValuedComputationNode currentNode;

	private final Map elemToNode = new HashMap();

	public boolean add(Object o) {
		try {
			this.sanityCheck(o);
		} catch (final JavaModelException e) {
			throw new RuntimeException(e);
		}

		final ValuedComputationNode elemNode = (ValuedComputationNode) this.elemToNode
				.get(o);

		if (elemNode != null) // its been seen before.
		{
			// get the roots.
			final ComputationNode elemNodeRoot = elemNode.getRoot();
			final ComputationNode currNodeRoot = this.currentNode.getRoot();

			if (elemNodeRoot != currNodeRoot) {
				// union the trees.
				final ComputationNode unionNode = this.union(elemNodeRoot,
						currNodeRoot);

				// remove the old trees from the forest.
				this.computationForest.remove(elemNodeRoot);
				this.computationForest.remove(currNodeRoot);

				// add the new tree to the forest.
				this.computationForest.add(unionNode);
			}

			return false;
		}

		else // it has not been seen before.
		{
			final ValuedComputationNode node = new ValuedComputationNode(
					(IJavaElement) o);
			this.elemToNode.put(o, node);
			if (this.currentNode == null)
				// seed the comp forest.
				this.computationForest.add(node);
			else
				// attach the new node.
				this.currentNode.makeParent(node);
			return super.add(o);
		}
	}

	public boolean addAll(Collection c) {
		boolean changed = false;
		for (final Iterator it = c.iterator(); it.hasNext();) {
			final Object e = it.next();
			changed |= this.add(e);
		}
		return changed;
	}

	public Collection getComputationForest() {
		return this.computationForest;
	}

	public Collection getCurrentComputationTreeElements() {
		// find the tree in the forest that contains the current node.
		final ComputationNode root = this.currentNode.getRoot();
		return root.getComputationTreeElements();
	}

	public ValuedComputationNode getCurrentNode() {
		return this.currentNode;
	}

	public Set getSeen() {
		return this.elemToNode.keySet();
	}

	public boolean hasNext() {
		return !this.isEmpty();
	}

	public Object next() {
		final Iterator it = this.iterator();
		final Object ret = it.next();
		this.currentNode = (ValuedComputationNode) this.elemToNode.get(ret);
		it.remove();
		return ret;
	}

	public void remove() {
		this.iterator().remove();
	}

	private void sanityCheck(Object e) throws JavaModelException {
		final IJavaElement o = (IJavaElement) e;
		if (o.isReadOnly())
			throw new IllegalArgumentException("Illegal worklist element: " + o);

		switch (o.getElementType()) {
		case IJavaElement.LOCAL_VARIABLE: {
			final ILocalVariable lv = (ILocalVariable) o;
			final String sig = lv.getTypeSignature();
			if (!isValidTypeSignature(sig))
				throw new IllegalArgumentException("Illegal worklist element: "
						+ o);
			break;
		}

		case IJavaElement.FIELD: {
			final IField f = (IField) o;
			final String sig = f.getTypeSignature();
			if (!isValidTypeSignature(sig))
				throw new IllegalArgumentException("Illegal worklist element: "
						+ o);
			break;
		}

		case IJavaElement.METHOD: {
			final IMethod m = (IMethod) o;
			final String retType = m.getReturnType();
			if (!isValidTypeSignature(retType))
				throw new IllegalArgumentException("Illegal worklist element: "
						+ o);

			break;
		}

		default: {
			throw new IllegalArgumentException("Illegal worklist element: " + o);
		}
		}
	}

	private ComputationNode union(ComputationNode root1, ComputationNode root2) {
		final ComputationNode ret = new UnionComputationNode();
		ret.makeParent(root1);
		ret.makeParent(root2);
		return ret;
	}
}