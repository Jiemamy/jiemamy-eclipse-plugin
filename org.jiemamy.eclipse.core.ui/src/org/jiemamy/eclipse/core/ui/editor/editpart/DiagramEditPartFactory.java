/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.editpart;

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
import org.jiemamy.eclipse.core.ui.editor.editpart.diagram.JiemamyContextEditPart;
import org.jiemamy.eclipse.core.ui.editor.editpart.diagram.StickyEditPart;
import org.jiemamy.eclipse.core.ui.editor.editpart.diagram.TableEditPart;
import org.jiemamy.eclipse.core.ui.editor.editpart.diagram.ViewEditPart;
import org.jiemamy.model.DatabaseObjectModel;
import org.jiemamy.model.DefaultDatabaseObjectNodeModel;
import org.jiemamy.model.StickyNodeModel;
import org.jiemamy.model.table.TableModel;
import org.jiemamy.model.view.ViewModel;
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
		} else if (model instanceof DefaultDatabaseObjectNodeModel) {
			DefaultDatabaseObjectNodeModel node = (DefaultDatabaseObjectNodeModel) model;
			try {
				DatabaseObjectModel core = context.resolve(node.getCoreModelRef());
				if (core instanceof TableModel) {
					part = new TableEditPart(node);
				} else if (core instanceof ViewModel) {
					part = new ViewEditPart(node);
				}
			} catch (EntityNotFoundException e) {
				String message = "Cannot resolve core model: " + node.getCoreModelRef();
				JiemamyUIPlugin.log(message, Status.ERROR);
			}
		} else if (model instanceof StickyNodeModel) {
			part = new StickyEditPart((StickyNodeModel) model);
//		} else if (model instanceof ConnectionModel) {
//			ConnectionModel connectionAdapter = (ConnectionModel) model;
//			ForeignKeyConstraintModel foreignKey = connectionAdapter.unwrap();
//			if (foreignKey != null) {
//				part = new ForeignKeyEditPart(connectionAdapter);
//			}
		}
		
		if (part == null) {
			String message = "Cannot create EditPart for unknown model: " + model.getClass().getName();
			JiemamyUIPlugin.log(message, Status.ERROR);
		}
		
		return part;
	}
}
