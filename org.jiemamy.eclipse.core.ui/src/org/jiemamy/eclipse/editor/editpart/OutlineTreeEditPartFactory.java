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

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.editor.ModelContainer;
import org.jiemamy.eclipse.editor.editpart.outlinetree.ColumnTreeEditPart;
import org.jiemamy.eclipse.editor.editpart.outlinetree.ConstraintTreeEditPart;
import org.jiemamy.eclipse.editor.editpart.outlinetree.DomainTreeEditPart;
import org.jiemamy.eclipse.editor.editpart.outlinetree.ModelContainerEditPart;
import org.jiemamy.eclipse.editor.editpart.outlinetree.RootTreeEditPart;
import org.jiemamy.eclipse.editor.editpart.outlinetree.TableTreeEditPart;
import org.jiemamy.eclipse.editor.editpart.outlinetree.ViewTreeEditPart;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.attribute.constraint.ConstraintModel;
import org.jiemamy.model.dbo.DomainModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.model.dbo.ViewModel;

/**
 * アウトラインツリー用EditPartファクトリ。
 * 
 * @author daisuke
 */
public class OutlineTreeEditPartFactory implements EditPartFactory {
	
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		
		if (model instanceof JiemamyContext) {
			JiemamyContext rootModel = (JiemamyContext) model;
			part = new RootTreeEditPart(rootModel);
		} else if (model instanceof ModelContainer) {
			ModelContainer modelContainer = (ModelContainer) model;
			part = new ModelContainerEditPart(modelContainer);
		} else if (model instanceof DomainModel) {
			DomainModel domainModel = (DomainModel) model;
			part = new DomainTreeEditPart(domainModel);
		} else if (model instanceof TableModel) {
			TableModel tableModel = (TableModel) model;
			part = new TableTreeEditPart(tableModel);
		} else if (model instanceof ColumnModel) {
			ColumnModel columnModel = (ColumnModel) model;
			part = new ColumnTreeEditPart(columnModel);
		} else if (model instanceof ConstraintModel) {
			ConstraintModel constraintModel = (ConstraintModel) model;
			part = new ConstraintTreeEditPart(constraintModel);
		} else if (model instanceof ViewModel) {
			ViewModel viewModel = (ViewModel) model;
			part = new ViewTreeEditPart(viewModel);
		} else {
			JiemamyUIPlugin.log("unknown model: " + model.getClass().getName(), Status.ERROR);
		}
		
		return part;
	}
}
