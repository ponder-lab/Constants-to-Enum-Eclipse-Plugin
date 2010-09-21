/**
 * 
 */
package edu.ohio_state.khatchad.refactoring.core;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;

import edu.ohio_state.khatchad.refactoring.Messages;

/**
 * @author raffi
 * 
 */
public class EnumConstantComparator implements Comparator {

	private Map newEnumConstantToOldConstantFieldMap;

	public EnumConstantComparator(Map map) {
		this.newEnumConstantToOldConstantFieldMap = map;
	}

	public int compare(Object o1, Object o2) {
		if (!(o2 instanceof EnumConstantDeclaration && o2 instanceof EnumConstantDeclaration)) {
			String message = Messages.EnumConstantComparator_BothObjectsMustMatch;
			throw new IllegalArgumentException(MessageFormat.format(message, new Object[]{EnumConstantDeclaration.class.getName()}));
		}

		final IField f1 = (IField) this.newEnumConstantToOldConstantFieldMap.get(o1);
		final IField f2 = (IField) this.newEnumConstantToOldConstantFieldMap.get(o2);

		Object v1 = null;
		Object v2 = null;

		try {
			v1 = f1.getConstant();
			v2 = f2.getConstant();
		} catch (final JavaModelException E) {
			final ClassCastException ce = new ClassCastException(
					Messages.EnumConstantComparator_CannotCompare);
			ce.initCause(E);
			throw ce;
		}

		if (v1 == null || v2 == null) // no constant value.
			throw new ClassCastException(
					Messages.EnumConstantComparator_CannotCompareWithoutPrimitives);
		else
			return ((Comparable) v1).compareTo(v2);
	}
}