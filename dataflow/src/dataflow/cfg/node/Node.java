package dataflow.cfg.node;

/*>>>
import checkers.nullness.quals.Nullable;
*/

import dataflow.cfg.CFGBuilder;
import dataflow.cfg.block.Block;

import java.util.Collection;
import java.util.LinkedList;

import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.Tree;

/**
 * A node in the abstract representation used for Java code inside a basic
 * block.
 *
 * <p>
 *
 * The following invariants hold:
 *
 * <pre>
 * block == null || block instanceof RegularBlock || block instanceof ExceptionBlock
 * block instanceof RegularBlock ==&gt; block.getContents().contains(this)
 * block instanceof ExceptionBlock ==&gt; block.getNode() == this
 * block == null &lt;==&gt; "This object represents a parameter of the method."
 * </pre>
 *
 * <pre>
 * type != null
 * tree != null ==&gt; node.getType() == InternalUtils.typeOf(node.getTree())
 * </pre>
 *
 * @author Stefan Heule
 *
 */
public abstract class Node {

    /**
     * The basic block this node belongs to (see invariant about this field
     * above).
     */
    protected /*@Nullable*/ Block block;

    /**
     * Is this node an l-value?
     */
    protected boolean lvalue = false;

    /**
     * The assignment context of this node. See {@link AssignmentContext}.
     */
    protected /*@Nullable*/ AssignmentContext assignmentContext;

    /**
     * Does this node represent a tree that appears in the source code (true)
     * or one that the CFG builder added while desugaring (false).
     */
    protected boolean inSource = true;

    /**
     * The type of this node. For {@link Node}s with {@link Tree}s, this type is
     * the type of the {@link Tree}. Otherwise, it is the type is set by the
     * {@link CFGBuilder}.
     */
    protected final TypeMirror type;

    public Node(TypeMirror type) {
        assert type != null;
        this.type = type;
    }

    /**
     * @return The basic block this node belongs to (or {@code null} if it
     *         represents the parameter of a method).
     */
    public /*@Nullable*/ Block getBlock() {
        return block;
    }

    /** Set the basic block this node belongs to. */
    public void setBlock(Block b) {
        block = b;
    }

    /**
     * Returns the {@link Tree} in the abstract syntax tree, or
     * <code>null</code> if no corresponding tree exists. For instance, this is
     * the case for an {@link ImplicitThisLiteralNode}.
     *
     * @return The corresponding {@link Tree} or <code>null</code>.
     */
    abstract public /*@Nullable*/ Tree getTree();

    /**
     * Returns a {@link TypeMirror} representing the type of a {@link Node} A
     * {@link Node} will always have a type even when it has no {@link Tree}.
     *
     * @return A {@link TypeMirror} representing the type of this {@link Node}.
     */
    public TypeMirror getType() {
        return type;
    }

    /**
     * Accept method of the visitor pattern
     *
     * @param <R>
     *            Result type of the operation.
     * @param <P>
     *            Parameter type.
     * @param visitor
     *            The visitor to be applied to this node.
     * @param p
     *            The parameter for this operation.
     */
    public abstract <R, P> R accept(NodeVisitor<R, P> visitor, P p);

    public boolean isLValue() {
        return lvalue;
    }

    /**
     * Make this node an l-value.
     */
    public void setLValue() {
        lvalue = true;
    }

    public boolean getInSource() {
        return inSource;
    }

    public void setInSource(boolean inSrc) {
        inSource = inSrc;
    }

    public AssignmentContext getAssignmentContext() {
        return assignmentContext;
    }

    public void setAssignmentContext(AssignmentContext assignmentContext) {
        this.assignmentContext = assignmentContext;
    }

    /**
     * @return A collection containing all of the operand {@link Node}s of this
     *         {@link Node}.
     */
    public abstract Collection<Node> getOperands();

    /**
     * @return A collection containing all of the operand {@link Node}s of this
     *         {@link Node}, as well as (transitively) the operands of its
     *         operands.
     */
    public Collection<Node> getTransitiveOperands() {
        LinkedList<Node> operands = new LinkedList<>(getOperands());
        LinkedList<Node> transitiveOperands = new LinkedList<>();
        while (!operands.isEmpty()) {
            Node next = operands.removeFirst();
            operands.addAll(next.getOperands());
            transitiveOperands.add(next);
        }
        return transitiveOperands;
    }

}
