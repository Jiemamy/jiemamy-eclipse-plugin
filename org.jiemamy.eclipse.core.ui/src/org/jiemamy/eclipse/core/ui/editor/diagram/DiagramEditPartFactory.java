/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2008/07/29
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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import org.apache.commons.lang.Validate;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityNotFoundException;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.JiemamyEditor;
import org.jiemamy.eclipse.core.ui.editor.diagram.connection.ForeignKeyEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.sticky.StickyEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.TableEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.view.ViewEditPart;
import org.jiemamy.model.DbObject;
import org.jiemamy.model.JmConnection;
import org.jiemamy.model.JmStickyNode;
import org.jiemamy.model.SimpleDbObjectNode;
import org.jiemamy.model.constraint.JmForeignKeyConstraint;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.view.JmView;
import org.jiemamy.utils.LogMarker;

/**
 * ダイアグラム用EditPartファクトリ。
 * 
 * @author daisuke
 */
public class DiagramEditPartFactory implements EditPartFactory {
	
	private static Logger logger = LoggerFactory.getLogger(DiagramEditPartFactory.class);
	
	private final JiemamyEditor editor;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param editor {@link EditPart}を司るエディタインスタンス
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public DiagramEditPartFactory(JiemamyEditor editor) {
		Validate.notNull(editor);
		this.editor = editor;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @param parent {@link EditPart}
	 * @param model model object
	 * @return new {@link EditPart} instance
	 */
	public EditPart createEditPart(EditPart parent, Object model) {
		logger.debug(LogMarker.LIFECYCLE, "createEditPart for {}", model);
		EditPart part = null;
		JiemamyContext context = editor.getJiemamyContext();
		
		if (model instanceof JiemamyContext) {
			part = new JiemamyContextEditPart((JiemamyContext) model);
		} else if (model instanceof SimpleDbObjectNode) {
			SimpleDbObjectNode node = (SimpleDbObjectNode) model;
			try {
				DbObject core = context.resolve(node.getCoreModelRef());
				if (core instanceof JmTable) {
					part = new TableEditPart(node);
				} else if (core instanceof JmView) {
					part = new ViewEditPart(node);
				}
			} catch (EntityNotFoundException e) {
				String message = "Cannot resolve core model: " + node.getCoreModelRef();
				JiemamyUIPlugin.log(message, Status.ERROR);
			}
		} else if (model instanceof JmStickyNode) {
			part = new StickyEditPart((JmStickyNode) model);
		} else if (model instanceof JmConnection) {
			JmConnection connection = (JmConnection) model;
			JmForeignKeyConstraint foreignKey = context.resolve(connection.getCoreModelRef());
			if (foreignKey != null) {
				part = new ForeignKeyEditPart(connection);
			}
		}
		
		if (part == null) {
			String message = "Cannot create EditPart for unknown model: " + model.getClass().getName();
			JiemamyUIPlugin.log(message, Status.ERROR);
		}
		
		return part;
	}
}
