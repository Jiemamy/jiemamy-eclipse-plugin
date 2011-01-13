/*
 * Copyright 2007-2010 Jiemamy Project and the Others.
 * Created on 2010/12/28
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
package org.jiemamy.eclipse.core.ui.model;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DefaultDatabaseObjectModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.utils.NamingUtil;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public abstract class NodeCreation implements Creation {
	
	public void execute(JiemamyContext context, DefaultDiagramModel diagramModel) {
		NodeModel nodeModel = getDiagramElement();
		DefaultDatabaseObjectModel coreModel = getCoreElement();
		NamingUtil.autoName(coreModel, context);
		context.store(coreModel);
		diagramModel.store(nodeModel);
	}
	
	public abstract DefaultDatabaseObjectModel getCoreElement();
	
	public abstract DefaultNodeModel getDiagramElement();
	
}