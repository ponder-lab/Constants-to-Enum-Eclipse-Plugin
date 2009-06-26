package edu.ohio_state.khatchad.refactoring.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import edu.ohio_state.khatchad.refactoring.exceptions.DefinitelyNotEnumerizableException;
import edu.ohio_state.khatchad.refactoring.exceptions.NonEnumerizableException;

public class EnumerizationComputer {
	/**
	 * @param candidateSets
	 * @return
	 * @throws JavaModelException
	 */
	public static Collection filterSetsAccordingToMemberConstraints(
			final Collection candidateSets) throws JavaModelException {

		final Collection ret = new LinkedHashSet(candidateSets);

		// make sure the values are distinct.
		final Collection distinctEnumerizableElementSets = Util
				.getDistinctSets(candidateSets);
		ret.retainAll(distinctEnumerizableElementSets);

		// make sure the fields have a consistent visibility.
		final Collection consistentEnumerizableElementSets = Util
				.getConsistentlyVisibleSets(candidateSets);
		ret.retainAll(consistentEnumerizableElementSets);

		// make sure the fields are named uniquely.
		final Collection uniquelyNamedEnumerizableElementSets = Util
				.getUniquelyNamedSets(candidateSets);
		ret.retainAll(uniquelyNamedEnumerizableElementSets);

		return ret;
	}

	private final Collection constFields;
	private final Collection defNotEnumConstants = new LinkedHashSet();

	private final Map elemToLegalInfixExpressionSourceRangeMap = new LinkedHashMap();

	private Collection enumerizationForest;

	private final IProgressMonitor monitor;

	private final Collection nonEnumerizableList = new LinkedHashSet();

	private final IJavaSearchScope scope;

	private final SearchEngine searchEngine = new SearchEngine();

	private final Worklist wl = new Worklist();

	/**
	 * @param constFields
	 * @param scope
	 * @param monitor
	 * @throws JavaModelException
	 */
	public EnumerizationComputer(Collection constFields,
			IJavaSearchScope scope, IProgressMonitor monitor)
			throws JavaModelException {
		this.constFields = constFields;
		this.scope = scope;
		this.monitor = monitor;
	}

	public void compute() throws CoreException {
		this.reset();
		this.wl.addAll(this.constFields);
		while (this.wl.hasNext()) {
			final IJavaElement je = (IJavaElement) this.wl.next();
			final SearchPattern pattern = SearchPattern.createPattern(je,
					IJavaSearchConstants.ALL_OCCURRENCES,
					SearchPattern.R_EXACT_MATCH);
			final SearchRequestor requestor = new SearchRequestor() {
				public void acceptSearchMatch(SearchMatch match)
						throws CoreException {
					if (match.getAccuracy() == SearchMatch.A_ACCURATE
							&& !match.isInsideDocComment()) {
						ASTNode node = Util.getExactASTNode(match,
								EnumerizationComputer.this.monitor);
						ASTNodeProcessor processor = new ASTNodeProcessor(node,
								EnumerizationComputer.this.constFields,
								EnumerizationComputer.this.scope,
								EnumerizationComputer.this.monitor);
						processor.process();
						EnumerizationComputer.this.wl.addAll(processor
								.getFound());
						Collection infixCol = (Collection) EnumerizationComputer.this.elemToLegalInfixExpressionSourceRangeMap
								.get(je);
						if (infixCol == null)
							EnumerizationComputer.this.elemToLegalInfixExpressionSourceRangeMap
									.put(
											je,
											processor
													.getLegalEncounteredInfixExpressionSourceLocations());
						else
							infixCol
									.addAll(processor
											.getLegalEncounteredInfixExpressionSourceLocations());
					}
				}
			};

			try {
				this.searchEngine.search(pattern,
						new SearchParticipant[] { SearchEngine
								.getDefaultSearchParticipant() }, this.scope,
						requestor, new SubProgressMonitor(this.monitor, 1,
								SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));

				// Work around for bug 164121. Force match for formal
				// parameters.
				if (je.getElementType() == IJavaElement.LOCAL_VARIABLE) {
					final ISourceRange isr = ((ILocalVariable) je)
							.getNameRange();
					final SearchMatch match = new SearchMatch(je,
							SearchMatch.A_ACCURATE, isr.getOffset(), isr
									.getLength(), SearchEngine
									.getDefaultSearchParticipant(), je
									.getResource());
					requestor.acceptSearchMatch(match);
				}
			} catch (final DefinitelyNotEnumerizableException E) {
				this.defNotEnumConstants.addAll(this.wl
						.getCurrentComputationTreeElements());
				this.nonEnumerizableList.addAll(this.wl
						.getCurrentComputationTreeElements());
				this.wl.removeAll(this.nonEnumerizableList);
				for (final Iterator it = this.wl
						.getCurrentComputationTreeElements().iterator(); it
						.hasNext();) {
					final IJavaElement elem = (IJavaElement) it.next();
					this.elemToLegalInfixExpressionSourceRangeMap.remove(elem);
				}
				continue;
			}

			catch (final NonEnumerizableException E) {
				this.nonEnumerizableList.addAll(this.wl
						.getCurrentComputationTreeElements());
				this.wl.removeAll(this.nonEnumerizableList);
				for (final Iterator it = this.wl
						.getCurrentComputationTreeElements().iterator(); it
						.hasNext();) {
					final IJavaElement elem = (IJavaElement) it.next();
					this.elemToLegalInfixExpressionSourceRangeMap.remove(elem);
				}
				continue;
			}
		}

		this.defNotEnumConstants.retainAll(this.constFields);
		final Collection computationForest = Util.trimForest(this.wl
				.getComputationForest(), this.nonEnumerizableList);

		final Collection candidateSets = Util
				.getElementForest(computationForest);

		this.enumerizationForest = filterSetsAccordingToMemberConstraints(candidateSets);
	}

	/**
	 * @return the defNotEnumConstants
	 */
	public Collection getDefNotEnumConstants() {
		return this.defNotEnumConstants;
	}

	public Map getElemToLegalInfixExpressionSourceRangeMap() {
		return this.elemToLegalInfixExpressionSourceRangeMap;
	}

	/**
	 * @return the enumerizationForest
	 */
	public Collection getEnumerizationForest() {
		return this.enumerizationForest;
	}

	public Collection getLegalEncounteredInfixExpressionSourceRanges() {
		final Collection ret = new LinkedHashSet();
		for (final Iterator cit = this.elemToLegalInfixExpressionSourceRangeMap
				.values().iterator(); cit.hasNext();) {
			final Collection col = (Collection) cit.next();
			ret.addAll(col);
		}
		return ret;
	}

	/**
	 * @return the nonEnumerizableList
	 */
	public Collection getNonEnumerizableList() {
		return this.nonEnumerizableList;
	}

	private void reset() {
		this.wl.clear();
		this.nonEnumerizableList.clear();
		this.defNotEnumConstants.clear();
	}
}