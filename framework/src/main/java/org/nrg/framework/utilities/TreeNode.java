/*
 * framework: org.nrg.framework.utilities.TreeNode
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

public class TreeNode<T> {
    public TreeNode() {

    }

    public TreeNode(final T data) {
        setData(data);
    }

    public TreeNode<T> getParent() {
        return _parent;
    }

    public T getData() {
        return _data;
    }

    public void setData(final T data) {
        _data = data;
    }

    public List<TreeNode<T>> getChildren() {
        return _children;
    }

    public void setChildren(final Collection<TreeNode<T>> children) {
        for (final TreeNode<T> child : _children) {
            child._parent = null;
        }
        _children.clear();
        for (final TreeNode<T> child : children) {
            addChild(child);
        }
    }

    @SafeVarargs
    public final void setChildren(final TreeNode<T>... children) {
        setChildren(Arrays.asList(children));
    }

    public void addChild(final TreeNode<T> child) {
        child._parent = this;
        _children.add(child);
    }

    /**
     * Returns the direct ancestry of this node as a list, with the first member of the list being the parent of this
     * node and each subsequent member of the list the parent of the previous member. The last ancestor is the root
     * node. Note that the ancestry list does <em>not</em> include this node.
     * @return The ancestors of this node in ascending order.
     */
    public List<TreeNode<T>> getAncestry() {
        final List<TreeNode<T>> ancestry = new ArrayList<>();
        TreeNode<T> current = this;
        while (current.getParent() != null) {
            current = current.getParent();
            ancestry.add(current);
        }
        return ancestry;
    }

    public Set<TreeNode<T>> getDistinctAncestry() {
        final Set<TreeNode<T>> ancestry = new HashSet<>();
        TreeNode<T> current = this;
        while (current.getParent() != null && !ancestry.contains(current.getParent())) {
            current = current.getParent();
            ancestry.add(current);
        }
        return ancestry;
    }

    public int size() {
        return size(this);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof TreeNode)) {
            return false;
        }

        final TreeNode<?> that = (TreeNode<?>) other;

        return new EqualsBuilder().append(_data, that._data).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(_data).toHashCode();
    }

    @Override
    public String toString() {
        return _data.toString();
    }

    private int size(final TreeNode<T> node) {
        int size = 1; // Start with this node.
        for (final TreeNode<T> child : node.getChildren()) {
            size += size(child);
        }
        return size;
    }

    private TreeNode<T> _parent;
    private T           _data;
    private List<TreeNode<T>> _children = new ArrayList<>();
}
