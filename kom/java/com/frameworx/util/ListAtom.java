/*
 * This file contains software that has been made available under
 * The Frameworx Open License 1.0. Use and distribution hereof are
 * subject to the restrictions set forth therein.
 *
 * Copyright (c) 2003 The Frameworx Company
 * All Rights Reserved
 */

package com.frameworx.util;

import java.io.Serializable;

/**
 * A doubly-linked list atom
 *
 * @author <a href="mailto:pontus.rydin@frameworx.com">Pontus Rydin</a>
 */
public class ListAtom implements Serializable
	{
	/**
	 * Predecessor
	 */
	private ListAtom m_prev;

	/**
	 * Successor
	 */
	private ListAtom m_next;

	/**
	 * Creates an empty <code>ListAtom</code> with both ends
	 * pointing to itself.
	 */
	public ListAtom()
		{
		m_prev = this;
		m_next = this;
		}

	/**
	 * Returns the succeeding <code>ListAtom</code>
	 * @return The succeeding <code>ListAtom</code>
	 */
	public ListAtom next()
		{
		return m_next;
		}

	/**
	 * Yanks, i.e. unlinks a <code>ListAtom</code> from a list
	 */
	public ListAtom yank()
		{
		ListAtom p = m_prev;
		if(p != this)
			{
			ListAtom n = m_next;
			p.m_next = n;
			n.m_prev = p;
			m_prev = m_next = this;
			}
		return this;
		}

	/**
	 * Makes 'this' the predecessor of 'atom'.
	 * @param atom A <code>ListAtom</code>
	 */
	public ListAtom precede(ListAtom atom)
		{
		ListAtom tn;
		ListAtom an;

		// If 'this' does not already preceede 'atom'
		//
		if((tn = atom.m_prev) != this)
			{
			if((an = m_next) != this)
				{
				// Let 'this' out
				//
				ListAtom ap = m_prev;
				ap.m_next = an;
				an.m_prev = ap;
				}

			m_next = atom;
			m_prev = tn;
			atom.m_prev  = this; 
			tn.m_next    = this;
			}
		return this;
		}

	/**
	 * Returns the predecessor.
	 * @return The predecessor.
	 */
	public ListAtom previous()
		{
		return m_prev;
		}

	/**
	 * Makes 'this' the succcessor of 'atom'.
	 * @param atom A <code>ListAtom</code>
	 */
	public ListAtom succeed(ListAtom atom)
		{
		ListAtom tn;
		ListAtom an;

		// If 'this' does not already succeed 'atom'
		//
		if((tn = atom.m_next) != this)
			{
			if((an = m_next) != this)
				{
				// Let 'this' out
				//
				ListAtom ap = m_prev;
				ap.m_next = an;
				an.m_prev = ap;
				}

			m_prev = atom;
			m_next = tn;
			atom.m_next  = this;
			tn.m_prev    = this;
			}
		return this;
		}
	}


