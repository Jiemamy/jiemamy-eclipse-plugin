/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2008/08/03
 *
 * This file is part of Jiemamy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.jiemamy.eclipse.core.ui.editor.action;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.JiemamyContextEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.AbstractJmNodeEditPart;
import org.jiemamy.model.JmConnection;
import org.jiemamy.model.JmNode;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.SimpleJmNode;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.model.geometory.JmRectangle;

/**
 * 自動レイアウトアクション。
 * @author daisuke
 */
public class AutoLayoutAction extends AbstractJiemamyAction {
	
	private static final int PADDING = 40;
	
	
	private static Node getNode(List<Node> list, EntityRef<? extends JmNode> ref) {
		for (Node obj : list) {
			EntityNode node = (EntityNode) obj;
			if (ref.isReferenceOf(node.model)) {
				return node;
			}
		}
		return null;
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param viewer ビューア
	 */
	public AutoLayoutAction(GraphicalViewer viewer) {
		super(Messages.AutoLayoutAction_name, viewer);
	}
	
	@Override
	public void run() {
		ProgressMonitorDialog dialog =
				new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		try {
			dialog.run(/*fork*/true, /*cancelable*/false, new Operation(getViewer()));
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	private static class ConnectionEdge extends Edge {
		
		private JmConnection model;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param source 接続元ノード
		 * @param target 接続先ノード
		 * @param model コネクションを表すモデル
		 */
		public ConnectionEdge(Node source, Node target, JmConnection model) {
			super(source, target);
			this.model = model;
		}
	}
	
	private static class EntityNode extends Node {
		
		private SimpleJmNode model;
		
	}
	
	/**
	 * Command to relocate the entity model. This command is executed as a part of
	 * CompoundCommand.
	 */
	private static class LayoutCommand extends Command {
		
		private JiemamyContext context;
		
		private final int diagramIndex;
		
		private SimpleJmNode target;
		
		private int x;
		
		private int y;
		
		private int oldX;
		
		private int oldY;
		
		private Map<JmConnection, List<JmPoint>> oldBendpoints = new HashMap<JmConnection, List<JmPoint>>();
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param context ルートモデル
		 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
		 * @param target 対象ノード
		 * @param x X座標
		 * @param y Y座標
		 */
		public LayoutCommand(JiemamyContext context, int diagramIndex, SimpleJmNode target, int x, int y) {
			this.context = context;
			this.diagramIndex = diagramIndex;
			this.target = target;
			this.x = x;
			this.y = y;
			JmRectangle boundary = target.getBoundary();
			oldX = boundary.x;
			oldY = boundary.y;
		}
		
		@Override
		public void execute() {
			DiagramFacet facet = context.getFacet(DiagramFacet.class);
			SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(diagramIndex);
			target.setBoundary(new JmRectangle(x, y, -1, -1));
			diagram.store(target);
			oldBendpoints.clear();
			for (JmConnection conn : diagram.getSourceConnectionsFor(target.toReference())) {
				List<JmPoint> bendpoints = conn.getBendpoints();
				oldBendpoints.put(conn, new ArrayList<JmPoint>(bendpoints));
				bendpoints.clear();
			}
		}
		
		@Override
		public void undo() {
			DiagramFacet facet = context.getFacet(DiagramFacet.class);
			SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(diagramIndex);
			for (JmConnection conn : diagram.getSourceConnectionsFor(target.toReference())) {
				List<JmPoint> bendpoints = conn.getBendpoints();
				bendpoints.clear();
				for (JmPoint bendpoint : oldBendpoints.get(conn)) {
					bendpoints.add(bendpoint);
				}
			}
			target.setBoundary(new JmRectangle(oldX, oldY, -1, -1));
			diagram.store(target);
		}
	}
	
	private static class Operation implements IRunnableWithProgress {
		
		private final GraphicalViewer viewer;
		
		
		public Operation(GraphicalViewer viewer) {
			this.viewer = viewer;
		}
		
		public void run(IProgressMonitor monitor) {
			// 0:assemble nodes, 1:assemble edges, 2:analyze graph, 3:execute commands, 4:refresh views
			int totalWork = 5;
			int worked = 0;
			monitor.beginTask(Messages.AutoLayoutAction_name, totalWork);
			
			@SuppressWarnings("unchecked")
			// Java1.4対応APIのため、Classに型パラメータをつけることができない
			final List<EditPart> editParts = ((JiemamyContextEditPart) viewer.getContents()).getChildren();
			
			@SuppressWarnings("unchecked")
			// Java1.4対応APIのため、Classに型パラメータをつけることができない
			final List<Node> graphNodes = new NodeList();
			
			@SuppressWarnings("unchecked")
			// Java1.4対応APIのため、Classに型パラメータをつけることができない
			final List<Edge> graphEdges = new EdgeList();
			
			CompoundCommand commands = new CompoundCommand();
			JiemamyContext context = ((JiemamyContextEditPart) viewer.getContents()).getModel();
			
			// assemble nodes
			monitor.setTaskName(Messages.AutoLayoutAction_name + " - assemble nodes."); // RESOURCE
			assembleNodes(editParts, graphNodes);
			monitor.worked(++worked);
			
			// assemble edges
			monitor.setTaskName(Messages.AutoLayoutAction_name + " - assemble edges."); // RESOURCE
			assembleEdges(graphNodes, graphEdges);
			monitor.worked(++worked);
			
			// amnalyze graph
			monitor.setTaskName(Messages.AutoLayoutAction_name + " - analyze graph."); // RESOURCE
			analyzeGraph(graphNodes, graphEdges, commands, context);
			monitor.worked(++worked);
			
			monitor.setTaskName(Messages.AutoLayoutAction_name + " - execute command stack."); // RESOURCE
			viewer.getEditDomain().getCommandStack().execute(commands);
			monitor.worked(++worked);
			
			monitor.setTaskName(Messages.AutoLayoutAction_name + " - refresh views."); // RESOURCE
			refreshViews(editParts);
			monitor.worked(++worked);
			monitor.done();
		}
		
		private void analyzeGraph(List<Node> graphNodes, List<Edge> graphEdges, CompoundCommand commands,
				JiemamyContext context) {
			DirectedGraph graph = new DirectedGraph();
			graph.setDefaultPadding(new Insets(PADDING));
			graph.nodes = (NodeList) graphNodes;
			graph.edges = (EdgeList) graphEdges;
			new DirectedGraphLayout().visit(graph);
			for (Object obj : graph.nodes) {
				EntityNode node = (EntityNode) obj;
				commands.add(new LayoutCommand(context, TODO.DIAGRAM_INDEX, node.model, node.x, node.y));
			}
		}
		
		private void assembleEdges(List<Node> graphNodes, List<Edge> graphEdges) {
			JiemamyContext context = ((JiemamyContextEditPart) viewer.getContents()).getModel();
			DiagramFacet facet = context.getFacet(DiagramFacet.class);
			SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
			for (Object obj : graphNodes) {
				EntityNode node = (EntityNode) obj;
				
				Collection<? extends JmConnection> connections =
						diagram.getSourceConnectionsFor(node.model.toReference());
				CONN_LOOP: for (JmConnection connection : connections) {
					if (connection.isSelfConnection()) {
						continue;
					}
					
					// skip if the connection already added
					for (Object obj2 : graphEdges) {
						ConnectionEdge edge = (ConnectionEdge) obj2;
						if (edge.model == connection) {
							continue CONN_LOOP;
						}
					}
					Node source = getNode(graphNodes, connection.getSource());
					Node target = getNode(graphNodes, connection.getTarget());
					if (source != null && target != null) {
						graphEdges.add(new ConnectionEdge(source, target, connection));
					}
				}
			}
		}
		
		private void assembleNodes(List<EditPart> editParts, List<Node> graphNodes) {
			for (EditPart obj : editParts) {
				if (obj instanceof AbstractJmNodeEditPart) {
					AbstractJmNodeEditPart editPart = (AbstractJmNodeEditPart) obj;
					SimpleJmNode model = (SimpleJmNode) editPart.getModel();
					EntityNode node = new EntityNode();
					node.model = model;
					node.width = editPart.getFigure().getSize().width;
					node.height = editPart.getFigure().getSize().height;
					graphNodes.add(node);
				}
			}
		}
		
		private void refreshViews(final List<EditPart> editParts) {
			viewer.getControl().getDisplay().syncExec(new Runnable() {
				
				public void run() {
					for (EditPart editPart : editParts) {
						editPart.refresh();
					}
				}
			});
		}
	}
}
