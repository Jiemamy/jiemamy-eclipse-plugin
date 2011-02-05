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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.sticky;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.JmStickyNode;
import org.jiemamy.model.SimpleJmDiagram;

/**
 * {@link JmStickyNode}を編集するコマンド。
 * 
 * @version $Id$
 * @author daisuke
 */
public class EditStickyCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(EditStickyCommand.class);
	
	private final JiemamyContext context;
	
	private final SimpleJmDiagram diagram;
	
	private final JmStickyNode stickyNode;
	
	private final JmStickyNode oldStickyNode;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コンテキスト
	 * @param diagram ダイアグラム
	 * @param stickyNode メモ
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public EditStickyCommand(JiemamyContext context, SimpleJmDiagram diagram, JmStickyNode stickyNode) {
		Validate.notNull(context);
		Validate.notNull(diagram);
		Validate.notNull(stickyNode);
		this.context = context;
		this.diagram = diagram;
		this.stickyNode = stickyNode;
		oldStickyNode = diagram.resolve(stickyNode.toReference());
	}
	
	@Override
	public void execute() {
		logger.debug("execute");
		diagram.store(stickyNode);
		context.getFacet(DiagramFacet.class).store(diagram);
	}
	
	@Override
	public void undo() {
		logger.debug("undo");
		diagram.store(oldStickyNode);
		context.getFacet(DiagramFacet.class).store(diagram);
	}
}
