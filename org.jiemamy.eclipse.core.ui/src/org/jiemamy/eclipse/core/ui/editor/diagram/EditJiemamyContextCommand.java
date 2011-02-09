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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.JmMetadata;
import org.jiemamy.SqlFacet;
import org.jiemamy.model.script.JmAroundScript;

/**
 * {@link JiemamyContext}を編集するコマンド。
 * 
 * @version $Id$
 * @author daisuke
 */
public class EditJiemamyContextCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(EditJiemamyContextCommand.class);
	
	private final JiemamyContext context;
	
	private final JmMetadata metadata;
	
	private final JmAroundScript universalAroundScript;
	
	private final JmMetadata oldMetadata;
	
	private final JmAroundScript oldUniversalAroundScript;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コンテキスト
	 * @param metadata new metdata
	 * @param universalAroundScript new around script.  May be {@code null}
	 * @throws IllegalArgumentException 引数{@code context}または{@code metadata}に{@code null}を与えた場合
	 */
	public EditJiemamyContextCommand(JiemamyContext context, JmMetadata metadata, JmAroundScript universalAroundScript) {
		Validate.notNull(context);
		Validate.notNull(metadata);
		this.context = context;
		this.metadata = metadata;
		this.universalAroundScript = universalAroundScript;
		
		oldMetadata = context.getMetadata();
		oldUniversalAroundScript = context.getFacet(SqlFacet.class).getUniversalAroundScript();
	}
	
	@Override
	public void execute() {
		logger.debug("execute");
		context.setMetadata(metadata);
		context.getFacet(SqlFacet.class).setUniversalAroundScript(universalAroundScript);
	}
	
	@Override
	public void undo() {
		logger.debug("undo");
		context.setMetadata(oldMetadata);
		context.getFacet(SqlFacet.class).setUniversalAroundScript(oldUniversalAroundScript);
	}
}
