/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.connection;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.ConnectionModel;

/**
 * {@link ConnectionModel}のEditPolicy。 
 * 
 * @author daisuke
 */
public class JmConnectionEditPolicy extends ConnectionEditPolicy {
	
	@Override
	protected Command getDeleteCommand(GroupRequest request) {
		JiemamyContext context = (JiemamyContext) getHost().getRoot().getContents().getModel();
		return new DeleteConnectionCommand(context, (ConnectionModel) getHost().getModel());
	}
	
}
