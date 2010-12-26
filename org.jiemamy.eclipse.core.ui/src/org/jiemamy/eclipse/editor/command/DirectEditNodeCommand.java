/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.editor.command;

import ch.qos.logback.core.status.Status;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.editor.DisplayStatus;
import org.jiemamy.eclipse.extension.IllegalImplementationException;
import org.jiemamy.model.Mode;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.DatabaseObjectModel;
import org.jiemamy.transaction.SavePoint;

/**
 * ノードのダイレクト編集GEFコマンド。
 * 
 * @author daisuke
 */
public class DirectEditNodeCommand extends Command {
	
	/** 変更後の値 */
	private String newValue;
	
	/** 編集対象 */
	private final NodeModel nodeAdapter;
	
	private final DisplayStatus displayStatus;
	
	private final JiemamyViewFacade jiemamyFacade;
	
	private SavePoint savePoint;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel ルートモデル
	 * @param displayStatus ディスプレイモード
	 * @param nodeAdapter 対象となるノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public DirectEditNodeCommand(JiemamyContext rootModel, DisplayStatus displayStatus, NodeModel nodeAdapter) {
		Validate.notNull(rootModel);
		Validate.notNull(displayStatus);
		Validate.notNull(nodeAdapter);
		this.displayStatus = displayStatus;
		this.nodeAdapter = nodeAdapter;
		jiemamyFacade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
	}
	
	@Override
	public void execute() {
		DatabaseObjectModel entityModel = nodeAdapter.unwrap();
		if (entityModel != null) {
			if (displayStatus.getMode() == Mode.PHYSICAL) {
				savePoint = jiemamyFacade.save();
				jiemamyFacade.changeModelProperty(entityModel, EntityProperty.name, newValue);
			} else if (displayStatus.getMode() == Mode.LOGICAL) {
				savePoint = jiemamyFacade.save();
				jiemamyFacade.changeModelProperty(entityModel, EntityProperty.logicalName, newValue);
			} else {
				JiemamyUIPlugin.log("想定外のenum要素", Status.ERROR);
			}
		} else if (nodeAdapter instanceof StickyModel) {
			StickyModel stickyModel = (StickyModel) nodeAdapter;
			savePoint = jiemamyFacade.save();
			jiemamyFacade.changeModelProperty(stickyModel, StickyProperty.contents, newValue);
		} else {
			throw new IllegalImplementationException(nodeAdapter, "Unknown node: " + nodeAdapter.getClass());
		}
	}
	
	/**
	 * 変更後の値を設定する。
	 * 
	 * @param newValue 変更後の値
	 */
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	
	@Override
	public void undo() {
		jiemamyFacade.rollback(savePoint);
	}
}
