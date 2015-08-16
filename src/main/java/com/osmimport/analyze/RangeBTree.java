package com.osmimport.analyze;

import java.io.Serializable;

public class RangeBTree<Key extends Comparable<Key>> implements Serializable {

	private static final short M = (short)Math.pow(2,7); // max children per B-tree node = M-1

	private Node root; // root of the B-tree
	private short HT; // height of the B-tree
	private short N; // number of key-value pairs in the B-tree

	// helper B-tree node data type
	public static final class Node implements Serializable{
		private static long cpt = 0;
		private long nodeid = cpt++;
		private short m; // number of children
		private Entry[] children = new Entry[M]; // the array of children

		private Node(short k) {
			m = k;
		} // create a node with k children

		@Override
		public String toString() {
			return "Node " + nodeid;
		}
	}

	// internal nodes: only use key and next
	// external nodes: only use key and value
	private static class Entry implements Serializable{
		private long key;
		private Node next;

		public Entry(long key, Node next) {
			this.key = key;
			this.next = next;
		}
	}

	// constructor
	public RangeBTree() {
		root = new Node((short)0);
	}

	// return number of key-value pairs in the B-tree
	public int size() {
		return N;
	}

	// return height of B-tree
	public int height() {
		return HT;
	}

	// search for given key, return associated value; return null if no such key
	public Node get(long key) {
		return search(root, key, HT);
	}

	private Node search(Node x, long key, int ht) {
		Entry[] children = x.children;

		// external node
		if (ht == 0) {
			for (int j = 0; j < x.m; j++) {
				if (key == children[j].key)
					return x;
			}
		}

		// internal node
		else {
			for (int j = 0; j < x.m; j++) {
				if (j + 1 == x.m || (key < children[j + 1].key))
					return search(children[j].next, key, ht - 1);
			}
		}
		return null;
	}

	// insert key-value pair
	// add code to check for duplicate keys
	public void put(long key) {
		Node u = insert(root, key, HT);
		N++;
		if (u == null)
			return;

		// need to split root
		Node t = new Node((short)2);
		t.children[0] = new Entry(root.children[0].key, root);
		t.children[1] = new Entry(u.children[0].key, u);
		root = t;
		HT++;
	}

	private Node insert(Node h, long key, int ht) {
		int j;
		Entry t = new Entry(key, null);

		// external node
		if (ht == 0) {
			for (j = 0; j < h.m; j++) {
				if (key < h.children[j].key)
					break;
			}
		}

		// internal node
		else {
			for (j = 0; j < h.m; j++) {
				if ((j + 1 == h.m) || (key < h.children[j + 1].key)) {
					Node u = insert(h.children[j++].next, key, ht - 1);
					if (u == null)
						return null;
					t.key = u.children[0].key;
					t.next = u;
					break;
				}
			}
		}

		for (int i = h.m; i > j; i--)
			h.children[i] = h.children[i - 1];
		h.children[j] = t;
		h.m++;
		if (h.m < M)
			return null;
		else
			return split(h);
	}

	// split node in half
	private Node split(Node h) {
		Node t = new Node((short)(M / 2));
		h.m = (short)(M / 2);
		for (int j = 0; j < M / 2; j++)
			t.children[j] = h.children[M / 2 + j];
		return t;
	}

	// for debugging
	public String toString() {
		return toString(root, HT, "") + "\n";
	}

	private String toString(Node h, int ht, String indent) {
		String s = "";
		Entry[] children = h.children;

		if (ht == 0) {
			for (int j = 0; j < h.m; j++) {
				s += indent + children[j].key + "\n";
			}
		} else {
			for (int j = 0; j < h.m; j++) {
				if (j > 0)
					s += indent + "(" + children[j].key + ")\n";
				s += toString(children[j].next, ht - 1, indent + "     ");
			}
		}
		return s;
	}

	/*************************************************************************
	 * test client
	 *************************************************************************/
	public static void main(String[] args) {
		RangeBTree<Long> st = new RangeBTree<Long>();

		long start = System.currentTimeMillis();

		for (long l = 0; l < 30000000; l++) {
			st.put(l);
			if (l%1000000 == 0) {
				System.out.println(l + " max noeuds :" + Node.cpt);
				System.out.println("h :" + st.HT);
			}
		}

		System.out.println("done in " + (System.currentTimeMillis() - start) + " ms");
		

	}

}