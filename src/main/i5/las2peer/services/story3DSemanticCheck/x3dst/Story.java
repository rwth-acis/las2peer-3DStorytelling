package i5.las2peer.services.story3DSemanticCheck.x3dst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import i5.cae.semanticCheck.SemanticCheckResponse;
import i5.cae.simpleModel.SimpleModel;
import i5.las2peer.services.story3DSemanticCheck.model.EntityAttribute;
import i5.las2peer.services.story3DSemanticCheck.model.Model;
import i5.las2peer.services.story3DSemanticCheck.model.edge.Edge;
import i5.las2peer.services.story3DSemanticCheck.model.node.Node;

public class Story extends Model {

	
	private class StoryNode {
		
		private Ancestory heritage = new Ancestory();
		private Node under;
		
		public StoryNode(Node node) {
			this.under = node;
		}
		
		public EntityAttribute getAttribute(String name) {
			for (EntityAttribute curr:this.under.getAttributes()) {
				if (curr.getName() == name) {
					return curr;
				}
			}
			return null;
		}
				
		public ArrayList<BiDirEdge> filterAdjecentEdges(Model model) {
			ArrayList<BiDirEdge> res = new ArrayList<BiDirEdge>();
			for (Edge e:model.getEdges()) {
				if (e.getSourceNode().equals(this.under.getId()) ||
						e.getTargetNode().equals(this.under.getId())) {
					res.add(new BiDirEdge(e));	
				}
			}
			return res;
		}
		
		public ArrayList<BiDirEdge> filterIncomingEdges(Model model) {
			ArrayList<BiDirEdge> res = new ArrayList<BiDirEdge>();
			for (BiDirEdge e:this.filterAdjecentEdges(model)) {
				if (e.getTargetNode().equals(this.under.getId())) {
					res.add(e);	
				}
			}
			return res;
		}
		
		public ArrayList<BiDirEdge> filterOutgoingEdges(Model model) {
			ArrayList<BiDirEdge> res = new ArrayList<BiDirEdge>();
			for (BiDirEdge e:this.filterAdjecentEdges(model)) {
				if (e.getSourceNode().equals(this.under.getId())) {
					res.add(e);	
				}
			}
			return res;
		}
		
		public ArrayList<StoryNode> filterAdjacentNodes(Story s, ArrayList<BiDirEdge> edges) {
			ArrayList<StoryNode> res = new ArrayList<StoryNode>();
			for (BiDirEdge e:edges) {
				res.add(e.getOther(s, this));
			}
			return res;
		}
		
		public boolean isMedia() {
			if (this.under.getType().equals(Story.NODE_TYPE_TEXT) ||
				this.under.getType().equals(Story.NODE_TYPE_IMAGE) ||
				this.under.getType().equals(Story.NODE_TYPE_VIDEO)) {
				return true;
			} else {
				return false;
			}
		}
		
		public char getShortType() {
			String t = this.under.getType();
			if (t.equals(Story.NODE_TYPE_SU)) {
				return 'P';
			} if (t.equals(Story.NODE_TYPE_BEGIN)) {
				return 'B';
			} if (t.equals(Story.NODE_TYPE_MIDDLE)) {
				return 'M';
			} if (t.equals(Story.NODE_TYPE_END)) {
				return 'E';
			} else {
				return 'N';
			}
		}

		public Ancestory getHeritage() {
			return heritage;
		}

		public void setHeritage(Ancestory heritage) {
			this.heritage = heritage;
		}
		
		public String getContext() {
			return this.heritage.get(0).getType();
		}
		
		public String getId() {
			return this.under.getId();
		}
		
		public String getType() {
			return this.under.getType();
		}
	}
	
	@SuppressWarnings("serial")
	private class Ancestory extends ArrayList<StoryNode> {
		private String stringRepr = null;
		
		public boolean test(String regex) {
			return this.getString().matches(regex);
		}
		
		public String getString() {
			if (this.stringRepr == null) {
				this.stringRepr = "";
				this.forEach((n) -> this.stringRepr += n.getShortType());
			}
			return this.stringRepr;
		}
	}
	
	private class BiDirEdge {
		
		private Edge under;

		public BiDirEdge(Edge edge) {
			this.under = edge;
		}

		public StoryNode getOther(Story s, StoryNode node) {
			if (this.getSourceNode().equals(node.getId())) {
				return s.findNodeById(this.getTargetNode());
			} else if (this.getTargetNode().equals(node.getId())) {
				return s.findNodeById(this.getTargetNode());
			} else {
				return null;
			}
		}
		
		private String getTargetNode() {
			return this.under.getTargetNode();
		}

		public String getSourceNode() {
			return this.under.getSourceNode();
		}

		public Object getType() {
			return this.under.getType();
		}
	}
	
	public static final int ERROR_NO_ROOT = 1;
	public static final int ERROR_NO_TREE = 2;
	public static final int ERROR_MULTI_BEGIN = 3;
	public static final int ERROR_DEAD_END = 4;
	public static final int ERROR_INVALID_TRANSITION = 5;
	public static final int ERROR_NO_BEGIN = 6;

	public static final String NODE_TYPE_SU = "Story Unit";
	public static final String NODE_TYPE_BEGIN = "Begin";
	public static final String NODE_TYPE_MIDDLE = "Middle";
	public static final String NODE_TYPE_END = "End";
	public static final String NODE_TYPE_TEXT = "Text";
	public static final String NODE_TYPE_IMAGE = "Image";
	public static final String NODE_TYPE_VIDEO = "Video";
	public static final String NODE_TYPE_MEDIA = "Media";
	public static final String EDGE_TYPE_REQUIREMENT = "Requirement";
	public static final String EDGE_TYPE_TRANSITION = "Story Transition";
		
	private StoryNode root = null;
	private StoryNode start = null;
	private ArrayList<StoryNode> relevants = new ArrayList<StoryNode>();
	
	private SemanticCheckResponse error = null;
	
	public Story(SimpleModel simpleModel) {
		super(simpleModel);
		// TODO Auto-generated constructor stub
	}

	public SemanticCheckResponse validate() {
		if ((this.root = this.findRoot()) == null) {
			System.out.println("1");
			return this.error;
		} else if (!this.parseTree(this.root)) {
			System.out.println("2");
			return this.error;
		} else if ((this.start = this.findStart()) == null) {
			System.out.println("3");
			return this.error;
		} else if (!this.checkTransitions(this.start)) {
			System.out.println("4");
			return this.error;
		}
		return SemanticCheckResponse.success();
	}
	
	private StoryNode findNodeById(String id) {
		for (Node n:this.getNodes()) {
			if (n.getId().equals(id)) {
				return toStoryNode(n);
			}
		}
		return null;
	}
	
	private StoryNode findRoot() {
		StoryNode res = null; 
		for (StoryNode curr:this.getStoryNodes()) {
			if (curr.getType().equals(Story.NODE_TYPE_SU) &&
					curr.filterIncomingEdges(this).size() == 0) {
				if (res == null) {
					res = curr;
				} else {
					this.error = SemanticCheckResponse.error(Story.ERROR_NO_ROOT, curr.getId(), null, "The Story Graph has multiple root nodes");
					return null;
				}
			}
		}
		if (res == null) {
			this.error = SemanticCheckResponse.error(Story.ERROR_NO_ROOT, null, null, "The Story Graph has no root node");
		}
		return res;
	}
	
	public StoryNode findStart() {
		StoryNode start = null;
		for (StoryNode n:this.relevants) {
			if (n.getHeritage().test("(BP)*")) {
				if (start == null) {
					start = n;
				} else {
					this.error = SemanticCheckResponse.error(Story.ERROR_MULTI_BEGIN, n.getId(), null, "The Story has multiple starts");
					return null;
				}
			}
		}
		if (start == null) {
			this.error = SemanticCheckResponse.error(Story.ERROR_NO_BEGIN, null, null, "The Story has no start");
		}
		
		return start;			
	}
	
	private ArrayList<StoryNode> visited2 = new ArrayList<StoryNode>(); 
	public boolean checkTransitions(StoryNode n) {
		if (visited2.contains(n)) {
			return true;
		}
		visited2.add(n);
		
		if (n.getContext().equals(Story.NODE_TYPE_BEGIN) ||
			n.getContext().equals(Story.NODE_TYPE_MIDDLE)) {
			ArrayList<BiDirEdge> nextEdges = n.filterOutgoingEdges(this);
			nextEdges.removeIf((e) -> !e.getType().equals(EDGE_TYPE_TRANSITION));
			ArrayList<StoryNode> next = n.filterAdjacentNodes(this, nextEdges);
			if (next.size() == 0) {
				this.error = SemanticCheckResponse.error(Story.ERROR_DEAD_END , n.getId(), null, "There is a dead end");
				return false;					
			}
			String regex = n.getHeritage().getString();
			regex = regex.substring(1, regex.length());
			regex = "(BP)*" + (n.getContext().equals(Story.NODE_TYPE_BEGIN) ? "M" : "E") + regex;
							
			for (StoryNode con:next) {
				if (!con.getHeritage().test(regex)) {
					this.error = SemanticCheckResponse.error(Story.ERROR_INVALID_TRANSITION, n.getId(), con.getId(), "Invalid transition");
					return false;
				}
				if (!this.checkTransitions(con)) {
					return false;
				}
			}
		} else if (n.getContext().equals(Story.NODE_TYPE_END)) {
			ArrayList<BiDirEdge> nextEdges = n.filterOutgoingEdges(this);
			nextEdges.removeIf((e) -> !e.getType().equals(EDGE_TYPE_TRANSITION));
			ArrayList<StoryNode> next = n.filterAdjacentNodes(this, nextEdges);
			
			int num = 0;
			
			String upperCut = n.getHeritage().getString().replaceFirst("((EP)*)", "");
			String foward = "";
			if (upperCut.length() == 0) {
			} else	if (upperCut.charAt(0) == 'B') {
				foward = "M" + upperCut.substring(1, upperCut.length());
			} else if (upperCut.charAt(0) == 'M') {
				foward = "E" + upperCut.substring(1, upperCut.length());
			}
			foward = "(BP)*" + foward;
			
			for (StoryNode con:next) {
				if (con.getHeritage().test(foward)) {
					num ++;
					if (!this.checkTransitions(con)) {
						return false;
					}
				} else {
					this.error = SemanticCheckResponse.error(Story.ERROR_INVALID_TRANSITION, n.getId(), con.getId(), "Illegal transition");
					return false;					
				}
			}
			System.out.println(num+" "+ upperCut);
			if (num == 0 && upperCut.length() > 0) {
				this.error = SemanticCheckResponse.error(Story.ERROR_DEAD_END, n.getId(), null, "There is a dead end");
				return false;
			}
			if (num > 0 && upperCut.length() == 0) {
				this.error = SemanticCheckResponse.error(Story.ERROR_INVALID_TRANSITION, n.getId(), null, "TIllegal transition");
				return false;
			}
		}
		
		return true;
	}
	
	ArrayList<StoryNode> visited = new ArrayList<StoryNode>();
	public boolean parseTree(StoryNode curr) {
		if (visited.contains(curr)) {
			this.error = SemanticCheckResponse.error(Story.ERROR_NO_TREE, curr.getId(), null, "There is a loop in the Story Tree");
			return false;
		}
		visited.add(curr);
		if (curr.isMedia()) {
			relevants.add(curr);
			return true;
		}
		ArrayList<StoryNode> next = new ArrayList<StoryNode>();
		
		if (curr.getType().equals(Story.NODE_TYPE_SU)) {
			next = curr.filterAdjacentNodes(this, curr.filterOutgoingEdges(this));
			
			StoryNode begin = null;
			StoryNode middle = null;
			StoryNode end = null;
			for (StoryNode n:next) {
				if (n.getType().equals(Story.NODE_TYPE_BEGIN)) {
					if (begin == null) {
						begin = n;
					} else {
						this.error = SemanticCheckResponse.error(Story.ERROR_NO_TREE, curr.getId(), n.getId(), "There can only be one 'Begin' to a Story Unit");
						return false;
					}
				} else if (n.getType().equals(Story.NODE_TYPE_MIDDLE)) {
					if (middle == null) {
						middle = n;
					} else {
						this.error = SemanticCheckResponse.error(Story.ERROR_NO_TREE, curr.getId(), n.getId(), "There can only be one 'Middle' to a Story Unit");
						return false;
					}
				} else if (n.getType().equals(Story.NODE_TYPE_END)) {
					if (end == null) {
						end = n;
					} else {
						this.error = SemanticCheckResponse.error(Story.ERROR_NO_TREE, curr.getId(), n.getId(), "There can only be one 'End' to a Story Unit");
						return false;
					}
				} else {
					next.remove(n);
				}
			}
			if (begin == null || middle == null || end == null) {
				this.error = SemanticCheckResponse.error(Story.ERROR_NO_TREE, curr.getId(), null, "A Story Unit is lacking a Begin, Middle, or End");
				return false;
			}
		} else if (curr.getType().equals(Story.NODE_TYPE_BEGIN) ||
				curr.getType().equals(Story.NODE_TYPE_MIDDLE) ||	
				curr.getType().equals(Story.NODE_TYPE_END)) {
			next = curr.filterAdjacentNodes(this, curr.filterOutgoingEdges(this));
			
			for (StoryNode n:next) {
				if (n.getType().equals(Story.NODE_TYPE_SU) ||
					n.isMedia()) {
				} else {
					next.remove(n);
				}
			}
		}

		Ancestory heritage = (Ancestory) curr.getHeritage().clone();
		heritage.add(0, curr);
		
		for (StoryNode n:next) {
			n.setHeritage(heritage);
			if (!this.parseTree(n)) {
				return false;
			}
		}
		
		return true;
	}
	
	public ArrayList<StoryNode> getStoryNodes() {
		ArrayList<StoryNode> res = new ArrayList<StoryNode>();
		super.getNodes().forEach((n) -> res.add(toStoryNode(n)));
		return res;
	}
	
	private Map<String, StoryNode> generated = new HashMap<String, StoryNode>();
	private StoryNode toStoryNode(Node n) {
		StoryNode res;
		if ((res = generated.get(n.getId())) == null) {
			res = new StoryNode(n);
			generated.put(n.getId(), res);
		}
		return res;
	}
}
