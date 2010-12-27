/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/03/03
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
package org.jiemamy.eclipse.editor.command;

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * モデル編集GEFコマンド。
 * 
 * @author daisuke
 */
public class DialogEditCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(DialogEditCommand.class);
	
	private final JiemamyFacade jiemamyFacade;
	
	private final SavePoint beforeEditSavePoint;
	
	private final SavePoint afterEditSavePoint;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param jiemamyFacade 編集に用いたファサード
	 * @param beforeEditSavePoint 編集前のセーブポイント
	 * @param afterEditSavePoint 編集後のセーブポイント
	 */
	public DialogEditCommand(JiemamyFacade jiemamyFacade, SavePoint beforeEditSavePoint, SavePoint afterEditSavePoint) {
		this.jiemamyFacade = jiemamyFacade;
		this.beforeEditSavePoint = beforeEditSavePoint;
		this.afterEditSavePoint = afterEditSavePoint;
	}
	
	@Override
	public void execute() {
		logger.debug(LogMarker.LIFECYCLE, "execute");
		// nothing to do
	}
	
	@Override
	public void redo() {
		logger.debug(LogMarker.LIFECYCLE, "redo");
		jiemamyFacade.rollback(afterEditSavePoint);
	}
	
	@Override
	public void undo() {
		logger.debug(LogMarker.LIFECYCLE, "undo");
		jiemamyFacade.rollback(beforeEditSavePoint);
	}
}
