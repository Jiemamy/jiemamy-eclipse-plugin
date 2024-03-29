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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.sticky;

import org.apache.commons.lang.StringUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.AbstractJmNodeEditPart;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.JmStickyNode;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.utils.LogMarker;

/**
 * ビューモデルに対するDiagram用EditPart（コントローラ）。
 * 
 * @author daisuke
 */
public class StickyEditPart extends AbstractJmNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(StickyEditPart.class);
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param stickyNode コントロール対象の付箋モデル
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public StickyEditPart(JmStickyNode stickyNode) {
		super(stickyNode);
	}
	
	@Override
	public JmStickyNode getModel() {
		return (JmStickyNode) super.getModel();
	}
	
	public void openEditDialog() {
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		JmStickyNode stickyNode = getModel();
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog: {}", stickyNode);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		
		Shell shell = getViewer().getControl().getShell();
		StickyEditDialog dialog = new StickyEditDialog(shell, context, stickyNode, diagram);
		
		if (dialog.open() == Dialog.OK) {
			org.eclipse.gef.commands.Command command = new EditStickyCommand(context, diagram, stickyNode);
			GraphicalViewer viewer = (GraphicalViewer) getViewer();
			viewer.getEditDomain().getCommandStack().execute(command);
		}
	}
	
//	@Override
//	protected DirectEditManager getDirectEditManager() {
//		StickyFigure figure = (StickyFigure) getFigure();
//		CellEditorLocator locator = new NodeCellEditorLocator(figure.getContentsLabel());
//		return new StickyDirectEditManager(this, MultiLineTextCellEditor.class, locator);
//	}
	
	@Override
	protected IFigure createFigure() {
		StickyFigure figure = new StickyFigure();
		String contents = getModel().getContents();
		
		if (StringUtils.isEmpty(contents) == false) {
			Panel tooltip = new Panel();
			tooltip.setLayoutManager(new StackLayout());
			tooltip.setBackgroundColor(ColorConstants.tooltipBackground);
			tooltip.add(new Label(contents));
			
			figure.setToolTip(tooltip);
		}
		
		updateFigure(figure);
		return figure;
	}
	
	/**
	 * {@link StickyFigure}のアップデートを行う。
	 * 
	 * @param figure アップデート対象のフィギュア
	 */
	@Override
	protected void updateFigure(IFigure figure) {
		logger.debug(LogMarker.LIFECYCLE, "updateFigure");
		
		JmStickyNode stickyNode = getModel();
		StickyFigure stickyFigure = (StickyFigure) figure;
		
		stickyFigure.setContents(stickyNode.getContents());
		stickyFigure.setBgColor(ConvertUtil.convert(stickyNode.getColor()));
	}
}
