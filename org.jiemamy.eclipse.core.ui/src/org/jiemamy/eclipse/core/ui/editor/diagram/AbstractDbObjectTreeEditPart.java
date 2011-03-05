/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/03/05
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

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.model.DbObject;
import org.jiemamy.utils.LogMarker;

/**
 * {@link DbObject}に対するTree用EditPart（コントローラ）の抽象クラス。
 * 
 * @version $Id$
 * @author daisuke
 */
public abstract class AbstractDbObjectTreeEditPart extends AbstractModelTreeEditPart implements EditDialogSupport {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractDbObjectTreeEditPart.class);
	

	@Override
	public void performRequest(Request req) {
		if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			logger.info(LogMarker.LIFECYCLE, "doubleClicked");
			openEditDialog();
			return;
		}
		super.performRequest(req);
	}
}
