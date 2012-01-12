/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2011/03/02
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

import org.eclipse.core.runtime.Status;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.TableTreeEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column.ColumnTreeEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.constraint.ConstraintTreeEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.view.JmViewTreeEditPart;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.constraint.JmConstraint;
import org.jiemamy.model.domain.JmDomain;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.view.JmView;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class OutlineTreeEditPartFactory implements EditPartFactory {
	
	public EditPart createEditPart(EditPart parent, Object model) {
		EditPart part = null;
		
		if (model instanceof JiemamyContext) {
			JiemamyContext context = (JiemamyContext) model;
			part = new JiemamyContextTreeEditPart(context);
//		} else if (model instanceof ModelContainer) {
//			ModelContainer modelContainer = (ModelContainer) model;
//			part = new ModelContainerEditPart(modelContainer);
		} else if (model instanceof JmDomain) {
			JmDomain domain = (JmDomain) model;
			part = new DomainTreeEditPart(domain);
		} else if (model instanceof JmTable) {
			JmTable table = (JmTable) model;
			part = new TableTreeEditPart(table);
		} else if (model instanceof JmColumn) {
			JmColumn column = (JmColumn) model;
			part = new ColumnTreeEditPart(column);
		} else if (model instanceof JmConstraint) {
			JmConstraint constraint = (JmConstraint) model;
			part = new ConstraintTreeEditPart(constraint);
		} else if (model instanceof JmView) {
			JmView view = (JmView) model;
			part = new JmViewTreeEditPart(view);
		}
		
		if (part == null) {
			String message = "Cannot create EditPart for unknown model: " + model.getClass().getName();
			JiemamyUIPlugin.log(message, Status.ERROR);
		}
		
		return part;
	}
	
}
