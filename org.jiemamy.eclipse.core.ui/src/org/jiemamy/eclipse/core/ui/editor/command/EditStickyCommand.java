/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/01/26
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
package org.jiemamy.eclipse.core.ui.editor.command;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.StickyNodeModel;

/**
 * {@link StickyNodeModel}を編集するコマンド。
 * 
 * @version $Id$
 * @author daisuke
 */
public class EditStickyCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(EditStickyCommand.class);
	
	private final JiemamyContext context;
	
	private final DefaultDiagramModel diagramModel;
	
	private final StickyNodeModel stickyModel;
	
	private final StickyNodeModel oldStickyModel;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コンテキスト
	 * @param diagramModel ダイアグラム
	 * @param stickyModel メモ
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public EditStickyCommand(JiemamyContext context, DefaultDiagramModel diagramModel, StickyNodeModel stickyModel) {
		Validate.notNull(context);
		Validate.notNull(diagramModel);
		Validate.notNull(stickyModel);
		this.context = context;
		this.diagramModel = diagramModel;
		this.stickyModel = stickyModel;
		oldStickyModel = diagramModel.resolve(stickyModel.toReference());
	}
	
	@Override
	public void execute() {
		logger.debug("execute");
		diagramModel.store(stickyModel);
		context.getFacet(DiagramFacet.class).store(diagramModel);
	}
	
	@Override
	public void undo() {
		logger.debug("undo");
		diagramModel.store(oldStickyModel);
		context.getFacet(DiagramFacet.class).store(diagramModel);
	}
}
