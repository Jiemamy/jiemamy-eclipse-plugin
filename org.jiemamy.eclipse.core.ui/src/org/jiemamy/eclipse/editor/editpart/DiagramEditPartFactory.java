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
package org.jiemamy.eclipse.editor.editpart;

import org.eclipse.core.runtime.Status;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.editor.editpart.diagram.ForeignKeyEditPart;
import org.jiemamy.eclipse.editor.editpart.diagram.RootEditPart;
import org.jiemamy.eclipse.editor.editpart.diagram.StickyEditPart;
import org.jiemamy.eclipse.editor.editpart.diagram.TableEditPart;
import org.jiemamy.eclipse.editor.editpart.diagram.ViewEditPart;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.attribute.constraint.ForeignKeyConstraintModel;
import org.jiemamy.model.dbo.DatabaseObjectModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.model.dbo.ViewModel;
import org.jiemamy.utils.LogMarker;

/**
 * ダイアグラム用EditPartファクトリ。
 * 
 * @author daisuke
 */
public class DiagramEditPartFactory implements EditPartFactory {
	
	private static Logger logger = LoggerFactory.getLogger(DiagramEditPartFactory.class);
	

	public EditPart createEditPart(EditPart context, Object model) {
		logger.debug(LogMarker.LIFECYCLE, "createEditPart for " + model);
		EditPart part = null;
		
		if (model instanceof JiemamyContext) {
			part = new RootEditPart((JiemamyContext) model);
		} else if (model instanceof NodeModel) {
			NodeModel nodeAdapter = (NodeModel) model;
			DatabaseObjectModel entityModel = nodeAdapter.unwrap();
			if (entityModel instanceof TableModel) {
				part = new TableEditPart((NodeModel) model);
			} else if (entityModel instanceof ViewModel) {
				part = new ViewEditPart((NodeModel) model);
			} else if (entityModel == null) {
				if (nodeAdapter instanceof StickyModel) {
					part = new StickyEditPart((StickyModel) nodeAdapter);
				} else {
					JiemamyUIPlugin.log("unknown node: " + model.getClass().getName(), Status.ERROR);
				}
			} else {
				JiemamyUIPlugin.log("unknown entity: " + model.getClass().getName(), Status.ERROR);
			}
		} else if (model instanceof ConnectionModel) {
			ConnectionModel connectionAdapter = (ConnectionModel) model;
			ForeignKeyConstraintModel foreignKey = connectionAdapter.unwrap();
			if (foreignKey != null) {
				part = new ForeignKeyEditPart(connectionAdapter);
			} else {
				JiemamyUIPlugin.log("unknown connection: " + model.getClass().getName(), Status.ERROR);
			}
		} else {
			JiemamyUIPlugin.log("unknown model: " + model.getClass().getName(), Status.ERROR);
		}
		
		if (part == null) {
			JiemamyUIPlugin.log("Cannot create EditPart for unknown model.", Status.ERROR);
		}
		
		return part;
	}
}
